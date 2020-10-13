/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zm SSO is the the  Zimbra Collaboration Open Source Edition extension Single Sign On authentication to the Zimbra Web Client.
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
 * Zimbra Hierarchical Address Book
 *
 * Written by Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
package com.iwayvietnam.zmsso.pac4j;

import com.iwayvietnam.zmsso.DbSsoSession;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public class ZmLogoutHandler<C extends WebContext> extends DefaultLogoutHandler<C> implements LogoutHandler<C> {
    protected static final Provisioning prov = Provisioning.getInstance();
    public static final String X_ORIGINATING_IP_HEADER = "X-Forwarded-For";
    public static final String USER_AGENT_HEADER = "User-Agent";

    @Override
    public void recordSession(final C context, final String key) {
        super.recordSession(context, key);
        getProfileManager(context).get(true).ifPresent(profile -> {
            try {
                singleLogin(context, profile.getUsername(), key, profile.getClientName());
            } catch (ServiceException e) {
                ZimbraLog.extensions.error(e);
            }
        });
    }

    @Override
    public void destroySessionFront(final C context, final String key) {
        try {
            clearAuthToken(context, key);
        } catch (AuthTokenException | ServiceException e) {
            ZimbraLog.extensions.error(e);
        }
        super.destroySessionFront(context, key);
    }

    @Override
    public void destroySessionBack(final C context, final String key) {
        try {
            singleLogout(key);
        } catch (ServiceException e) {
            ZimbraLog.extensions.error(e);
        }
        super.destroySessionBack(context, key);
    }

    private void singleLogin(final C context, String accountName, String ssoToken, String client) throws ServiceException {
        Map<String, Object> authCtxt = new HashMap<>();
        String origIp = context.getRequestHeader(X_ORIGINATING_IP_HEADER).orElse(null);
        String remoteIp = context.getRemoteAddr();
        String userAgent = context.getRequestHeader(USER_AGENT_HEADER).orElse(null);

        authCtxt.put(AuthContext.AC_REMOTE_IP, remoteIp);
        authCtxt.put(AuthContext.AC_ACCOUNT_NAME_PASSEDIN, accountName);
        authCtxt.put(AuthContext.AC_USER_AGENT, userAgent);

        Account account = prov.getAccountByName(accountName);
        prov.ssoAuthAccount(account, AuthContext.Protocol.soap, authCtxt);
        AuthToken authToken = AuthProvider.getAuthToken(account, false);
        setAuthTokenCookie(context, authToken);

        DbSsoSession.ssoSessionLogin(account, ssoToken, client, origIp, remoteIp, userAgent);
    }

    private void setAuthTokenCookie(final C context, final AuthToken authToken) throws ServiceException {
        final boolean isAdmin = AuthToken.isAnyAdmin(authToken);
        if (context instanceof JEEContext) {
            final JEEContext jeeCxt = (JEEContext) context;
            authToken.encode(jeeCxt.getNativeResponse(), isAdmin, context.isSecure());
        }
    }

    private void clearAuthToken(final C context, final String key) throws AuthTokenException, ServiceException {
        if (context instanceof JEEContext) {
            final JEEContext jeeCxt = (JEEContext) context;
            final AuthToken authToken = AuthUtil.getAuthTokenFromHttpReq(jeeCxt.getNativeRequest(), false);
            final Optional<AuthToken> optional = Optional.ofNullable(authToken);
            if (optional.isPresent()) {
                authToken.encode(jeeCxt.getNativeRequest(), jeeCxt.getNativeResponse(), true);
                authToken.deRegister();
            }
            ZimbraCookie.clearCookie(jeeCxt.getNativeResponse(), ZimbraCookie.COOKIE_ZM_AUTH_TOKEN);
            final String accountId = DbSsoSession.ssoSessionLogout(key);
            ZimbraLog.extensions.debug(String.format("SSO session logout for account id: %s", accountId));
        }
    }

    private void singleLogout(final String key) throws ServiceException {
        final String accountId = DbSsoSession.ssoSessionLogout(key);
        if (!StringUtil.isNullOrEmpty(accountId)) {
            final Account account = prov.getAccountById(accountId);
            final int validityValue = account.getAuthTokenValidityValue();
            if (validityValue > 99) {
                account.setAuthTokenValidityValue(1);
            } else {
                account.setAuthTokenValidityValue(validityValue + 1);
            }
        }
    }
}
