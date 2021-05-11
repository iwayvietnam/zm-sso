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
import com.zimbra.cs.extension.ExtensionDispatcherServlet;
import com.zimbra.cs.extension.ExtensionHttpHandler;

import com.zimbra.cs.servlet.util.AuthUtil;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.factory.ProfileManagerFactory;
import org.pac4j.core.util.FindBest;

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
            ZimbraLog.extensions.debug("SSO login with: {}", client.getName());
            request.getSession().setAttribute(SSO_CLIENT_NAME_SESSION_ATTR, client.getName());
            final var context = new JEEContext(request, response);
            configBuilder.clientInit();
            JEEHttpActionAdapter.INSTANCE.adapt((RedirectionAction) client.getRedirectionAction(context).get(), context);
        }
        else {
            redirectToMail(request, response);
        }
    }

    protected void doCallback(final HttpServletRequest request, final HttpServletResponse response, final Client client) {
        final var defaultUrl = String.format("%s/%s", ExtensionDispatcherServlet.EXTENSION_PATH, SecurityHandler.HANDLER_PATH);
        final var saveInSession = configBuilder.getSaveInSession();
        final var multiProfile = configBuilder.getMultiProfile();
        final var renewSession = configBuilder.getRenewSession();
        ZimbraLog.extensions.debug("SSO callback with: {}", client.getName());
        configBuilder.clientInit();
        DefaultCallbackLogic.INSTANCE.perform(new JEEContext(request, response), configBuilder.getConfig(), JEEHttpActionAdapter.INSTANCE, defaultUrl, multiProfile, saveInSession, renewSession, client.getName());
    }

    protected void doSecurity(final HttpServletRequest request, final HttpServletResponse response) throws ServiceException, IOException {
        if (!isLoggedIn(request)) {
            ZimbraLog.extensions.debug("SSO security check");
            final var context = new JEEContext(request, response);
            final var profileManager = FindBest.profileManagerFactory(null, configBuilder.getConfig(), ProfileManagerFactory.DEFAULT).apply(context);
            profileManager.get(true).ifPresent(profile -> {
                ZimbraLog.extensions.debug("Profile: {}", profile);
                if (profile instanceof CommonProfile) {
                    final var commonProfile = (CommonProfile) profile;
                    final var accountName = Optional.ofNullable(commonProfile.getEmail()).orElse(commonProfile.getId());
                    final var sessionId = context.getSessionStore().getOrCreateSessionId(context);
                    final var sessionKey = configBuilder.getLogoutHandler().getStore().get(sessionId).orElse(null).toString();

                    try {
                        configBuilder.getLogoutHandler().singleLogin(context, accountName, sessionKey, commonProfile.getClientName());
                    } catch (ServiceException e) {
                        ZimbraLog.extensions.error(e);
                    }
                }
            });
        }
        redirectToMail(request, response);
    }

    private boolean isLoggedIn(final HttpServletRequest request) {
        final var authToken = AuthUtil.getAuthTokenFromHttpReq(request, false);
        return Optional.ofNullable(authToken).isPresent() && !authToken.isExpired() && authToken.isRegistered();
    }

    private void redirectToMail(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServiceException {
        final var redirectUrl = AuthUtil.getRedirectURL(request, Provisioning.getInstance().getLocalServer(), false, true) + AuthUtil.IGNORE_LOGIN_URL;
        ZimbraLog.extensions.debug("Redirecting to url: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
