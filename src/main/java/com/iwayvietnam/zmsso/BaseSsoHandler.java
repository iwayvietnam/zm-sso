/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zm SSO is the Zimbra Collaboration Open Source Edition extension for single sign-on authentication to the Zimbra Web Client.
 * Copyright (C) 2020-present iWay Vietnam and/or its affiliates. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 * ***** END LICENSE BLOCK *****
 *
 * Zimbra Single Sign On
 *
 * Written by Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
package com.iwayvietnam.zmsso;

import com.iwayvietnam.zmsso.pac4j.ConfigBuilder;
import com.iwayvietnam.zmsso.util.Log;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.*;
import com.zimbra.cs.extension.ExtensionHttpHandler;

import com.zimbra.cs.servlet.util.AuthUtil;
import org.pac4j.core.client.Client;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.http.adapter.JEEHttpActionAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.iwayvietnam.zmsso.pac4j.SettingsConstants.SSO_CLIENT_NAME_SESSION_ATTR;
import static com.iwayvietnam.zmsso.pac4j.SettingsConstants.SSO_PROFILE_SESSION_ATTR;

/**
 * Base Sso Handler
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public abstract class BaseSsoHandler extends ExtensionHttpHandler {
    protected final ConfigBuilder configBuilder;

    public BaseSsoHandler() {
        configBuilder = ConfigBuilder.getInstance();
    }

    protected void doLogin(final HttpServletRequest request, final HttpServletResponse response, final Client client) throws IOException, ServiceException {
        if (!isLoggedIn(request)) {
            Log.sso.info("SSO login with: %s", client.getName());
            request.getSession().setAttribute(SSO_CLIENT_NAME_SESSION_ATTR, client.getName());
            final var config = configBuilder.getConfig();
            final var context = config.getWebContextFactory().newContext(request, response);
            final var sessionStore = config.getSessionStoreFactory().newSessionStore();
            client.getRedirectionAction(context, sessionStore).ifPresent(action -> {
                JEEHttpActionAdapter.INSTANCE.adapt(action, context);
            });
        }
        else {
            redirectToMail(request, response);
        }
    }

    protected void doCallback(final HttpServletRequest request, final HttpServletResponse response, final Client client) {
        Log.sso.info("SSO callback with: %s", client.getName());

        final var defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;
        final var renewSession = configBuilder.getRenewSession();

        final var config = configBuilder.getConfig();
        final var context = config.getWebContextFactory().newContext(request, response);
        final var sessionStore = config.getSessionStoreFactory().newSessionStore();
        config.getCallbackLogic().perform(
            context,
            sessionStore,
            config,
            JEEHttpActionAdapter.INSTANCE,
            defaultUrl,
            renewSession,
            client.getName()
        );
        Log.sso.info("SSO callback is performed with: %s", client.getName());

        final var manager = new ProfileManager(context, sessionStore);
        manager.setConfig(config);
        manager.getProfile(CommonProfile.class).ifPresent(profile -> {
            request.getSession().setAttribute(SSO_PROFILE_SESSION_ATTR, profile);
            final var logoutHandler = configBuilder.getLogoutHandler();
            final var nameAttr = configBuilder.getAccountNameAttr();
            final var accountName = Optional.ofNullable((String) profile.getAttribute(nameAttr))
                    .orElse(profile.getUsername());
            final var sessionId = sessionStore.getSessionId(context, true).orElse("");
            final var sessionKey = (String) logoutHandler.getStore().get(sessionId).orElse(sessionId);
            try {
                logoutHandler.singleLogin(context, accountName, sessionKey, profile.getClientName());
            } catch (final ServiceException e) {
                Log.sso.error(e);
            }
        });
    }

    private boolean isLoggedIn(final HttpServletRequest request) {
        final var authToken = AuthUtil.getAuthTokenFromHttpReq(request, false);
        return Optional.ofNullable(authToken).isPresent() && !authToken.isExpired() && authToken.isRegistered();
    }

    private void redirectToMail(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServiceException {
        final var redirectUrl = AuthUtil.getRedirectURL(request, Provisioning.getInstance().getLocalServer(), false, true) + AuthUtil.IGNORE_LOGIN_URL;
        response.sendRedirect(redirectUrl);
    }
}
