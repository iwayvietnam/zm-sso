package com.iwayvietnam.zmsso.saml;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.saml.credentials.SAML2Credentials;
import org.pac4j.saml.profile.SAML2Profile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class SamlCallbackHandler extends SamlBaseHandler {

    public SamlCallbackHandler() throws ExtensionException {
        super();
    }

    @Override
    public String getPath() {
        return SamlSsoConstants.CALLBACK_HANDLER_PATH;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final JEEContext context = new JEEContext(request, response);
        Optional<SAML2Credentials> credentials = client.getCredentials(context);
        Optional<UserProfile> profile = client.getUserProfile(credentials.orElse(null), context);
        ProfileManager<SAML2Profile> manager = new ProfileManager<>(context);

        if (profile.isPresent()) {
            manager.save(true, (SAML2Profile) profile.get(), true);
            try {
                String token = credentials.get().getSessionIndex();
                singleLogin(request, response, profile.get().getUsername(), token, SSOProtocol.ZM_SSO_CAS);
            } catch (ServiceException e) {
                throw new ServletException(e);
            }
        }
    }
}
