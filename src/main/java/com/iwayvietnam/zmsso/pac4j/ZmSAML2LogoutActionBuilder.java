package com.iwayvietnam.zmsso.pac4j;

import com.zimbra.common.util.ZimbraLog;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.logout.SAML2LogoutActionBuilder;

import java.util.Optional;

public class ZmSAML2LogoutActionBuilder extends SAML2LogoutActionBuilder {

    public ZmSAML2LogoutActionBuilder(final SAML2Client client) {
        super(client);
    }

    @Override
    public Optional<RedirectionAction> getLogoutAction(final WebContext context, final SessionStore sessionStore, final UserProfile currentProfile, final String targetUrl) {
        final var thread = Thread.currentThread();
        final var origCl = thread.getContextClassLoader();
        thread.setContextClassLoader(getClass().getClassLoader());

        ZimbraLog.extensions.debug("Get saml logout action");
        final var action = super.getLogoutAction(context, sessionStore, currentProfile, targetUrl);
        thread.setContextClassLoader(origCl);

        return action;
    }
}
