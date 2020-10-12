/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zm SSO is the the  Zimbra Collaboration Open Source Edition extension Single Sign On authentication to the Zimbra Web Client.
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
 * Zimbra Hierarchical Address Book
 *
 * Written by Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
package com.iwayvietnam.zmsso.cas;

import com.iwayvietnam.zmsso.BaseSsoHandler;
import com.zimbra.cs.extension.ExtensionException;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.context.ContextHelper;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;

import java.util.Optional;

/**
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public class CasBaseHandler  extends BaseSsoHandler {
    protected final CasClient client;

    public CasBaseHandler () throws ExtensionException {
        super();
        client = pac4jConfig.getClients().findClient(CasClient.class).orElseThrow(() -> new ExtensionException("No client found"));
    }

    protected boolean isBackLogoutRequest(final WebContext context) {
        return ContextHelper.isPost(context)
                && !isMultipartRequest(context)
                && context.getRequestParameter(CasConfiguration.LOGOUT_REQUEST_PARAMETER).isPresent();
    }

    protected boolean isMultipartRequest(final WebContext context) {
        final Optional<String> contentType = context.getRequestHeader(HttpConstants.CONTENT_TYPE_HEADER);
        return contentType.isPresent() && contentType.get().toLowerCase().startsWith("multipart");
    }
}
