package com.iwayvietnam.zmsso.pac4j;

import com.zimbra.common.util.ZimbraLog;
import org.pac4j.core.context.WebContext;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.credentials.SAML2Credentials;
import org.pac4j.saml.credentials.extractor.SAML2CredentialsExtractor;

import java.util.Optional;

public class ZmSAML2CredentialsExtractor extends SAML2CredentialsExtractor {

    public ZmSAML2CredentialsExtractor(SAML2Client client) {
        super(client);
    }

    @Override
    public Optional<SAML2Credentials> extract(WebContext context) {
        final var thread = Thread.currentThread();
        final var origCl = thread.getContextClassLoader();
        thread.setContextClassLoader(getClass().getClassLoader());

        Optional<SAML2Credentials> credentials;
        try {
            ZimbraLog.extensions.debug("Extract credentials");
            credentials = super.extract(context);
        }
        catch (NullPointerException npe) {
            throw new RuntimeException(npe);
        }
        finally {
            thread.setContextClassLoader(origCl);
        }

        return credentials;
    }
}
