package com.iwayvietnam.zmsso.oidc;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.oidc.profile.OidcProfile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OidcLogoutHandler extends OidcBaseHandler {

    public OidcLogoutHandler() throws ExtensionException {
        super();
    }

    @Override
    public String getPath() {
        return OidcSsoConstants.LOGOUT_HANDLER_PATH;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final JEEContext context = new JEEContext(request, response);
        ProfileManager<OidcProfile> manager = new ProfileManager<>(context);
        manager.setConfig(pac4jConfig);

        OidcProfile profile = manager.get(true).get();
        try {
            clearAuthToken(request, response, profile.getIdTokenString());
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
