Register Identity Experience Framework applications
===================================================
Azure AD B2C requires you to register two applications that it uses to sign up and sign in users with local accounts: *IdentityExperienceFramework*, a web API, and *ProxyIdentityExperienceFramework*, a native app with delegated permission to the IdentityExperienceFramework app.
Your users can sign up with an email address or username and a password to access tenant-registered applications, which creates a "local account." Local accounts exist only in Azure AD B2C tenant.

### Register the IdentityExperienceFramework application
1. Select **App registrations**, and then select **New registration**.
2. For **Name**, enter *Zimbra SAML IdentityExperienceFramework*.
3. Under **Supported account types**, select **Accounts in this organizational directory only**.
4. Under **Redirect URI**, select **Web**, and then enter *`https://tenant.b2clogin.com/tenant.onmicrosoft.com`*.
5. Under **Permissions**, select the *Grant admin consent to openid and offline_access permissions* check box.
6. Select **Register**.

Next, expose the API by adding a scope:
1. In the left menu, under **Manage**, select **Expose an API**.
2. Select **Add a scope**, then select **Save and continue** to accept the default application ID URI.
3. Enter the following values to create a scope that allows custom policy execution in Azure AD B2C tenant
   * **Scope name**: *user_impersonation*
   * **Admin consent display name**: *Access IdentityExperienceFramework*
   * **Admin consent description**: *Allow the application to access IdentityExperienceFramework on behalf of the signed-in user*.
4. Select **Add scope**

### Register the ProxyIdentityExperienceFramework application
1. Select **App registrations**, and then select **New registration**.
2. For **Name**, enter *Zimbra SAML ProxyIdentityExperienceFramework*.
3. Under **Supported account types**, select **Accounts in this organizational directory only**.
4. Under **Redirect URI**, use the drop-down to select **Public client/native (mobile & desktop)**.
5. For **Redirect URI**, enter *`myapp://auth`*.
6. Under **Permissions**, select the *Grant admin consent to openid and offline_access permissions* check box.
7. Select **Register**.

Next, specify that the application should be treated as a public client:
1. In the left menu, under **Manage**, select **Authentication**.
2. Under **Advanced settings**, in the **Allow public client flows** section, set **Enable the following mobile and desktop flows** to **Yes**.
3. Select **Save**.
4. Ensure that **"allowPublicClient"**: true is set in the application manifest:
   * In the left menu, under **Manage**, select **Manifest** to open application manifest.
   * Find **allowPublicClient** key and ensure its value is set to **true**.

Now, grant permissions to the API scope you exposed earlier in the *IdentityExperienceFramework* registration:
1. In the left menu, under **Manage**, select **API permissions**.
2. Under **Configured permissions**, select **Add a permission**.
3. Select the **My APIs** tab, then select the **IdentityExperienceFramework** application.
4. Under **Permission**, select the **user_impersonation** scope that you defined earlier.
5. Select **Add permissions**. As directed, wait a few minutes before proceeding to the next step.
6. Select **Grant admin consent for ....**.
7. Select **Yes**.
8. Select **Refresh**, and then verify that "Granted for ..." appears under **Status** for the scope.

### Register the Zimbra Application
1. Select **App registrations**, and then select **New registration**.
2. Enter a Name for the application such as: *Zimbra SAML SSO*.
3. Under **Supported account types**, select **Accounts in any organizational directory or any identity provider. For authenticating users with Azure AD B2C**.
4. Under **Redirect URI**, select **Web**, and then enter the URL of Zimbra Application. Example: `https://tenant.onmicrosoft.com`
4. Select Register.
5. Under **Manage**, click on **Expose an API**.
6. Click on **Set** for the **Application ID URI** and then click on **Save**, accepting the default value.

Add signing and encryption keys for Identity Experience Framework applications
==============================================================================
In the Azure portal, search for and select Azure AD B2C. On the overview page, under Policies, select Identity Experience Framework

### Create the signing key
1. Select **Policy Keys** and then select **Add**.
2. For **Options**, choose *Generate*.
3. In **Name**, enter *ZimbraSAMLTokenSigningKeyContainer*. The prefix *B2C_1A_* might be added automatically.
4. For **Key type**, select *RSA*.
5. For **Key usage**, select *Signature*.
6. Select **Create**.

### Create the encryption key
1. Select **Policy Keys** and then select **Add**.
2. For **Options**, choose *Generate*.
3. In **Name**, enter *ZimbraSAMLTokenEncryptionKeyContainer*. The prefix *B2C_1A_* might be added automatically.
4. For **Key type**, select *RSA*.
5. For **Key usage**, select *Encryption*.
6. Select **Create**.

### Create a policy key for SAML application
To have a trust relationship between SAML application and Azure AD B2C, create a signing certificate for the SAML response.
Azure AD B2C uses this certificate to sign the SAML response sent to SAML application

