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
package com.iwayvietnam.zmsso.service;

import com.iwayvietnam.zmsso.ZmSsoExtension;
import com.iwayvietnam.zmsso.db.DbSsoSession;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.service.admin.AdminDocumentHandler;
import org.dom4j.QName;

import java.util.Map;

/**
 * GetAllSsoSessions class
 * @author Nguyen Van Nguyen <nguyennv@iwayvietnam.com>
 */
public class GetAllSsoSessions extends AdminDocumentHandler {
    public static QName GET_ALL_SSO_SESSIONS_REQUEST = QName.get(ZmSsoExtension.E_GET_ALL_SSO_SESSIONS_REQUEST, AdminConstants.NAMESPACE);
    public static QName GET_ALL_SSO_SESSIONS_RESPONSE = QName.get(ZmSsoExtension.E_GET_ALL_SSO_SESSIONS_RESPONSE, AdminConstants.NAMESPACE);

    @Override
    public Element handle(final Element request, final Map<String, Object> context) throws ServiceException {
        final var response = getZimbraSoapContext(context).createElement(GET_ALL_SSO_SESSIONS_RESPONSE);
        final var offset = request.getAttributeInt(AdminConstants.A_OFFSET, 0);
        final var limit = request.getAttributeInt(AdminConstants.A_LIMIT, 25);

        final var total = DbSsoSession.CountAllSsoSessions();
        final var ssoSessions = DbSsoSession.GetAllSsoSessions(offset, limit);
        ssoSessions.forEach(ssoSession -> {
            final Element element = response.addNonUniqueElement("ssoSession");
            element.addAttribute("ssoToken", ssoSession.getSsoToken());
            element.addAttribute("accountId", ssoSession.getAccountId());
            element.addAttribute("accountName", ssoSession.getAccountName());
            element.addAttribute("protocol", ssoSession.getProtocol());
            element.addAttribute("originClientIp", ssoSession.getOriginClientIp());
            element.addAttribute("remoteIp", ssoSession.getRemoteIp());
            element.addAttribute("userAgent", ssoSession.getUserAgent());
            element.addAttribute("loginAt", ssoSession.getLoginAt().toString());
            element.addAttribute("logoutAt", ssoSession.getLogoutAt().toString());
        });
        response.addAttribute(AdminConstants.A_MORE, total > ssoSessions.size());
        response.addAttribute(AdminConstants.A_TOTAL, total);
        return response;
    }
}
