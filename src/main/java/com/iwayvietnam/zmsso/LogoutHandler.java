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

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.servlet.util.AuthUtil;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.util.Pac4jConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SSO Logout Handler
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public class LogoutHandler extends BaseSsoHandler {
    public static final String HANDLER_PATH = "/sso/logout";

    @Override
    public String getPath() {
        return HANDLER_PATH;
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        try {
            clearAuthToken(request, response);
        }
        catch (final ServiceException | AuthTokenException e) {
            throw new ServletException(e);
        }
        final var defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;
        final var logoutUrlPattern = Pac4jConstants.DEFAULT_LOGOUT_URL_PATTERN_VALUE;

        final var localLogout = configBuilder.getLocalLogout();
        final var destroySession = configBuilder.getDestroySession();
        final var centralLogout = configBuilder.getCentralLogout();

        try {
            DefaultLogoutLogic.INSTANCE.perform(new JEEContext(request, response), configBuilder.getConfig(), JEEHttpActionAdapter.INSTANCE, defaultUrl, logoutUrlPattern, localLogout, destroySession, centralLogout);
            ZimbraLog.extensions.info("SSO logout is performed");
        }
        catch (TechnicalException ex) {
            ZimbraLog.extensions.error(ex);
            throw new ServletException(ex);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }

    private void clearAuthToken(final HttpServletRequest request, final HttpServletResponse response) throws ServiceException, AuthTokenException {
        final var authToken = AuthUtil.getAuthTokenFromHttpReq(request, false);
        if (authToken != null) {
            authToken.encode(request, response, true);
            authToken.deRegister();
        }
        ZimbraCookie.clearCookie(response, ZimbraCookie.COOKIE_ZM_AUTH_TOKEN);
    }
}
