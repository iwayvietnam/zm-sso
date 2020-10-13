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
import com.zimbra.cs.extension.ExtensionException;
import com.zimbra.cs.extension.ExtensionHttpHandler;
import com.zimbra.cs.httpclient.URLUtil;

import com.zimbra.cs.servlet.util.AuthUtil;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/**
 * Base Sso Handler
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public abstract class BaseSsoHandler extends ExtensionHttpHandler {
    protected final Config config;

    public BaseSsoHandler() throws ExtensionException {
        config = SettingsBuilder.build();
    }

    protected boolean isLogin(final AuthToken authToken) {
        final Optional<AuthToken> optional = Optional.ofNullable(authToken);
        return optional.isPresent() && !authToken.isExpired() && authToken.isRegistered();
    }

    protected void doLogin(final HttpServletRequest request, final HttpServletResponse response, final Client client) throws IOException, ServiceException {
        final AuthToken authToken = AuthUtil.getAuthTokenFromHttpReq(request, false);
        if (!isLogin(authToken)) {
            final JEEContext context = new JEEContext(request, response);
            RedirectionAction action;

            try {
                final Optional<RedirectionAction> actionOpt = client.getRedirectionAction(context);
                action = actionOpt.get();
            } catch (final RedirectionAction e) {
                action = e;
            }

            JEEHttpActionAdapter.INSTANCE.adapt(action, context);
        } else {
            redirectByAuthToken(request, response, authToken);
        }
    }

    protected void redirectByAuthToken(final HttpServletRequest request, final HttpServletResponse response, final AuthToken authToken) throws IOException, ServiceException {
        final boolean isAdmin = AuthToken.isAnyAdmin(authToken);
        final boolean secureCookie = isProtocolSecure(request.getScheme());

        final Server server = authToken.getAccount().getServer();
        String redirectUrl = AuthUtil.getRedirectURL(request, server, isAdmin, false);
        redirectUrl = appendIgnoreLoginURL(redirectUrl);

        final URL url = new URL(redirectUrl);
        boolean isRedirectProtocolSecure = isProtocolSecure(url.getProtocol());

        if (secureCookie && !isRedirectProtocolSecure) {
            throw ServiceException.INVALID_REQUEST(String.format("Cannot redirect to non-secure protocol: %s", redirectUrl), null);
        }

        ZimbraLog.extensions.debug(String.format("SSO Login - redirecting (with auth token) to: %s", redirectUrl));
        response.sendRedirect(redirectUrl);
    }

    private String appendIgnoreLoginURL(String redirectUrl) {
        if (!redirectUrl.endsWith("/")) {
            redirectUrl = redirectUrl + "/";
        }
        return redirectUrl + AuthUtil.IGNORE_LOGIN_URL;
    }

    private boolean isProtocolSecure(final String protocol) {
        return URLUtil.PROTO_HTTPS.equalsIgnoreCase(protocol);
    }
}
