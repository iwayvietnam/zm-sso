package com.iwayvietnam.zmsso.oidc;

import com.iwayvietnam.zmsso.BaseSsoHandler;
import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.oidc.client.OidcClient;

public abstract class OidcBaseHandler extends BaseSsoHandler {
    protected final OidcClient client;

    public OidcBaseHandler() throws ExtensionException {
        super();
        client = config.getClients().findClient(OidcClient.class).orElseThrow(() -> new ExtensionException("No oidc client found"));
    }
}
