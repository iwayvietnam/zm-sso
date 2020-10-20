package com.iwayvietnam.zmsso.pac4j;

import com.zimbra.common.util.ZimbraLog;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.redirect.SAML2RedirectionActionBuilder;

import java.util.Optional;

public class ZmSAML2RedirectionActionBuilder extends SAML2RedirectionActionBuilder {

    public ZmSAML2RedirectionActionBuilder(SAML2Client client) {
        super(client);
    }

    @Override
    public Optional<RedirectionAction> getRedirectionAction(final WebContext wc) {
        final Thread thread = Thread.currentThread();
        final ClassLoader origCl = thread.getContextClassLoader();
        thread.setContextClassLoader(SettingsBuilder.class.getClassLoader());

        Optional<RedirectionAction> action = Optional.empty();
        try {
            action = super.getRedirectionAction(wc);
        } catch (final RedirectionAction e) {
            ZimbraLog.extensions.error(e);
        } finally {
            thread.setContextClassLoader(origCl);
        }
        return action;
    }
}