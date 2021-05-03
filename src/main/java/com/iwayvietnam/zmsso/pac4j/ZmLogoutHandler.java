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
package com.iwayvietnam.zmsso.pac4j;

import com.iwayvietnam.zmsso.db.DbSsoSession;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.auth.AuthContext;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.servlet.util.AuthUtil;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.logout.handler.DefaultLogoutHandler;
import org.pac4j.core.logout.handler.LogoutHandler;
import org.pac4j.core.profile.CommonProfile;

import java.util.HashMap;
import java.util.Optional;

/**
 * Pac4j Logout Handler
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 * Logout url:  https://mail.zimbra-server.com/?loginOp=logout
 */
public final class ZmLogoutHandler<C extends WebContext> extends DefaultLogoutHandler<C> implements LogoutHandler<C> {
    private static final Provisioning prov = Provisioning.getInstance();
    private static final String X_ORIGINATING_IP_HEADER = LC.zimbra_http_originating_ip_header.value();
    private static final String USER_AGENT_HEADER = "User-Agent";

    /**
     * Associates a key with the current web session.
     * @param context the web context
     * @param key the key
     */
    @Override
    public void recordSession(final C context, final String key) {
        super.recordSession(context, key);
        getProfileManager(context).get(true).ifPresent(profile -> {
            try {
                final var commonProfile = (CommonProfile) profile;
                final var accountName = Optional.ofNullable(commonProfile.getEmail()).orElse(commonProfile.getUsername());
                singleLogin(context, accountName, key, commonProfile.getClientName());
            } catch (final ServiceException e) {
                ZimbraLog.extensions.error(e);
            }
        });
    }

    /**
     * Destroys the current web session for the given key for a front channel logout.
     * @param context the web context
     * @param key the key
     */
    @Override
    public void destroySessionFront(final C context, final String key) {
        try {
            clearAuthToken(context, key);
        } catch (final ServiceException e) {
            ZimbraLog.extensions.error(e);
        }
        super.destroySessionFront(context, key);
    }

    /**
     * Destroys the current web session for the given key for a back channel logout.
     * @param context the web context
     * @param key the key
     */
    @Override
    public void destroySessionBack(final C context, final String key) {
        try {
            singleLogout(key);
        } catch (final ServiceException e) {
            ZimbraLog.extensions.error(e);
        }
        super.destroySessionBack(context, key);
    }

    private void singleLogin(final C context, final String accountName, final String key, final String client) throws ServiceException {
        final var authCtxt = new HashMap<String, Object>();
        final var remoteIp = context.getRemoteAddr();
        final var origIp = context.getRequestHeader(X_ORIGINATING_IP_HEADER).orElse(remoteIp);
        final var userAgent = context.getRequestHeader(USER_AGENT_HEADER).orElse(null);

        authCtxt.put(AuthContext.AC_ORIGINATING_CLIENT_IP, origIp);
        authCtxt.put(AuthContext.AC_REMOTE_IP, remoteIp);
        authCtxt.put(AuthContext.AC_ACCOUNT_NAME_PASSEDIN, accountName);
        authCtxt.put(AuthContext.AC_USER_AGENT, userAgent);

        final var account = prov.getAccountByName(accountName);
        prov.ssoAuthAccount(account, AuthContext.Protocol.soap, authCtxt);
        final var authToken = AuthProvider.getAuthToken(account, false);
        setAuthTokenCookie(context, authToken);

        DbSsoSession.ssoSessionLogin(account, key, client, origIp, remoteIp, userAgent);
    }

    private void setAuthTokenCookie(final C context, final AuthToken authToken) throws ServiceException {
        if (context instanceof JEEContext) {
            final var isAdmin = AuthToken.isAnyAdmin(authToken);
            final var jeeCxt = (JEEContext) context;
            authToken.encode(jeeCxt.getNativeResponse(), isAdmin, context.isSecure());
            ZimbraLog.extensions.debug(String.format("Set auth token cookie for account id: %s", authToken.getAccountId()));
        }
    }

    private void clearAuthToken(final C context, final String key) throws ServiceException {
        if (context instanceof JEEContext) {
            final var jeeCxt = (JEEContext) context;
            final var authToken = AuthUtil.getAuthTokenFromHttpReq(jeeCxt.getNativeRequest(), false);
            final var optional = Optional.ofNullable(authToken);
            if (optional.isPresent()) {
                authToken.encode(jeeCxt.getNativeRequest(), jeeCxt.getNativeResponse(), true);
                try {
                    authToken.deRegister();
                } catch (final AuthTokenException e) {
                    throw ServiceException.FAILURE(e.getMessage(), e);
                }
            }
            ZimbraCookie.clearCookie(jeeCxt.getNativeResponse(), ZimbraCookie.COOKIE_ZM_AUTH_TOKEN);
            final var accountId = DbSsoSession.ssoSessionLogout(key);
            ZimbraLog.extensions.debug(String.format("SSO session logout for account id: %s", accountId));
        }
    }

    private void singleLogout(final String key) throws ServiceException {
        final var accountId = DbSsoSession.ssoSessionLogout(key);
        if (!StringUtil.isNullOrEmpty(accountId)) {
            ZimbraLog.extensions.debug(String.format("SSO single logout for account id: %s", accountId));
            final var account = prov.getAccountById(accountId);
            final var validityValue = account.getAuthTokenValidityValue();
            if (validityValue > 99) {
                account.setAuthTokenValidityValue(1);
            } else {
                account.setAuthTokenValidityValue(validityValue + 1);
            }
        }
    }
}
