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

import com.zimbra.common.util.ZimbraLog;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.redirect.RedirectionActionBuilder;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.redirect.SAML2RedirectionActionBuilder;

import java.util.Optional;

/**
 * Redirection action builder for SAML 2.
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public class ZmSAML2RedirectionActionBuilder extends SAML2RedirectionActionBuilder implements RedirectionActionBuilder {

    public ZmSAML2RedirectionActionBuilder(final SAML2Client client) {
        super(client);
    }

    @Override
    public Optional<RedirectionAction> getRedirectionAction(final WebContext wc, final SessionStore sessionStore) {
        final var thread = Thread.currentThread();
        final var origCl = thread.getContextClassLoader();
        thread.setContextClassLoader(getClass().getClassLoader());

        ZimbraLog.extensions.debug("SAML getRedirectionAction");
        final var action = super.getRedirectionAction(wc, sessionStore);
        thread.setContextClassLoader(origCl);
        return action;
    }
}
