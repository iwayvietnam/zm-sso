package com.iwayvietnam.zmsso.oidc;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.extension.ExtensionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OidcLoginHandler extends OidcBaseHandler {

    public OidcLoginHandler() throws ExtensionException {
        super();
    }

    @Override
    public String getPath() {
        return OidcSsoConstants.LOGIN_HANDLER_PATH;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            doLogin(request, response, client);
        } catch (ServiceException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }
}
