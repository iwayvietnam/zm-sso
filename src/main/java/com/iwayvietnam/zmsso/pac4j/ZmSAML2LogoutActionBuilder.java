package com.iwayvietnam.zmsso.pac4j;

import com.zimbra.common.util.ZimbraLog;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.logout.SAML2LogoutActionBuilder;

import java.util.Optional;

public class ZmSAML2LogoutActionBuilder extends SAML2LogoutActionBuilder {

    public ZmSAML2LogoutActionBuilder(SAML2Client client) {
        super(client);
    }
    @Override
    public Optional<RedirectionAction> getLogoutAction(WebContext context, UserProfile currentProfile, String targetUrl) {
        final var thread = Thread.currentThread();
        final var origCl = thread.getContextClassLoader();
        thread.setContextClassLoader(getClass().getClassLoader());

        Optional<RedirectionAction> action = Optional.empty();

        try {
            action = super.getLogoutAction(context, currentProfile, targetUrl);
        } catch (final RedirectionAction e) {
            ZimbraLog.extensions.error(e);
        } finally {
            thread.setContextClassLoader(origCl);
        }

        return action;
    }
}