1. Create a SAML policy key
   * By using openssl
   ```shell
   openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 3650 -subj '/CN=saml.tenant.onmicrosoft.com'
   openssl pkcs12 -export -out ZimbraSAMLIdpCertificate.pfx -inkey key.pem -in cert.pem
   ```
   * By using PowerShell
   ```shell
   New-SelfSignedCertificate `
    -KeyExportPolicy Exportable `
    -Subject "CN=saml.tenant.onmicrosoft.com" `
    -KeyAlgorithm RSA `
    -KeyLength 2048 `
    -KeyUsage DigitalSignature `
    -NotAfter (Get-Date).AddMonths(36) `
    -CertStoreLocation "Cert:\CurrentUser\My"
   ```
2. Under **Policies**, select **Identity Experience Framework** and then **Policy keys**.
3. Select **Add**, and then select **Options > Upload**
4. Enter the **Name** as *ZimbraSAMLIdpCertificate*. The prefix **B2C_1A_** is automatically added to the name of policy key.
5. Using the upload file control, upload certificate that was generated in the above steps along with the SSO policies (ZimbraSAMLIdpCertificate.pfx).
6. Enter the certificate's password as tenant name and click on **Create**.
7. You should be able to see a new policy key with the name **B2C_1A_ZimbraSAMLIdpCertificate**.

Create custom policies in Azure Active Directory B2C
====================================================
Custom policies are a set of XML files you upload to Azure AD B2C tenant to define technical profiles and user journeys.

### Get the starter pack
Get the custom policy starter packs from GitHub, then update the XML files in the LocalAccounts starter pack with Azure AD B2C tenant name.
1. [Download the .zip file](https://github.com/Azure-Samples/active-directory-b2c-custom-policy-starterpack/archive/master.zip) or clone the repository:
```shell
git clone https://github.com/Azure-Samples/active-directory-b2c-custom-policy-starterpack
```
2. In all of the files in the **LocalAccounts** directory, replace the string yourtenant with the name of Azure AD B2C tenant.
For example, if the name of B2C tenant is contosotenant, all instances of *yourtenant.onmicrosoft.com* become *contosotenant.onmicrosoft.com*.

### Add application IDs to the custom policy
1. Open **LocalAccounts/TrustFrameworkExtensions.xml** and find the element `<TechnicalProfile Id="login-NonInteractive">`.
2. Replace **B2C_1A_TrustFrameworkLocalization** with **B2C_1A_TrustFrameworkBase**
2. Replace both instances of **IdentityExperienceFrameworkAppId** with the application ID of the *IdentityExperienceFramework* application that you created earlier.
3. Replace both instances of **ProxyIdentityExperienceFrameworkAppId** with the application ID of the *ProxyIdentityExperienceFramework* application that you created earlier.
4. Save the file.

### Register a SAML application in Azure AD B2C
1. Clone **LocalAccounts/SignUpOrSignin.xml** to **LocalAccounts/SignUpOrSigninSAML.xml**
2. Open **LocalAccounts/SignUpOrSigninSAML.xml** with preferred editor and then remove `<RelyingParty>` section.
3. Change the **PolicyId** and **PublicPolicyUri** values of the policy to *B2C_1A_Zimbra_SAML_Signin* and *`http://tenant.onmicrosoft.com/B2C_1A_Zimbra_SAML_Signin`*
4. Enable custom policy to connect with a SAML application by adding the `<ClaimsProviders>` section in `<TrustFrameworkPolicy>`
```xml
  <ClaimsProviders>
    <ClaimsProvider>
      <DisplayName>Token Issuer</DisplayName>
      <TechnicalProfiles>
        <!-- SAML Token Issuer technical profile -->
        <TechnicalProfile Id="Saml2AssertionIssuer">
          <DisplayName>Token Issuer</DisplayName>
          <Protocol Name="SAML2" />
          <OutputTokenFormat>SAML2</OutputTokenFormat>
          <Metadata>
            <Item Key="IssuerUri">https://tenant.b2clogin.com/tenant.onmicrosoft.com/B2C_1A_Zimbra_SAML_Signin</Item>
          </Metadata>
          <CryptographicKeys>
            <Key Id="MetadataSigning" StorageReferenceId="B2C_1A_SamlIdpCert" />
            <Key Id="SamlAssertionSigning" StorageReferenceId="B2C_1A_SamlIdpCert" />
            <Key Id="SamlMessageSigning" StorageReferenceId="B2C_1A_SamlIdpCert" />
          </CryptographicKeys>
          <InputClaims/>
          <OutputClaims/>
          <UseTechnicalProfileForSessionManagement ReferenceId="SM-Saml" />
        </TechnicalProfile>

        <!-- Session management technical profile for SAML based tokens -->
        <TechnicalProfile Id="SM-Saml">
          <DisplayName>Session Management Provider</DisplayName>
          <Protocol Name="Proprietary" Handler="Web.TPEngine.SSO.SamlSSOSessionProvider, Web.TPEngine, Version=1.0.0.0, Culture=neutral, PublicKeyToken=null" />
        </TechnicalProfile>
      </TechnicalProfiles>
    </ClaimsProvider>
  </ClaimsProviders>
```
5. Create a sign-up or sign-in policy configured for SAML by adding the `<UserJourneys>` section and `<RelyingParty>` section in `<TrustFrameworkPolicy>`
```xml
  <UserJourneys>
    <UserJourney Id="SignUpOrSigninSAML">
      <OrchestrationSteps>
        <OrchestrationStep Order="1" Type="CombinedSignInAndSignUp" ContentDefinitionReferenceId="api.signuporsignin">
          <ClaimsProviderSelections>
            <ClaimsProviderSelection ValidationClaimsExchangeId="LocalAccountSigninEmailExchange" />
          </ClaimsProviderSelections>
          <ClaimsExchanges>
            <ClaimsExchange Id="LocalAccountSigninEmailExchange" TechnicalProfileReferenceId="SelfAsserted-LocalAccountSignin-Email" />
          </ClaimsExchanges>
        </OrchestrationStep>

        <OrchestrationStep Order="2" Type="ClaimsExchange">
          <Preconditions>
            <Precondition Type="ClaimsExist" ExecuteActionsIf="true">
              <Value>objectId</Value>
              <Action>SkipThisOrchestrationStep</Action>
            </Precondition>
          </Preconditions>
          <ClaimsExchanges>
            <ClaimsExchange Id="SignUpWithLogonEmailExchange" TechnicalProfileReferenceId="LocalAccountSignUpWithLogonEmail" />
          </ClaimsExchanges>
        </OrchestrationStep>

        <!-- This step reads any user attributes that we may not have received when in the token. -->
        <OrchestrationStep Order="3" Type="ClaimsExchange">
          <ClaimsExchanges>
            <ClaimsExchange Id="AADUserReadWithObjectId" TechnicalProfileReferenceId="AAD-UserReadUsingObjectId" />
          </ClaimsExchanges>
        </OrchestrationStep>

        <OrchestrationStep Order="4" Type="SendClaims" CpimIssuerTechnicalProfileReferenceId="Saml2AssertionIssuer" />
      </OrchestrationSteps>
      <ClientDefinition ReferenceId="DefaultWeb" />
    </UserJourney>
  </UserJourneys>

  <RelyingParty>
    <DefaultUserJourney ReferenceId="SignUpOrSigninSAML" />
    <TechnicalProfile Id="PolicyProfile">
      <DisplayName>PolicyProfile</DisplayName>
      <Protocol Name="SAML2"/>
      <Metadata>
        <Item Key="PartnerEntity">https://zimbra.server/service/extension/saml/metadata</Item>
        <!-- <Item Key="PartnerEntity"><![CDATA[
        Embed the metadata directly
        ]]></Item> -->
      </Metadata>
      <OutputClaims>
        <OutputClaim ClaimTypeReferenceId="displayName" />
        <OutputClaim ClaimTypeReferenceId="signInNames.emailAddress" PartnerClaimType="email" />
        <OutputClaim ClaimTypeReferenceId="signInName" />
        <OutputClaim ClaimTypeReferenceId="objectId" PartnerClaimType="objectId"/>
      </OutputClaims>
      <SubjectNamingInfo ClaimType="signInName" ExcludeAsClaim="true"/> 
    </TechnicalProfile>
  </RelyingParty>
```

