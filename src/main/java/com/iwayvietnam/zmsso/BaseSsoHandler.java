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
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.*;
import com.zimbra.cs.extension.ExtensionHttpHandler;

import com.zimbra.cs.servlet.util.AuthUtil;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.util.Pac4jConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Base Sso Handler
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public abstract class BaseSsoHandler extends ExtensionHttpHandler {
    protected static final String SSO_CLIENT_NAME_SESSION_ATTR = "sso.ClientName";

    protected final ConfigBuilder configBuilder;

    public BaseSsoHandler() {
        configBuilder = ConfigBuilder.getInstance();
    }

    protected void doLogin(final HttpServletRequest request, final HttpServletResponse response, final Client client) throws IOException, ServiceException {
        if (!isLoggedIn(request)) {
            ZimbraLog.extensions.info("SSO login with: %s", client.getName());
            request.getSession().setAttribute(SSO_CLIENT_NAME_SESSION_ATTR, client.getName());
            final var context = new JEEContext(request, response);
            final Optional<RedirectionAction> loginAction = client.getRedirectionAction(context);
            loginAction.ifPresent(action -> {
                ZimbraLog.extensions.debug("Adapt redirection action: %s", action);
                JEEHttpActionAdapter.INSTANCE.adapt(action, context);
            });
        }
        else {
            redirectToMail(request, response);
        }
    }

    protected void doCallback(final HttpServletRequest request, final HttpServletResponse response, final Client client) {
        ZimbraLog.extensions.info("SSO callback with: %s", client.getName());

        final var defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;
        final var saveInSession = configBuilder.getSaveInSession();
        final var multiProfile = configBuilder.getMultiProfile();
        final var renewSession = configBuilder.getRenewSession();

        final var context = new JEEContext(request, response);
        DefaultCallbackLogic.INSTANCE.perform(context, configBuilder.getConfig(), JEEHttpActionAdapter.INSTANCE, defaultUrl, multiProfile, saveInSession, renewSession, client.getName());
        ZimbraLog.extensions.info("SSO callback is performed");

        final var manager = new ProfileManager<CommonProfile>(context);
        manager.get(saveInSession).ifPresent(profile -> {
            final var logoutHandler = configBuilder.getLogoutHandler();
            final var accountName = Optional.ofNullable(profile.getEmail()).orElse(profile.getId());
            final var sessionId = context.getSessionStore().getOrCreateSessionId(context);
            final var sessionKey = (String) logoutHandler.getStore().get(sessionId).orElse(sessionId);
            try {
                logoutHandler.singleLogin(context, accountName, sessionKey, profile.getClientName());
            } catch (ServiceException e) {
                ZimbraLog.extensions.error(e);
            }
        });
    }

    private boolean isLoggedIn(final HttpServletRequest request) {
        final var authToken = AuthUtil.getAuthTokenFromHttpReq(request, false);
        return Optional.ofNullable(authToken).isPresent() && !authToken.isExpired() && authToken.isRegistered();
    }

    private void redirectToMail(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServiceException {
        final var redirectUrl = AuthUtil.getRedirectURL(request, Provisioning.getInstance().getLocalServer(), false, true) + AuthUtil.IGNORE_LOGIN_URL;
        ZimbraLog.extensions.debug("Redirecting to url: %s", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
