package com.iwayvietnam.zmsso.saml;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.oidc.profile.OidcProfile;
import org.pac4j.saml.profile.SAML2Profile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SamlLogoutHandler extends SamlBaseHandler {

    public SamlLogoutHandler() throws ExtensionException {
        super();
    }

    @Override
    public String getPath() {
        return SamlSsoConstants.LOGOUT_HANDLER_PATH;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final JEEContext context = new JEEContext(request, response);
        ProfileManager<SAML2Profile> manager = new ProfileManager<>(context);
        manager.setConfig(pac4jConfig);

        SAML2Profile profile = manager.get(true).get();
        try {
            clearAuthToken(request, response, profile.getSessionIndex());
            manager.logout();
        } catch (AuthTokenException | ServiceException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }
}
