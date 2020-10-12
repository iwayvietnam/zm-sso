package com.iwayvietnam.zmsso;

import com.iwayvietnam.zmsso.cas.*;
import com.iwayvietnam.zmsso.oidc.*;
import com.iwayvietnam.zmsso.saml.*;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.extension.ExtensionDispatcherServlet;
import com.zimbra.cs.extension.ExtensionException;
import com.zimbra.cs.extension.ZimbraExtension;

public class ZmSsoExtension implements ZimbraExtension {
    private static final String EXTENSION_NAME = "sso";

    @Override
    public String getName() {
        return EXTENSION_NAME;
    }

    @Override
    public void init() throws ExtensionException, ServiceException {
        DbSsoSession.createSsoSessionTable();

        ExtensionDispatcherServlet.register(this, new SamlMetadataHandler());
        ExtensionDispatcherServlet.register(this, new SamlLoginHandler());
        ExtensionDispatcherServlet.register(this, new SamlCallbackHandler());
        ExtensionDispatcherServlet.register(this, new SamlLogoutHandler());
        ExtensionDispatcherServlet.register(this, new SamlSloHandler());

        ExtensionDispatcherServlet.register(this, new CasLoginHandler());
        ExtensionDispatcherServlet.register(this, new CasCallbackHandler());
        ExtensionDispatcherServlet.register(this, new CasLogoutHandler());
        ExtensionDispatcherServlet.register(this, new CasSloHandler());

        ExtensionDispatcherServlet.register(this, new OidcLoginHandler());
        ExtensionDispatcherServlet.register(this, new OidcCallbackHandler());
        ExtensionDispatcherServlet.register(this, new OidcLogoutHandler());
        ExtensionDispatcherServlet.register(this, new OidcSloHandler());
    }

    @Override
    public void destroy() {
        ExtensionDispatcherServlet.unregister(this);
    }
}
