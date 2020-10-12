package com.iwayvietnam.zmsso;

import com.iwayvietnam.zmsso.pac4j.SettingsBuilder;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.*;
import com.zimbra.cs.account.auth.AuthContext;
import com.zimbra.cs.extension.ExtensionHttpHandler;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.servlet.ZimbraServlet;
import com.zimbra.cs.servlet.util.AuthUtil;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class BaseSsoHandler extends ExtensionHttpHandler {
    protected static final Provisioning prov = Provisioning.getInstance();
    protected final Config pac4jConfig;

    public enum SSOProtocol {
        ZM_SSO_SAML, ZM_SSO_CAS, ZM_SSO_OIDC
    }

    public BaseSsoHandler() {
        pac4jConfig = SettingsBuilder.build();
    }

    protected void doLogin(HttpServletRequest request, HttpServletResponse response, Client client) throws IOException, ServiceException {
        final AuthToken authToken = AuthUtil.getAuthTokenFromCookie(request, response);
        final Optional<AuthToken> optional = Optional.ofNullable(authToken);
        boolean mustLogin = !optional.isPresent() || authToken.isExpired();
        if (mustLogin) {
            final JEEContext context = new JEEContext(request, response);
            RedirectionAction action;

            try {
                action = (RedirectionAction) client.getRedirectionAction(context).get();
            } catch (final RedirectionAction e) {
                action = e;
            }

            JEEHttpActionAdapter.INSTANCE.adapt(action, context);
        } else {
            setAuthTokenCookieAndRedirect(request, response, authToken);
        }
    }

    protected void singleLogin(HttpServletRequest request, HttpServletResponse response, String accountName, String ssoToken, SSOProtocol protocol) throws ServiceException, IOException {
        Map<String, Object> authCtxt = new HashMap<>();

        String origIp = ZimbraServlet.getOrigIp(request);
        String remoteIp = ZimbraServlet.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        authCtxt.put(AuthContext.AC_ORIGINATING_CLIENT_IP, origIp);
        authCtxt.put(AuthContext.AC_REMOTE_IP, remoteIp);
        authCtxt.put(AuthContext.AC_ACCOUNT_NAME_PASSEDIN, accountName);
        authCtxt.put(AuthContext.AC_USER_AGENT, userAgent);

        Account account = prov.getAccountByName(accountName);
        prov.ssoAuthAccount(account, AuthContext.Protocol.soap, authCtxt);
        DbSsoSession.ssoSessionLogin(account, ssoToken, protocol.toString(), origIp, remoteIp, userAgent);

        AuthToken authToken = AuthProvider.getAuthToken(account, false);
        setAuthTokenCookieAndRedirect(request, response, authToken);
    }

    protected void singleLogout(String ssoToken) throws ServiceException {
        String accountId = DbSsoSession.ssoSessionLogout(ssoToken);
        if (!StringUtil.isNullOrEmpty(accountId)) {
            Account account = prov.getAccountById(accountId);
            int validityValue = account.getAuthTokenValidityValue();
            if (validityValue > 99) {
                validityValue = 0;
            }
            account.setAuthTokenValidityValue(validityValue + 1);
        }
    }

    protected void setAuthTokenCookieAndRedirect(HttpServletRequest request, HttpServletResponse response, AuthToken authToken) throws IOException, ServiceException {
        boolean isAdmin = AuthToken.isAnyAdmin(authToken);
        boolean secureCookie = isProtocolSecure(request.getScheme());
        authToken.encode(response, isAdmin, secureCookie);

        Server server = prov.getServer(authToken.getAccount());
        String redirectUrl = AuthUtil.getRedirectURL(request, server, isAdmin, false);
        redirectUrl = appendIgnoreLoginURL(redirectUrl);

        URL url = new URL(redirectUrl);
        boolean isRedirectProtocolSecure = isProtocolSecure(url.getProtocol());

        if (secureCookie && !isRedirectProtocolSecure) {
            throw ServiceException.INVALID_REQUEST(String.format("Cannot redirect to non-secure protocol: %s", redirectUrl), null);
        }

        ZimbraLog.extensions.debug(String.format("SSO Login - redirecting (with auth token) to: %s", redirectUrl));
        response.sendRedirect(redirectUrl);
    }

    protected void clearAuthToken(HttpServletRequest request, HttpServletResponse response, String ssoToken) throws AuthTokenException, ServiceException {
        AuthToken authToken = AuthUtil.getAuthTokenFromHttpReq(request, false);
        if (authToken != null) {
            authToken.encode(request, response, true);
            authToken.deRegister();
        }
        ZimbraCookie.clearCookie(response, ZimbraCookie.COOKIE_ZM_AUTH_TOKEN);
        String accountId = DbSsoSession.ssoSessionLogout(ssoToken);
        ZimbraLog.extensions.debug(String.format("SSO session logout for account id: %s", accountId));
//        Logout url:  https://yourzimbraserver.com/?loginOp=logout
    }

    private String appendIgnoreLoginURL(String redirectUrl) {
        if (!redirectUrl.endsWith("/")) {
            redirectUrl = redirectUrl + "/";
        }
        return redirectUrl + AuthUtil.IGNORE_LOGIN_URL;
    }

    private boolean isProtocolSecure(String protocol) {
        return URLUtil.PROTO_HTTPS.equalsIgnoreCase(protocol);
    }
}
