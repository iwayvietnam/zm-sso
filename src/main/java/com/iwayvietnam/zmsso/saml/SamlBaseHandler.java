package com.iwayvietnam.zmsso.saml;

import com.iwayvietnam.zmsso.BaseSsoHandler;
import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.saml.client.SAML2Client;

public class SamlBaseHandler  extends BaseSsoHandler {
    protected final SAML2Client client;

    public SamlBaseHandler() throws ExtensionException {
        super();
        client = pac4jConfig.getClients().findClient(SAML2Client.class).orElseThrow(() -> new ExtensionException("No client found"));
    }
}
