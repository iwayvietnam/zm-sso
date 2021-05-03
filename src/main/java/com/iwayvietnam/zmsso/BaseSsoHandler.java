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

import com.iwayvietnam.zmsso.pac4j.SettingsConstants;
import com.iwayvietnam.zmsso.pac4j.ZmLogoutHandler;
import com.iwayvietnam.zmsso.pac4j.ZmSAML2RedirectionActionBuilder;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.*;
import com.zimbra.cs.extension.ExtensionHttpHandler;
import com.zimbra.cs.extension.ZimbraExtension;
import com.zimbra.cs.httpclient.URLUtil;

import com.zimbra.cs.servlet.util.AuthUtil;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.xmlsec.config.DecryptionParserPool;
import org.pac4j.cas.client.CasClient;
import org.pac4j.config.client.PropertiesConfigFactory;
import org.pac4j.config.client.PropertiesConstants;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.JEEContextFactory;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.saml.client.SAML2Client;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

/**
 * Base Sso Handler
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public abstract class BaseSsoHandler extends ExtensionHttpHandler {
    protected static final String SSO_CLIENT_NAME_SESSION_ATTR = "sso.ClientName";
    protected static final Map<String, String> properties = new HashMap<>();

    protected Config config;

    @Override
    public void init(ZimbraExtension ext) throws ServiceException {
        super.init(ext);
        loadSettingsFromProperties();
        loadSettingsFromLocalConfig();
        if (hasSamlClient()) {
            openSAMLInitialization();
        }
        config = buildConfig();
    }

    protected void doLogin(final HttpServletRequest request, final HttpServletResponse response, final Client client) throws IOException, ServiceException {
        final var authToken = AuthUtil.getAuthTokenFromHttpReq(request, false);
        if (!isLoggedIn(authToken)) {
            ZimbraLog.extensions.debug(String.format("SSO login with: %s", client.getName()));
            request.getSession().setAttribute(SSO_CLIENT_NAME_SESSION_ATTR, client.getName());
            final var context = JEEContextFactory.INSTANCE.newContext(request, response);
            client.getRedirectionAction(context, JEESessionStore.INSTANCE).ifPresent(action -> JEEHttpActionAdapter.INSTANCE.adapt(action, context));
        } else {
            redirectByAuthToken(request, response, authToken);
        }
    }

    protected void doCallback(final HttpServletRequest request, final HttpServletResponse response, final Client client) {
        final var defaultUrl = Pac4jConstants.DEFAULT_URL_VALUE;
        final var renewSession = loadBooleanProperty(SettingsConstants.ZM_SSO_RENEW_SESSION);
        final var context = JEEContextFactory.INSTANCE.newContext(request, response);
        DefaultCallbackLogic.INSTANCE.perform(context, JEESessionStore.INSTANCE, config, JEEHttpActionAdapter.INSTANCE, defaultUrl, renewSession, client.getName());
    }

    private boolean isLoggedIn(final AuthToken authToken) {
        if (authToken != null) {
            return !authToken.isExpired() && authToken.isRegistered();
        }
        return false;
    }

    private void redirectByAuthToken(final HttpServletRequest request, final HttpServletResponse response, final AuthToken authToken) throws IOException, ServiceException {
        final var isAdmin = AuthToken.isAnyAdmin(authToken);
        final var server = authToken.getAccount().getServer();
        final var redirectUrl = AuthUtil.getRedirectURL(request, server, isAdmin, true) + AuthUtil.IGNORE_LOGIN_URL;

        final var url = new URL(redirectUrl);
        final var isRedirectProtocolSecure = isProtocolSecure(url.getProtocol());
        final var secureCookie = isProtocolSecure(request.getScheme());

        if (secureCookie && !isRedirectProtocolSecure) {
            throw ServiceException.INVALID_REQUEST(String.format("Cannot redirect to non-secure protocol: %s", redirectUrl), null);
        }

        ZimbraLog.extensions.debug(String.format("SSO login - redirecting (with auth token) to: %s", redirectUrl));
        response.sendRedirect(redirectUrl);
    }

    private boolean isProtocolSecure(final String protocol) {
        return URLUtil.PROTO_HTTPS.equalsIgnoreCase(protocol);
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

    private static void openSAMLInitialization() throws ServiceException {
        ZimbraLog.extensions.debug("OpenSAML Initialization and Configuration");
        XMLObjectProviderRegistry registry;
        synchronized (ConfigurationService.class) {
            registry = ConfigurationService.get(XMLObjectProviderRegistry.class);
            if (registry == null) {
                registry = new XMLObjectProviderRegistry();
                ConfigurationService.register(XMLObjectProviderRegistry.class, registry);
            }
        }

        final Thread thread = Thread.currentThread();
        final ClassLoader origCl = thread.getContextClassLoader();
        thread.setContextClassLoader(BaseSsoHandler.class.getClassLoader());

        try {
            InitializationService.initialize();
        } catch (final InitializationException e) {
            throw ServiceException.FAILURE("Exception initializing OpenSAM", e);
        } finally {
            thread.setContextClassLoader(origCl);
        }

        try {
            ZimbraLog.extensions.debug("Initializing parserPool");
            final var parserPool = new BasicParserPool();
            parserPool.setMaxPoolSize(100);
            parserPool.setCoalescing(true);
            parserPool.setIgnoreComments(true);
            parserPool.setNamespaceAware(true);
            parserPool.setExpandEntityReferences(false);
            parserPool.setXincludeAware(false);
            parserPool.setIgnoreElementContentWhitespace(true);

            final Map<String, Object> builderAttributes = new HashMap<>();
            parserPool.setBuilderAttributes(builderAttributes);

            final Map<String, Boolean> features = new HashMap<>();
            features.put("http://apache.org/xml/features/disallow-doctype-decl", Boolean.TRUE);
            features.put("http://apache.org/xml/features/dom/defer-node-expansion", Boolean.FALSE);
            features.put("http://apache.org/xml/features/validation/schema/normalized-value", Boolean.FALSE);
            features.put("http://javax.xml.XMLConstants/feature/secure-processing", Boolean.TRUE);
            features.put("http://xml.org/sax/features/external-general-entities", Boolean.FALSE);
            features.put("http://xml.org/sax/features/external-parameter-entities", Boolean.FALSE);

            parserPool.setBuilderFeatures(features);
            parserPool.initialize();
            registry.setParserPool(parserPool);

            ConfigurationService.register(DecryptionParserPool.class, new DecryptionParserPool(parserPool));
        } catch (final ComponentInitializationException e) {
            throw ServiceException.FAILURE("Exception initializing parserPool", e);
        }
    }

    private static void loadSettingsFromProperties() {
        try {
            final var confDir = Paths.get(LC.zimbra_home.value(), "conf").toString();
            ZimbraLog.extensions.debug(String.format("Load config properties: %s/%s", confDir, SettingsConstants.ZM_SSO_SETTINGS_FILE));
            final var props = new Properties();
            props.load(new FileInputStream(confDir + "/" + SettingsConstants.ZM_SSO_SETTINGS_FILE));
            props.stringPropertyNames().forEach((key) -> properties.put(key, props.getProperty(key)));
        }
        catch (IOException e) {
            ZimbraLog.extensions.error(e);
        }
    }

    private static void loadSettingsFromLocalConfig() {
        ZimbraLog.extensions.debug("Load settings from local config");
        Arrays.asList(SettingsConstants.class.getDeclaredFields()).forEach((field) -> {
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
        Arrays.asList(PropertiesConstants.class.getDeclaredFields()).forEach((field) -> {
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

    protected static String loadStringProperty(final String key) {
        return properties.get(key);
    }

    protected static Boolean loadBooleanProperty(final String key) {
        final var value = properties.get(key);
        if (!StringUtil.isNullOrEmpty(value)) {
            return Boolean.parseBoolean((value).trim());
        }
        return false;
    }

    protected Client defaultClient() throws ServiceException {
        return config.getClients()
                .findClient(loadStringProperty(SettingsConstants.ZM_SSO_DEFAULT_CLIENT))
                .orElseThrow(() -> ServiceException.NOT_FOUND("No default client found"));
    }

    private static boolean hasSamlClient() {
        return !StringUtil.isNullOrEmpty(loadStringProperty(PropertiesConstants.SAML_KEYSTORE_PASSWORD)) &&
                !StringUtil.isNullOrEmpty(loadStringProperty(PropertiesConstants.SAML_PRIVATE_KEY_PASSWORD)) &&
                !StringUtil.isNullOrEmpty(loadStringProperty(PropertiesConstants.SAML_KEYSTORE_PATH)) &&
                !StringUtil.isNullOrEmpty(loadStringProperty(PropertiesConstants.SAML_IDENTITY_PROVIDER_METADATA_PATH));
    }
}
