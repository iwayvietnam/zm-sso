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
package com.iwayvietnam.zmsso;

import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.util.Pac4jConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public class LogoutHandler extends BaseSsoHandler {
    private static final String LOGOUT_HANDLER_PATH = "logout";

    public LogoutHandler() throws ExtensionException {
        super();
    }

    @Override
    public String getPath() {
        return LOGOUT_HANDLER_PATH;
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        final String defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;
        final String logoutUrlPattern = Pac4jConstants.DEFAULT_LOGOUT_URL_PATTERN_VALUE;
        final boolean localLogout = true;
        final boolean destroySession = true;
        final boolean centralLogout = true;
        final JEEContext context = new JEEContext(request, response);
        DefaultLogoutLogic.INSTANCE.perform(context, config, JEEHttpActionAdapter.INSTANCE, defaultUrl, logoutUrlPattern, localLogout, destroySession, centralLogout);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }
}