### Upload the Policies
1. Select the **Identity Experience Framework** menu item in B2C tenant in the Azure portal.
2. Select **Upload custom policy**.
3. As per the following order, upload the policy files in the above steps: *TrustFrameworkBase.xml, TrustFrameworkExtensions.xml, SignUpOrSigninSAML.xml*

### Test the Azure AD B2C IdP SAML metadata
After the policy files are uploaded, Azure AD B2C uses the configuration information to generate the identity provider's SAML metadata document that the application will use. The SAML metadata document contains the locations of services, such as sign-in methods, logout methods, and certificates. 
The Azure AD B2C policy metadata is available at the following URL: https://tenant.b2clogin.com/tenant.onmicrosoft.com/B2C_1A_Zimbra_SAML_Signin/samlp/metadata

Reference Documents
===================
* Register an application tutorial: https://docs.microsoft.com/en-us/azure/active-directory-b2c/tutorial-register-applications
* Create custom policies tutorial: https://docs.microsoft.com/en-us/azure/active-directory-b2c/tutorial-create-user-flows
* TrustFrameworkPolicy reference: https://docs.microsoft.com/en-us/azure/active-directory-b2c/trustframeworkpolicy
* Register a SAML application in Azure AD B2C tutorial: https://docs.microsoft.com/en-us/azure/active-directory-b2c/saml-service-provider
* WordPress SAML SSO with Azure B2C SAML IdP tutorial: https://plugins.miniorange.com/saml-single-sign-on-sso-wordpress-using-azure-b2c
* Define a SAML identity provider technical profile in an Azure Active Directory B2C custom policy: https://docs.microsoft.com/en-us/azure/active-directory-b2c/saml-identity-provider-technical-profile
* Define a technical profile for a SAML token issuer in an Azure Active Directory B2C custom policy: https://docs.microsoft.com/en-us/azure/active-directory-b2c/saml-issuer-technical-profile
