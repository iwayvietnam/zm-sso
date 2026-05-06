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

import com.iwayvietnam.zmsso.util.Log;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.AbstractExceptionAwareLogic;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.NoContentAction;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.HttpActionHelper;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.iwayvietnam.zmsso.pac4j.SettingsConstants.SSO_PROFILE_SESSION_ATTR;
import static org.pac4j.core.util.CommonHelper.*;
import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * Pac4j Logout Logic
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 * Logout url:  https://mail.zimbra-server.com/?loginOp=logout
 */
public class ZmLogoutLogic extends AbstractExceptionAwareLogic implements LogoutLogic {
    public static final ZmLogoutLogic INSTANCE = new ZmLogoutLogic();

    @Override
    public Object perform(final WebContext context, final SessionStore sessionStore, final Config config,
                          final HttpActionAdapter httpActionAdapter, final String defaultUrl, final String inputLogoutUrlPattern,
                          final Boolean inputLocalLogout, final Boolean inputDestroySession, final Boolean inputCentralLogout) {
        Log.sso.debug("Performing logic logout");
        HttpAction action;
        try {
            final var logoutUrlPattern = Objects.requireNonNullElse(inputLogoutUrlPattern, Pac4jConstants.DEFAULT_LOGOUT_URL_PATTERN_VALUE);
            final var localLogout = inputLocalLogout == null || inputLocalLogout;
            final var destroySession = inputDestroySession != null && inputDestroySession;
            final var centralLogout = inputCentralLogout != null && inputCentralLogout;

            // checks
            assertNotNull("context", context);
            assertNotNull("config", config);
            assertNotNull("httpActionAdapter", httpActionAdapter);
            assertNotBlank(Pac4jConstants.LOGOUT_URL_PATTERN, logoutUrlPattern);
            final var configClients = config.getClients();
            assertNotNull("configClients", configClients);

            // logic
            final var manager = getProfileManager(context, sessionStore);
            manager.setConfig(config);
            final var profiles = manager.getProfiles();
            if (profiles.isEmpty() && context instanceof JEEContext jeeCxt) {
                final var session = jeeCxt.getNativeRequest().getSession();
                var profile = (UserProfile) session.getAttribute(SSO_PROFILE_SESSION_ATTR);
                Optional.of(profile).ifPresent(profile1 -> {
                    profiles.add(profile);
                });
            }

            // compute redirection URL
            final var url = context.getRequestParameter(Pac4jConstants.URL);
            var redirectUrl = defaultUrl;
            if (url.isPresent() && Pattern.matches(logoutUrlPattern, url.get())) {
                redirectUrl = url.get();
            }
            Log.sso.debug("redirectUrl: %s", redirectUrl);
            if (redirectUrl != null) {
                action = HttpActionHelper.buildRedirectUrlAction(context, redirectUrl);
            } else {
                action = NoContentAction.INSTANCE;
            }

            // local logout if requested or multiple profiles
            if (localLogout || profiles.size() > 1) {
                Log.sso.debug("Performing application logout");
                manager.removeProfiles();
                if (destroySession) {
                    if (sessionStore != null) {
                        final var removed = sessionStore.destroySession(context);
                        if (!removed) {
                            Log.sso.error("Unable to destroy the web session. The session store may not support this feature");
                        }
                    } else {
                        Log.sso.error("No session store available for this web context");
                    }
                }
            }

            // central logout
            if (centralLogout) {
                Log.sso.debug("Performing central logout");
                for (final var profile : profiles) {
                    Log.sso.debug("Profile: %s", profile);
                    final var clientName = profile.getClientName();
                    if (clientName != null) {
                        final var client = configClients.findClient(clientName);
                        if (client.isPresent()) {
                            String targetUrl = null;
                            if (redirectUrl != null) {
                                redirectUrl = enhanceRedirectUrl(config, client.get(), context, sessionStore, redirectUrl);
                                if (redirectUrl.startsWith(HttpConstants.SCHEME_HTTP) ||
                                        redirectUrl.startsWith(HttpConstants.SCHEME_HTTPS)) {
                                    targetUrl = redirectUrl;
                                }
                            }
                            final var logoutAction =
                                    client.get().getLogoutAction(context, sessionStore, profile, targetUrl);
                            Log.sso.debug("Logout action: %s", logoutAction);
                            if (logoutAction.isPresent()) {
                                action = logoutAction.get();
                                break;
                            }
                        }
                    }
                }
            }
        } catch (final RuntimeException e) {
            return handleException(e, httpActionAdapter, context);
        }

        return httpActionAdapter.adapt(action, context);
    }

    protected String enhanceRedirectUrl(final Config config, final Client client, final WebContext context,
                                        final SessionStore sessionStore, final String redirectUrl) {
        return redirectUrl;
    }

    @Override
    public String toString() {
        return toNiceString(this.getClass(), "errorUrl", getErrorUrl());
    }
}
