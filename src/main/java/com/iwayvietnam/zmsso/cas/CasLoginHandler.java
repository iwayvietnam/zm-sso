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
package com.iwayvietnam.zmsso.cas;

import com.iwayvietnam.zmsso.BaseSsoHandler;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.cas.client.CasClient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CAS SSO Login Handler
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public class CasLoginHandler extends BaseSsoHandler {
    private static final String LOGIN_HANDLER_PATH = "cas/login";
    private final CasClient client;

    public CasLoginHandler() throws ExtensionException {
        super();
        client = config.getClients().findClient(CasClient.class).orElseThrow(() -> new ExtensionException("No client found"));
    }

    @Override
    public String getPath() {
        return LOGIN_HANDLER_PATH;
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        try {
            doLogin(request, response, client);
        } catch (ServiceException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }
}
