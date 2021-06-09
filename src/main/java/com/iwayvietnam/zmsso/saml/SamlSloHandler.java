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
package com.iwayvietnam.zmsso.saml;

import com.zimbra.cs.extension.ExtensionException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.exception.http.RedirectionActionHelper;
import org.pac4j.saml.logout.impl.SAML2LogoutResponseBuilder;
import org.pac4j.saml.logout.impl.SAML2LogoutResponseMessageSender;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Saml Slo Handler
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public class SamlSloHandler extends SamlBaseHandler {
    public static final String HANDLER_PATH = "/saml/slo";

    public SamlSloHandler() throws ExtensionException {
        super();
    }

    @Override
    public String getPath() {
        return HANDLER_PATH;
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        client.init();
        final var context = new JEEContext(request, response);
        final var contextProvider = client.getContextProvider();
        final var logoutProfileHandler = client.getLogoutProfileHandler();
        final var spLogoutResponseBindingType = client.getConfiguration().getSpLogoutResponseBindingType();
        final var saml2LogoutResponseBuilder = new SAML2LogoutResponseBuilder(spLogoutResponseBindingType);
        final var saml2LogoutResponseMessageSender = new SAML2LogoutResponseMessageSender(client.getSignatureSigningParametersProvider(),
                spLogoutResponseBindingType, false, client.getConfiguration().isSpLogoutRequestSigned());

        final var samlContext = contextProvider.buildContext(context);
        logoutProfileHandler.receive(samlContext);
        final var logoutResponse = saml2LogoutResponseBuilder.build(samlContext);
        saml2LogoutResponseMessageSender.sendMessage(samlContext, logoutResponse, samlContext.getSAMLBindingContext().getRelayState());

        final var adapter = samlContext.getProfileRequestContextOutboundMessageTransportResponse();
        if (spLogoutResponseBindingType.equalsIgnoreCase(SAMLConstants.SAML2_POST_BINDING_URI)) {
            throw RedirectionActionHelper.buildFormPostContentAction(context, adapter.getOutgoingContent());
        } else {
            throw RedirectionActionHelper.buildRedirectUrlAction(context, adapter.getRedirectUrl());
        }
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }
}
