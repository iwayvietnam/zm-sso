/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zm SSO is the Zimbra Collaboration Open Source Edition extension for single sign-on authentication to the Zimbra Web Client.
 * Copyright (C) 2020-present iWay Vietnam and/or its affiliates. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 * ***** END LICENSE BLOCK *****
 *
 * Zimbra Single Sign On
 *
 * Written by Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
package com.iwayvietnam.zmsso.pac4j;

import com.zimbra.common.util.StringUtil;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.logout.OidcLogoutActionBuilder;

import java.util.Optional;

/**
 * Oidc logout action builder
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
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
