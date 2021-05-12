package com.iwayvietnam.zmsso.pac4j;

import com.zimbra.common.util.StringUtil;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.logout.OidcLogoutActionBuilder;

import java.util.Optional;

public class ZmOidcLogoutActionBuilder extends OidcLogoutActionBuilder {
    private final String postLogoutURL;

    public ZmOidcLogoutActionBuilder(final OidcConfiguration configuration, final String postLogoutURL) {
        super(configuration);
        this.postLogoutURL = postLogoutURL;
    }

    @Override
    public Optional<RedirectionAction> getLogoutAction(final WebContext context, final UserProfile currentProfile, final String targetUrl) {
        if (StringUtil.isNullOrEmpty(targetUrl)) {
            return super.getLogoutAction(context, currentProfile, postLogoutURL);
        }
        else {
            return super.getLogoutAction(context, currentProfile, targetUrl);
        }
    }
}
