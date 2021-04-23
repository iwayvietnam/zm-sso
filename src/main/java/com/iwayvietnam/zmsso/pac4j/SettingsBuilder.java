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

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import org.pac4j.cas.client.CasClient;
import org.pac4j.config.client.PropertiesConfigFactory;
import org.pac4j.config.client.PropertiesConstants;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.saml.client.SAML2Client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

/**
 * Pac4j Settings Builder
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public final class SettingsBuilder {
    private static final Map<String, String> properties = new HashMap<>();
    private static final Config config;

    private static final Boolean renewSession;
    private static final Boolean localLogout;
    private static final Boolean destroySession;
    private static final Boolean centralLogout;

    static {
        loadSettingsFromProperties();
        loadSettingsFromLocalConfig();

        config = buildConfig();

        renewSession = loadBooleanProperty(SettingsConstants.ZM_SSO_RENEW_SESSION);
        localLogout = loadBooleanProperty(SettingsConstants.ZM_SSO_LOCAL_LOGOUT);
        destroySession = loadBooleanProperty(SettingsConstants.ZM_SSO_DESTROY_SESSION);
        centralLogout = loadBooleanProperty(SettingsConstants.ZM_SSO_CENTRAL_LOGOUT);
    }

    public static Config getConfig() {
        return config;
    }

    public static Client defaultClient() throws ServiceException {
        return config.getClients()
             .findClient(loadStringProperty(SettingsConstants.ZM_SSO_DEFAULT_CLIENT))
             .orElseThrow(() -> ServiceException.NOT_FOUND("No default client found"));
    }

    public static Boolean renewSession() {
        return renewSession;
    }

    public static Boolean localLogout() {
        return localLogout;
    }

    public static Boolean destroySession() {
        return destroySession;
    }

    public static Boolean centralLogout() {
        return centralLogout;
    }

    private static Config buildConfig() {
        ZimbraLog.extensions.debug("Build Pac4J config");
        final var logoutHandler = new ZmLogoutHandler();
        final var factory = new PropertiesConfigFactory(loadStringProperty(SettingsConstants.ZM_SSO_CALLBACK_URL), properties);
        final var config = factory.build();

        config.getClients().findClient(CasClient.class).ifPresent(client -> {
            ZimbraLog.extensions.debug("Config cas client");
            final var cfg = client.getConfiguration();
            cfg.setLogoutHandler(logoutHandler);
        });
        config.getClients().findClient(OidcClient.class).ifPresent(client -> {
            ZimbraLog.extensions.debug("Config oidc client");
            final var cfg = client.getConfiguration();
            cfg.setLogoutHandler(logoutHandler);
            cfg.setWithState(loadBooleanProperty(SettingsConstants.ZM_OIDC_WITH_STATE));
        });
        config.getClients().findClient(SAML2Client.class).ifPresent(client -> {
            ZimbraLog.extensions.debug("Config saml client");
            client.setRedirectionActionBuilder(new ZmSAML2RedirectionActionBuilder(client));

            final var cfg = client.getConfiguration();
            cfg.setLogoutHandler(logoutHandler);
            cfg.setAuthnRequestSigned(loadBooleanProperty(SettingsConstants.ZM_SAML_AUTHN_REQUEST_SIGNED));
            cfg.setSpLogoutRequestSigned(loadBooleanProperty(SettingsConstants.ZM_SAML_SP_LOGOUT_REQUEST_SIGNED));
            cfg.setForceServiceProviderMetadataGeneration(loadBooleanProperty(SettingsConstants.ZM_SAML_SP_METADATA_GENERATION));
            cfg.setForceKeystoreGeneration(loadBooleanProperty(SettingsConstants.ZM_SAML_SP_KEYSTORE_GENERATION));
        });
        return config;
    }

    private static void loadSettingsFromProperties() {
        final var confDir = Paths.get(LC.zimbra_home.value(), "conf").toString();
        final var prop = new Properties();
        try {
            ZimbraLog.extensions.debug("Load config properties");
            final InputStream inputStream = new FileInputStream(confDir + "/" + SettingsConstants.ZM_SSO_SETTINGS_FILE);
            prop.load(inputStream);
            prop.stringPropertyNames().forEach(key -> properties.put(key, prop.getProperty(key)));
        } catch (IOException e) {
            ZimbraLog.extensions.error(e);
        }
    }

    private static void loadSettingsFromLocalConfig() {
        final var fields = Arrays.asList(SettingsConstants.class.getDeclaredFields());
        fields.addAll(Arrays.asList(PropertiesConstants.class.getDeclaredFields()));
        fields.forEach(field -> {
            try {
                final var key = field.get(null).toString();
                final var value = LC.get(key);
                if (!StringUtil.isNullOrEmpty(value)) {
                    properties.put(key, value);
                }
            } catch (IllegalAccessException e) {
                ZimbraLog.extensions.error(e);
            }
        });
    }

    private static String loadStringProperty(final String key) {
        return properties.get(key);
    }

    private static Boolean loadBooleanProperty(final String key) {
        final var value = properties.get(key);
        if (!StringUtil.isNullOrEmpty(value)) {
            return Boolean.parseBoolean((value).trim());
        }
        return false;
    }
}
