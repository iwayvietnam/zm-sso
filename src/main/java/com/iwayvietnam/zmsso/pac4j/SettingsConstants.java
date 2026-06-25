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

/**
 * Settings Constants
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public final class SettingsConstants {
    public static final String ZM_SSO_SETTINGS_FILE = "zm.sso.properties";

    public static final String ZM_SSO_DEFAULT_CLIENT = "sso.defaultClient";
    public static final String ZM_SSO_CALLBACK_URL = "sso.callbackUrl";
    public static final String ZM_CAS_CALLBACK_URL = "cas.callbackUrl";
    public static final String ZM_SAML_CALLBACK_URL = "saml.callbackUrl";
    public static final String ZM_OIDC_CALLBACK_URL = "oidc.callbackUrl";

    public static final String ZM_SSO_SAVE_IN_SESSION = "sso.saveInSession";
    public static final String ZM_SSO_MULTI_PROFILE = "sso.multiProfile";
    public static final String ZM_SSO_RENEW_SESSION = "sso.renewSession";

    public static final String ZM_SSO_LOCAL_LOGOUT = "sso.localLogout";
    public static final String ZM_SSO_DESTROY_SESSION = "sso.destroySession";
    public static final String ZM_SSO_CENTRAL_LOGOUT = "sso.centralLogout";
    public static final String ZM_SSO_POST_LOGOUT_URL = "sso.postLogoutURL";

    public static final String ZM_SAML_RESPONSE_BINDING = "saml.responseBindingType";
    public static final String ZM_SAML_LOGOUT_REQUEST_BINDING = "saml.spLogoutRequestBindingType";
    public static final String ZM_SAML_LOGOUT_RESPONSE_BINDING = "saml.spLogoutResponseBindingType";
    public static final String ZM_SAML_AUTHN_REQUEST_SIGNED = "saml.authnRequestSigned";
    public static final String ZM_SAML_LOGOUT_REQUEST_SIGNED = "saml.logoutRequestSigned";
    public static final String ZM_SAML_ALL_SIGNATURE_VALIDATION_DISABLED = "saml.allSignatureValidationDisabled";
    public static final String ZM_SAML_WANTS_ASSERTIONS_SIGNED = "saml.wantsAssertionsSigned";
    public static final String ZM_SAML_WANTS_RESPONSES_SIGNED = "saml.wantsResponsesSigned";
    public static final String ZM_SAML_FORCE_AUTH = "saml.forceAuth";

    public static final String ZM_OIDC_SCOPE = "oidc.scope";
    public static final String ZM_DEFAULT_OIDC_SCOPE = "openid email profile";
    public static final String ZM_OIDC_WITH_STATE = "oidc.withState";
}
