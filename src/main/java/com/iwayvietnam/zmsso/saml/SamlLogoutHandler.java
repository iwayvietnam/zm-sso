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
package com.iwayvietnam.zmsso.saml;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.oidc.profile.OidcProfile;
import org.pac4j.saml.profile.SAML2Profile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public class SamlLogoutHandler extends SamlBaseHandler {
    public static final String LOGOUT_HANDLER_PATH = "saml/logout";

    public SamlLogoutHandler() throws ExtensionException {
        super();
    }

    @Override
    public String getPath() {
        return LOGOUT_HANDLER_PATH;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final JEEContext context = new JEEContext(request, response);
        ProfileManager<SAML2Profile> manager = new ProfileManager<>(context);
        manager.setConfig(pac4jConfig);

        SAML2Profile profile = manager.get(true).get();
        try {
            clearAuthToken(request, response, profile.getSessionIndex());
            manager.logout();
        } catch (AuthTokenException | ServiceException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }
}
