package com.iwayvietnam.zmsso.saml;

import com.zimbra.cs.extension.ExtensionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SamlMetadataHandler extends SamlBaseHandler {

    public SamlMetadataHandler() throws ExtensionException {
        super();
    }

    @Override
    public String getPath() {
        return SamlSsoConstants.METADATA_HANDLER_PATH;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        client.init();
        response.getWriter().write(client.getServiceProviderMetadataResolver().getMetadata());
        response.getWriter().flush();
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }
}
