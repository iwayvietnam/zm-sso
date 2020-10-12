package com.iwayvietnam.zmsso.oidc;

import com.iwayvietnam.zmsso.BaseSsoHandler;
import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.oidc.client.OidcClient;

public class OidcBaseHandler  extends BaseSsoHandler {
    protected OidcClient client;

    public OidcBaseHandler () throws ExtensionException {
        super();
        client = pac4jConfig.getClients().findClient(OidcClient.class).orElseThrow(() -> new ExtensionException("No client found"));
    }
}
