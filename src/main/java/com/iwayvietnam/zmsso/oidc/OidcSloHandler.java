package com.iwayvietnam.zmsso.oidc;

import com.iwayvietnam.zmsso.BaseSsoHandler;
import com.zimbra.common.service.ServiceException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OidcSloHandler extends BaseSsoHandler {
    @Override
    public String getPath() {
        return OidcSsoConstants.SLO_HANDLER_PATH;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            String ssoToken = "";
            singleLogout(ssoToken);
        } catch (ServiceException e) {
            throw new ServletException(e);
        }
    }
}
