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
import com.iwayvietnam.zmsso.pac4j.SettingsConstants;
import com.iwayvietnam.zmsso.saml.*;
import com.zimbra.common.localconfig.ConfigException;
import com.zimbra.common.localconfig.LocalConfig;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.extension.ExtensionDispatcherServlet;
import com.zimbra.cs.extension.ExtensionException;
import com.zimbra.cs.extension.ZimbraExtension;
import org.dom4j.DocumentException;

import java.io.IOException;

/**
 * Zimbra Single Sign On Extension
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public class ZmSsoExtension implements ZimbraExtension {
    private static final String EXTENSION_NAME = "com_iwayvietnam_zmsso";

    @Override
    public String getName() {
        return EXTENSION_NAME;
    }

    @Override
    public void init() throws ExtensionException, ServiceException {
        ZimbraLog.extensions.info("Create sso session table");
        DbSsoSession.createSsoSessionTable();

        ZimbraLog.extensions.info("Init sso config");
        initSsoConfig();

        ZimbraLog.extensions.info("Register sso handlers");
        ExtensionDispatcherServlet.register(this, new LoginHandler());
        ExtensionDispatcherServlet.register(this, new CallbackHandler());
        ExtensionDispatcherServlet.register(this, new LogoutHandler());

        ZimbraLog.extensions.info("Register sso login handlers");
        ExtensionDispatcherServlet.register(this, new CasLoginHandler());
        ExtensionDispatcherServlet.register(this, new OidcLoginHandler());
        ExtensionDispatcherServlet.register(this, new SamlLoginHandler());

        ZimbraLog.extensions.info("Register saml metadata handlers");
        ExtensionDispatcherServlet.register(this, new SamlMetadataHandler());
    }

    @Override
    public void destroy() {
        ZimbraLog.extensions.info("Unregister sso extension");
        ExtensionDispatcherServlet.unregister(this);
    }

    private static void initSsoConfig() {
        try {
            final LocalConfig lc = new LocalConfig(null);
            boolean changed = false;

            if (!lc.isSet(SettingsConstants.ZM_SSO_DEFAULT_CLIENT)) {
                lc.set(SettingsConstants.ZM_SSO_DEFAULT_CLIENT, "SAML2Client");
                changed = true;
            }

            ZimbraLog.extensions.info("Callback configuration");
            if (!lc.isSet(SettingsConstants.ZM_SSO_CALLBACK_URL)) {
                final Config config = Provisioning.getInstance().getConfig();

                final StringBuilder callbackUrl = new StringBuilder();
                callbackUrl.append(config.getPublicServiceProtocol())
                    .append("://")
                    .append(config.getPublicServiceHostname())
                    .append(":")
                    .append(config.getPublicServicePort())
                    .append(ExtensionDispatcherServlet.EXTENSION_PATH)
                    .append(CallbackHandler.HANDLER_PATH);

                lc.set(SettingsConstants.ZM_SSO_CALLBACK_URL, callbackUrl.toString());
                changed = true;
            }
            if (!lc.isSet(SettingsConstants.ZM_SSO_SAVE_IN_SESSION)) {
                lc.set(SettingsConstants.ZM_SSO_SAVE_IN_SESSION, "true");
                changed = true;
            }
            if (!lc.isSet(SettingsConstants.ZM_SSO_MULTI_PROFILE)) {
                lc.set(SettingsConstants.ZM_SSO_MULTI_PROFILE, "true");
                changed = true;
            }
            if (!lc.isSet(SettingsConstants.ZM_SSO_RENEW_SESSION)) {
                lc.set(SettingsConstants.ZM_SSO_RENEW_SESSION, "true");
                changed = true;
            }

            ZimbraLog.extensions.info("Logout configuration");
            if (!lc.isSet(SettingsConstants.ZM_SSO_LOCAL_LOGOUT)) {
                lc.set(SettingsConstants.ZM_SSO_LOCAL_LOGOUT, "true");
                changed = true;
            }
            if (!lc.isSet(SettingsConstants.ZM_SSO_DESTROY_SESSION)) {
                lc.set(SettingsConstants.ZM_SSO_DESTROY_SESSION, "true");
                changed = true;
            }
            if (!lc.isSet(SettingsConstants.ZM_SSO_CENTRAL_LOGOUT)) {
                lc.set(SettingsConstants.ZM_SSO_CENTRAL_LOGOUT, "true");
                changed = true;
            }

            if (changed) {
                lc.save();
            }
        } catch (final ConfigException | DocumentException | IOException | ServiceException e) {
            ZimbraLog.extensions.error(e);
        }
    }
}
