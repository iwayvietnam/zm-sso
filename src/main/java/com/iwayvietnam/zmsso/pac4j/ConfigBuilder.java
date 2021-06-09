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
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.saml.client.SAML2Client;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Config builder
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public class ConfigBuilder {
    private static ConfigBuilder instance;

    private static final Map<String, String> properties = new HashMap<>();
    private final ConfigFactory configFactory;
    private final Config config;
    private final ZmLogoutHandler<? extends WebContext> logoutHandler;

    private final Boolean saveInSession;
    private final Boolean multiProfile;
    private final Boolean renewSession;

    private final Boolean localLogout;
    private final Boolean destroySession;
    private final Boolean centralLogout;
    private final String postLogoutURL;

    private ConfigBuilder() {
        loadSettingsFromProperties();
        configFactory = new PropertiesConfigFactory(loadStringProperty(SettingsConstants.ZM_SSO_CALLBACK_URL), properties);
        logoutHandler = new ZmLogoutHandler<>();
        config = buildConfig();

        saveInSession = loadBooleanProperty(SettingsConstants.ZM_SSO_SAVE_IN_SESSION);
        multiProfile = loadBooleanProperty(SettingsConstants.ZM_SSO_MULTI_PROFILE);
        renewSession = loadBooleanProperty(SettingsConstants.ZM_SSO_RENEW_SESSION);

        localLogout = loadBooleanProperty(SettingsConstants.ZM_SSO_LOCAL_LOGOUT);
        destroySession = loadBooleanProperty(SettingsConstants.ZM_SSO_DESTROY_SESSION);
        centralLogout = loadBooleanProperty(SettingsConstants.ZM_SSO_CENTRAL_LOGOUT);
        postLogoutURL = loadStringProperty(SettingsConstants.ZM_SSO_POST_LOGOUT_URL);
    }

    public static ConfigBuilder getInstance() {
        if (instance == null) {
            synchronized (ConfigBuilder.class) {
                if (instance == null) {
                    instance = new ConfigBuilder();
                }
            }
        }
        return instance;
    }

    public Config getConfig() {
        return config;
    }

    public Clients getClients() {
        return config.getClients();
    }

    public Client defaultClient() throws ServiceException {
        return config.getClients().findClient(loadStringProperty(SettingsConstants.ZM_SSO_DEFAULT_CLIENT)).orElseThrow(() -> ServiceException.NOT_FOUND("No default client found"));
    }

    public ZmLogoutHandler getLogoutHandler() {
        return logoutHandler;
    }

    public Boolean getSaveInSession() {
        return saveInSession;
    }

    public Boolean getMultiProfile() {
        return multiProfile;
    }

    public Boolean getRenewSession() {
        return renewSession;
    }

    public Boolean getLocalLogout() {
        return localLogout;
    }

    public Boolean getDestroySession() {
        return destroySession;
    }

    public Boolean getCentralLogout() {
        return centralLogout;
    }

    public String getPostLogoutURL() {
        return postLogoutURL;
    }

    private Config buildConfig() {
        ZimbraLog.extensions.info("Build Pac4J config");
        final var config = configFactory.build();

        config.getClients().findClient(CasClient.class).ifPresent(client -> {
            ZimbraLog.extensions.info("Config cas client");
            final var cfg = client.getConfiguration();
            cfg.setLogoutHandler(logoutHandler);

            Optional.ofNullable(loadStringProperty(SettingsConstants.ZM_CAS_CALLBACK_URL)).ifPresent(client::setCallbackUrl);
            final var serverLogoutUrl = cfg.computeFinalPrefixUrl(null) + "logout";
            client.setLogoutActionBuilder(new ZmCasLogoutActionBuilder(serverLogoutUrl, cfg.getPostLogoutUrlParameter(), getPostLogoutURL()));
        });
        config.getClients().findClient(OidcClient.class).ifPresent(client -> {
            ZimbraLog.extensions.info("Config oidc client");
            final var cfg = client.getConfiguration();
            cfg.setLogoutHandler(logoutHandler);
            cfg.setWithState(loadBooleanProperty(SettingsConstants.ZM_OIDC_WITH_STATE));
            if (StringUtil.isNullOrEmpty(loadStringProperty(SettingsConstants.ZM_OIDC_SCOPE))) {
                cfg.setScope(SettingsConstants.ZM_DEFAULT_OIDC_SCOPE);
            }

            Optional.ofNullable(loadStringProperty(SettingsConstants.ZM_OIDC_CALLBACK_URL)).ifPresent(client::setCallbackUrl);
            client.setLogoutActionBuilder(new ZmOidcLogoutActionBuilder(client.getConfiguration(), getPostLogoutURL()));
        });
        config.getClients().findClient(SAML2Client.class).ifPresent(client -> {
            ZimbraLog.extensions.info("Config saml client");
            final var cfg = client.getConfiguration();
            cfg.setLogoutHandler(logoutHandler);
            cfg.setForceServiceProviderMetadataGeneration(true);
            cfg.setForceKeystoreGeneration(false);
            cfg.setAuthnRequestSigned(loadBooleanProperty(SettingsConstants.ZM_SAML_AUTHN_REQUEST_SIGNED));
            cfg.setSpLogoutRequestSigned(loadBooleanProperty(SettingsConstants.ZM_SAML_LOGOUT_REQUEST_SIGNED));
            cfg.setWantsAssertionsSigned(loadBooleanProperty(SettingsConstants.ZM_SAML_WANTS_ASSERTIONS_SIGNED));
            cfg.setWantsResponsesSigned(loadBooleanProperty(SettingsConstants.ZM_SAML_WANTS_RESPONSES_SIGNED));
            cfg.setAllSignatureValidationDisabled(loadBooleanProperty(SettingsConstants.ZM_SAML_ALL_SIGNATURE_VALIDATION_DISABLED));
            cfg.setForceAuth(loadBooleanProperty(SettingsConstants.ZM_SAML_FORCE_AUTH));

            Optional.ofNullable(loadStringProperty(SettingsConstants.ZM_SAML_RESPONSE_BINDING)).ifPresent(cfg::setResponseBindingType);
            Optional.ofNullable(loadStringProperty(SettingsConstants.ZM_SAML_LOGOUT_REQUEST_BINDING)).ifPresent(cfg::setSpLogoutRequestBindingType);
            Optional.ofNullable(loadStringProperty(SettingsConstants.ZM_SAML_LOGOUT_RESPONSE_BINDING)).ifPresent(cfg::setSpLogoutResponseBindingType);

            final var postLogoutURL = Optional.ofNullable(getPostLogoutURL()).orElse(Pac4jConstants.DEFAULT_URL_VALUE);
            cfg.setPostLogoutURL(postLogoutURL);

            Optional.ofNullable(loadStringProperty(SettingsConstants.ZM_SAML_CALLBACK_URL)).ifPresent(client::setCallbackUrl);
        });
        return config;
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

    private static void loadSettingsFromProperties() {
        ZimbraLog.extensions.info("Load config properties");
        try {
            final var confDir = Paths.get(LC.zimbra_home.value(), "conf").toString();
            final var prop = new Properties();
            prop.load(new FileInputStream(confDir + "/" + SettingsConstants.ZM_SSO_SETTINGS_FILE));
            prop.stringPropertyNames().forEach(key -> properties.put(key, prop.getProperty(key)));
        } catch (IOException e) {
            ZimbraLog.extensions.error(e);
        }
    }
}
