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

import com.iwayvietnam.zmsso.pac4j.SettingsBuilder;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.*;
import com.zimbra.cs.extension.ExtensionHttpHandler;
import com.zimbra.cs.httpclient.URLUtil;

import com.zimbra.cs.servlet.util.AuthUtil;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.JEEContextFactory;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.util.Pac4jConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

/**
 * Base Sso Handler
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public abstract class BaseSsoHandler extends ExtensionHttpHandler {
    protected static final String SSO_CLIENT_NAME_SESSION_ATTR = "sso.ClientName";

    protected final Config config;

    public BaseSsoHandler() {
        config = SettingsBuilder.getConfig();
    }

    protected void doLogin(final HttpServletRequest request, final HttpServletResponse response, final Client client) throws IOException, ServiceException {
        final var authToken = AuthUtil.getAuthTokenFromHttpReq(request, false);
        if (!isLoggedIn(authToken)) {
            ZimbraLog.extensions.debug(String.format("SSO login with: %s", client.getName()));
            request.getSession().setAttribute(SSO_CLIENT_NAME_SESSION_ATTR, client.getName());
            final var context = JEEContextFactory.INSTANCE.newContext(request, response);
            client.getRedirectionAction(context, JEESessionStore.INSTANCE).ifPresent(action -> JEEHttpActionAdapter.INSTANCE.adapt(action, context));
        } else {
            redirectByAuthToken(request, response, authToken);
        }
    }

    protected void doCallback(final HttpServletRequest request, final HttpServletResponse response, final Client client) {
        final var defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;
        final var renewSession = SettingsBuilder.renewSession();
        final var context = JEEContextFactory.INSTANCE.newContext(request, response);
        DefaultCallbackLogic.INSTANCE.perform(context, JEESessionStore.INSTANCE, config, JEEHttpActionAdapter.INSTANCE, defaultUrl, renewSession, client.getName());
    }

    private boolean isLoggedIn(final AuthToken authToken) {
        if (authToken != null) {
            return !authToken.isExpired() && authToken.isRegistered();
        }
        return false;
    }

    private void redirectByAuthToken(final HttpServletRequest request, final HttpServletResponse response, final AuthToken authToken) throws IOException, ServiceException {
        final var isAdmin = AuthToken.isAnyAdmin(authToken);
        final var server = authToken.getAccount().getServer();
        final var redirectUrl = AuthUtil.getRedirectURL(request, server, isAdmin, true) + AuthUtil.IGNORE_LOGIN_URL;

        final var url = new URL(redirectUrl);
        final var isRedirectProtocolSecure = isProtocolSecure(url.getProtocol());
        final var secureCookie = isProtocolSecure(request.getScheme());

        if (secureCookie && !isRedirectProtocolSecure) {
            throw ServiceException.INVALID_REQUEST(String.format("Cannot redirect to non-secure protocol: %s", redirectUrl), null);
        }

        ZimbraLog.extensions.debug(String.format("SSO login - redirecting (with auth token) to: %s", redirectUrl));
        response.sendRedirect(redirectUrl);
    }

    private boolean isProtocolSecure(final String protocol) {
        return URLUtil.PROTO_HTTPS.equalsIgnoreCase(protocol);
    }
}
