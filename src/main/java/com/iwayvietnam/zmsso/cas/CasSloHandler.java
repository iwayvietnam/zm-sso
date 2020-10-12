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
package com.iwayvietnam.zmsso.cas;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.util.CommonHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public class CasSloHandler extends CasBaseHandler {
    public static final String SLO_HANDLER_PATH = "cas/slo";

    public CasSloHandler() throws ExtensionException {
        super();
    }

    @Override
    public String getPath() {
        return SLO_HANDLER_PATH;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final JEEContext context = new JEEContext(request, response);
        if (isBackLogoutRequest(context)) {
            try {
                final String logoutMessage = context.getRequestParameter(CasConfiguration.LOGOUT_REQUEST_PARAMETER).get();
                final String ticket = CommonHelper.substringBetween(logoutMessage, CasConfiguration.SESSION_INDEX_TAG + ">", "</");
                singleLogout(ticket);
            } catch (ServiceException e) {
                throw new ServletException(e);
            }
        }
        context.setResponseHeader("Cache-Control", "no-cache, no-store");
        context.setResponseHeader("Pragma", "no-cache");
    }
}
