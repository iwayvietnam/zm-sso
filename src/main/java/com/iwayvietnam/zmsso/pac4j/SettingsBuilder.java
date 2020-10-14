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
package com.iwayvietnam.zmsso.pac4j;

import com.nimbusds.jose.JWSAlgorithm;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.config.client.PropertiesConfigFactory;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.logout.handler.LogoutHandler;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Pac4j Settings Builder
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public final class SettingsBuilder {
    private static final String ZM_SSO_SETTINGS_FILE = "sso.pac4j.properties";
    private static final String ZM_SSO_DEFAULT_CLIENT = "sso.defaultClient";
    private static final String ZM_SSO_CALLBACK_URL = "sso.callbackUrl";

    private static final String ZM_SSO_SAVE_IN_SESSION = "sso.saveInSession";
    private static final String ZM_SSO_MULTI_PROFILE = "sso.multiProfile";
    private static final String ZM_SSO_RENEW_SESSION = "sso.renewSession";

    private static final String ZM_SSO_LOCAL_LOGOUT = "sso.localLogout";
    private static final String ZM_SSO_DESTROY_SESSION = "sso.destroySession";
    private static final String ZM_SSO_CENTRAL_LOGOUT = "sso.centralLogout";

    private static final String ZM_SSO_SAML_AUTHN_REQUEST_SIGNED = "saml.authnRequestSigned";
    private static final String ZM_SSO_SAML_SP_LOGOUT_REQUEST_SIGNED = "saml.spLogoutRequestSigned";


    private static final Map<String, String> properties = new HashMap<>();
    private static Client<Credentials> defaultClient;

    private static final Boolean saveInSession;
    private static final Boolean multiProfile;
    private static final Boolean renewSession;

    private static final Boolean localLogout;
    private static final Boolean destroySession;
    private static final Boolean centralLogout;

    static {
        loadProperties();
        saveInSession = loadBooleanProperty(ZM_SSO_SAVE_IN_SESSION);
        multiProfile = loadBooleanProperty(ZM_SSO_MULTI_PROFILE);
        renewSession = loadBooleanProperty(ZM_SSO_RENEW_SESSION);

        localLogout = loadBooleanProperty(ZM_SSO_LOCAL_LOGOUT);
        destroySession = loadBooleanProperty(ZM_SSO_DESTROY_SESSION);
        centralLogout = loadBooleanProperty(ZM_SSO_CENTRAL_LOGOUT);
    }

    public static Config buildConfig() throws ExtensionException {
        final LogoutHandler<WebContext> logoutHandler = new ZmLogoutHandler<>();
        final PropertiesConfigFactory factory = new PropertiesConfigFactory(loadStringProperty(ZM_SSO_CALLBACK_URL), properties);
        final Config config =  factory.build();

        config.getClients().findClient(CasClient.class).ifPresent(client -> {
            CasConfiguration cfg = client.getConfiguration();
            cfg.setLogoutHandler(logoutHandler);
        });
        config.getClients().findClient(OidcClient.class).ifPresent(client -> {
            OidcConfiguration cfg = client.getConfiguration();
            cfg.setLogoutHandler(logoutHandler);
            cfg.setPreferredJwsAlgorithm(JWSAlgorithm.RS256);
        });
        config.getClients().findClient(SAML2Client.class).ifPresent(client -> {
            SAML2Configuration cfg = client.getConfiguration();
            cfg.setLogoutHandler(logoutHandler);
            cfg.setAuthnRequestSigned(loadBooleanProperty(ZM_SSO_SAML_AUTHN_REQUEST_SIGNED));
            cfg.setSpLogoutRequestSigned(loadBooleanProperty(ZM_SSO_SAML_SP_LOGOUT_REQUEST_SIGNED));
        });

        defaultClient = config.getClients().findClient(loadStringProperty(ZM_SSO_DEFAULT_CLIENT)).orElseThrow(() -> new ExtensionException("No pac4j client found"));

        return config;
    }

    public static Client<Credentials> defaultClient() {
        return defaultClient;
    }

    public static boolean saveInSession() {
        return saveInSession;
    }

    public static boolean multiProfile() {
        return multiProfile;
    }

    public static boolean renewSession() {
        return renewSession;
    }

    public static boolean localLogout() {
        return localLogout;
    }

    public static boolean destroySession() {
        return destroySession;
    }

    public static boolean centralLogout() {
        return centralLogout;
    }

    private static void loadProperties() {
        final ClassLoader classLoader = SettingsBuilder.class.getClassLoader();
        final Properties prop = new Properties();
        final InputStream inputStream = classLoader.getResourceAsStream(ZM_SSO_SETTINGS_FILE);
        try {
            prop.load(inputStream);
            for (String key: prop.stringPropertyNames()) {
                properties.put(key, prop.getProperty(key));
            }
        } catch (IOException e) {
            ZimbraLog.extensions.error(e);
        }
    }

    private static String loadStringProperty(final String key) {
        return properties.get(key);
    }

    private static Boolean loadBooleanProperty(final String key) {
        final String value = properties.get(key);
        if (!StringUtil.isNullOrEmpty(value)) {
            return Boolean.parseBoolean((value).trim());
        }
        return false;
    }
}
