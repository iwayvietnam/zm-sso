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

import com.zimbra.common.util.ZimbraLog;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.engine.DefaultLogoutLogic;
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
        final var defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;
        final var logoutUrlPattern = Pac4jConstants.DEFAULT_LOGOUT_URL_PATTERN_VALUE;

        final var localLogout = configBuilder.getLocalLogout();
        final var destroySession = configBuilder.getDestroySession();
        final var centralLogout = configBuilder.getCentralLogout();

        try {
            configBuilder.clientInit();
            DefaultLogoutLogic.INSTANCE.perform(new JEEContext(request, response), configBuilder.getConfig(), JEEHttpActionAdapter.INSTANCE, defaultUrl, logoutUrlPattern, localLogout, destroySession, centralLogout);
        }
        catch (RuntimeException rte) {
            throw new ServletException(rte);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }
}
