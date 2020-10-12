package com.iwayvietnam.zmsso.cas;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class CasCallbackHandler extends CasBaseHandler {
    public CasCallbackHandler() throws ExtensionException {
        super();
    }


    @Override
    public String getPath() {
        return CasSsoConstants.CALLBACK_HANDLER_PATH;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        final JEEContext context = new JEEContext(request, response);
        Optional<TokenCredentials> credentials = client.getCredentials(context);
        final Optional<UserProfile> profile = client.getUserProfile(credentials.orElse(null), context);
        ProfileManager<CasProfile> manager = new ProfileManager<>(context);
        if (profile.isPresent()) {
            manager.save(true, (CasProfile) profile.get(), true);
            try {
                String token = credentials.get().getToken();
                singleLogin(request, response, profile.get().getUsername(), token, SSOProtocol.ZM_SSO_CAS);
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
