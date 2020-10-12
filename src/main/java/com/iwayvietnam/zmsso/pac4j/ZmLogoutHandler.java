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
import com.zimbra.cs.servlet.util.AuthUtil;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.logout.handler.DefaultLogoutHandler;
import org.pac4j.core.logout.handler.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public class ZmLogoutHandler<C extends WebContext> extends DefaultLogoutHandler<C> implements LogoutHandler<C> {

    @Override
    public void recordSession(final C context, final String key) {
        super.recordSession(context, key);
    }

    @Override
    public void destroySessionFront(final C context, final String key) {
        if (context instanceof JEEContext) {
            JEEContext jCxt = (JEEContext) context;
            try {
                clearAuthToken(jCxt.getNativeRequest(), jCxt.getNativeResponse(), key);
            } catch (AuthTokenException | ServiceException e) {
                ZimbraLog.extensions.error(e);
            }
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

    private void clearAuthToken(HttpServletRequest request, HttpServletResponse response, String key) throws AuthTokenException, ServiceException {
        AuthToken authToken = AuthUtil.getAuthTokenFromHttpReq(request, false);
        final Optional<AuthToken> optional = Optional.ofNullable(authToken);
        if (optional.isPresent()) {
            authToken.encode(request, response, true);
            authToken.deRegister();
        }
        ZimbraCookie.clearCookie(response, ZimbraCookie.COOKIE_ZM_AUTH_TOKEN);
        String accountId = DbSsoSession.ssoSessionLogout(key);
        ZimbraLog.extensions.debug(String.format("SSO session logout for account id: %s", accountId));
    }

    private void singleLogout(String key) throws ServiceException {
        String accountId = DbSsoSession.ssoSessionLogout(key);
        if (!StringUtil.isNullOrEmpty(accountId)) {
            Account account = Provisioning.getInstance().getAccountById(accountId);
            int validityValue = account.getAuthTokenValidityValue();
            if (validityValue > 99) {
                validityValue = 0;
            }
            account.setAuthTokenValidityValue(validityValue + 1);
        }
    }
}
