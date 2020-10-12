package com.iwayvietnam.zmsso.oidc;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.oidc.profile.OidcProfile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class OidcCallbackHandler extends OidcBaseHandler {

    public OidcCallbackHandler() throws ExtensionException {
        super();
    }

    @Override
    public String getPath() {
        return OidcSsoConstants.CALLBACK_HANDLER_PATH;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final JEEContext context = new JEEContext(request, response);
        final Optional<Credentials> credentials = client.getCredentials(context);
        final Optional<OidcProfile> profile = client.getUserProfile(credentials.orElse(null), context);
        ProfileManager<OidcProfile> manager = new ProfileManager<>(context);
        if (profile.isPresent()) {
            manager.save(true, profile.get(), true);
            try {
                singleLogin(request, response, profile.get().getUsername(), profile.get().getIdTokenString(), SSOProtocol.ZM_SSO_OIDC);
            } catch (ServiceException e) {
                throw new ServletException(e);
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }
}
