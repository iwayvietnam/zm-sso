package com.iwayvietnam.zmsso.cas;

import com.iwayvietnam.zmsso.BaseSsoHandler;
import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.cas.client.CasClient;

public class CasBaseHandler  extends BaseSsoHandler {
    protected final CasClient client;

    public CasBaseHandler () throws ExtensionException {
        super();
        client = pac4jConfig.getClients().findClient(CasClient.class).orElseThrow(() -> new ExtensionException("No client found"));
    }
}
