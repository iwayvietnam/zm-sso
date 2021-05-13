package com.iwayvietnam.zmsso.pac4j;

import com.zimbra.common.util.ZimbraLog;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.credentials.extractor.SAML2CredentialsExtractor;

import java.util.Optional;

public class ZmSAML2CredentialsExtractor extends SAML2CredentialsExtractor {

    public ZmSAML2CredentialsExtractor(final SAML2Client client) {
        super(client);
    }

    @Override
    public Optional<Credentials> extract(final WebContext context, final SessionStore sessionStore) {
        final var thread = Thread.currentThread();
        final var origCl = thread.getContextClassLoader();
        thread.setContextClassLoader(getClass().getClassLoader());

        ZimbraLog.extensions.debug("Extract saml credentials");
        final var credentials = super.extract(context, sessionStore);
        thread.setContextClassLoader(origCl);

        return credentials;
    }
}
