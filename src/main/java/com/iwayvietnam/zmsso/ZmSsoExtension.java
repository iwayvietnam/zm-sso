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

import com.iwayvietnam.zmsso.cas.*;
import com.iwayvietnam.zmsso.db.DbSsoSession;
import com.iwayvietnam.zmsso.oidc.*;
import com.iwayvietnam.zmsso.saml.*;
import com.iwayvietnam.zmsso.service.GetAllSsoSessions;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.extension.ExtensionDispatcherServlet;
import com.zimbra.cs.extension.ExtensionException;
import com.zimbra.cs.extension.ZimbraExtension;
import com.zimbra.soap.SoapServlet;

/**
 * Zimbra Single Sign On Extension
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public class ZmSsoExtension implements ZimbraExtension {
    public static final String E_GET_ALL_SSO_SESSIONS_REQUEST = "GetAllSsoSessionsequest";
    public static final String E_GET_ALL_SSO_SESSIONS_RESPONSE = "GetAllSsoSessionsResponse";

    private static final String EXTENSION_NAME = "com_iwayvietnam_zmsso";

    @Override
    public String getName() {
        return EXTENSION_NAME;
    }

    @Override
    public void init() throws ExtensionException, ServiceException {
        DbSsoSession.createSsoSessionTable();

        ZimbraLog.extensions.info("Register sso handlers");
        ExtensionDispatcherServlet.register(this, new LoginHandler());
        ExtensionDispatcherServlet.register(this, new CallbackHandler());
        ExtensionDispatcherServlet.register(this, new LogoutHandler());

        try {
            ZimbraLog.extensions.info("Register saml sso handlers");
            ExtensionDispatcherServlet.register(this, new SamlMetadataHandler());
            ExtensionDispatcherServlet.register(this, new SamlLoginHandler());
            ExtensionDispatcherServlet.register(this, new SamlCallbackHandler());
            ExtensionDispatcherServlet.register(this, new SamlSloHandler());
        } catch (ExtensionException e) {
            ZimbraLog.extensions.error(e);
        }

        try {
            ZimbraLog.extensions.info("Register cas sso handlers");
            ExtensionDispatcherServlet.register(this, new CasLoginHandler());
            ExtensionDispatcherServlet.register(this, new CasCallbackHandler());
        } catch (ExtensionException e) {
            ZimbraLog.extensions.error(e);
        }

        try {
            ZimbraLog.extensions.info("Register oidc sso handlers");
            ExtensionDispatcherServlet.register(this, new OidcLoginHandler());
            ExtensionDispatcherServlet.register(this, new OidcCallbackHandler());
        } catch (ExtensionException e) {
            ZimbraLog.extensions.error(e);
        }

        SoapServlet.addService("AdminServlet", dispatcher -> {
            ZimbraLog.extensions.info("Register admin soap services");
            dispatcher.registerHandler(GetAllSsoSessions.GET_ALL_SSO_SESSIONS_REQUEST, new GetAllSsoSessions());
        });
    }

    @Override
    public void destroy() {
        ZimbraLog.extensions.info("Unregister sso extension");
        ExtensionDispatcherServlet.unregister(this);
    }
}
