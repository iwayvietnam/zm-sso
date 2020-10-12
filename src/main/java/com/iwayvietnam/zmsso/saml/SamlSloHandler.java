package com.iwayvietnam.zmsso.saml;

import com.zimbra.cs.extension.ExtensionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SamlSloHandler extends SamlBaseHandler {

    public SamlSloHandler() throws ExtensionException {
        super();
    }

    @Override
    public String getPath() {
        return SamlSsoConstants.SLO_HANDLER_PATH;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }
}
