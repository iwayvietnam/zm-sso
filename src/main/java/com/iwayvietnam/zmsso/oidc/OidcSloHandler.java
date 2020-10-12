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
package com.iwayvietnam.zmsso.oidc;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.util.Pac4jConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.Optional;

/**
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public class OidcSloHandler extends OidcBaseHandler {
    public static final String SLO_HANDLER_PATH = "oidc/slo";

    public OidcSloHandler() throws ExtensionException {
        super();
    }

    @Override
    public String getPath() {
        return SLO_HANDLER_PATH;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final JEEContext context = new JEEContext(request, response);
        try {
            final Optional<String> logoutToken = context.getRequestParameter("logout_token");
            String sid;
            if (logoutToken.isPresent()) {
                final JWT jwt = JWTParser.parse(logoutToken.get());
                sid = (String) jwt.getJWTClaimsSet().getClaim(Pac4jConstants.OIDC_CLAIM_SESSIONID);
            } else {
                sid = context.getRequestParameter(Pac4jConstants.OIDC_CLAIM_SESSIONID).orElse(null);
            }
            singleLogout(sid);
        } catch (ServiceException | ParseException e) {
            throw new ServletException(e);
        }
        context.setResponseHeader("Cache-Control", "no-cache, no-store");
        context.setResponseHeader("Pragma", "no-cache");
    }
}
