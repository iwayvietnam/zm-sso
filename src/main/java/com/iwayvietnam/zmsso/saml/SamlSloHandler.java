package com.iwayvietnam.zmsso.saml;

import com.zimbra.cs.extension.ExtensionException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.util.HttpActionHelper;
import org.pac4j.saml.logout.impl.SAML2LogoutResponseBuilder;
import org.pac4j.saml.logout.impl.SAML2LogoutResponseMessageSender;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        final var context = new JEEContext(request, response);
        client.init();
        final var contextProvider = client.getContextProvider();
        final var logoutProfileHandler = client.getLogoutProfileHandler();
        final var spLogoutResponseBindingType = client.getConfiguration().getSpLogoutResponseBindingType();
        final var saml2LogoutResponseBuilder = new SAML2LogoutResponseBuilder(spLogoutResponseBindingType);
        final var saml2LogoutResponseMessageSender = new SAML2LogoutResponseMessageSender(client.getSignatureSigningParametersProvider(),
                spLogoutResponseBindingType, false, client.getConfiguration().isSpLogoutRequestSigned());

        final var samlContext = contextProvider.buildContext(client, context, JEESessionStore.INSTANCE);
        samlContext.setSaml2Configuration(client.getConfiguration());
        logoutProfileHandler.receive(samlContext);
        final var logoutResponse = saml2LogoutResponseBuilder.build(samlContext);
        saml2LogoutResponseMessageSender.sendMessage(samlContext, logoutResponse,
                samlContext.getSAMLBindingContext().getRelayState());

        final var adapter = samlContext.getProfileRequestContextOutboundMessageTransportResponse();
        if (spLogoutResponseBindingType.equalsIgnoreCase(SAMLConstants.SAML2_POST_BINDING_URI)) {
            throw HttpActionHelper.buildFormPostContentAction(context, adapter.getOutgoingContent());
        } else {
            throw HttpActionHelper.buildRedirectUrlAction(context, adapter.getRedirectUrl());
        }
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }
}
