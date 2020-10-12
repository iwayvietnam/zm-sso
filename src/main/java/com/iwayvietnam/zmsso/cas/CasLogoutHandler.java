package com.iwayvietnam.zmsso.cas;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.profile.ProfileManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class CasLogoutHandler extends CasBaseHandler {
    public CasLogoutHandler() throws ExtensionException {
        super();
    }

    @Override
    public String getPath() {
        return CasSsoConstants.LOGOUT_HANDLER_PATH;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final JEEContext context = new JEEContext(request, response);
        ProfileManager<CasProfile> manager = new ProfileManager<>(context);
        final Optional<TokenCredentials> credentials = client.getCredentials(context);
        manager.setConfig(pac4jConfig);
        final List<CasProfile> profiles = manager.getAll(true);

        HttpAction action;
        try {
            clearAuthToken(request, response, credentials.get().getToken());
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
