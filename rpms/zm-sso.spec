Name:           zm-sso
Version:        1.0.2
Release:        1%{?dist}
Summary:        Zimbra Single Sign On (Zm SSO)

Group:          Applications/Internet
License:        AGPLv3
URL:            https://github.com/iwayvietnam/zm-sso
Source0:        https://github.com/iwayvietnam/zm-sso/archive/refs/tags/%{version}.tar.gz

Requires:       zimbra-store = 8.8.15
BuildRequires:  java-11-openjdk-devel maven
BuildArch:      noarch

%description
Zm SSO is the Zimbra Collaboration Open Source Edition extension for single sign-on authentication to the Zimbra Web Client.

%prep
%setup -q

%build
mvn clean package

%install
mkdir -p $RPM_BUILD_ROOT/opt/zimbra/lib/ext/zm-sso
mkdir -p $RPM_BUILD_ROOT/opt/zimbra/jetty_base/common/lib
mkdir -p $RPM_BUILD_ROOT/opt/zimbra/conf
cp -R target/*.jar $RPM_BUILD_ROOT/opt/zimbra/lib/ext/zm-sso
cp -R target/dependencies/*.jar $RPM_BUILD_ROOT/opt/zimbra/jetty_base/common/lib
cp -R conf/zm.sso.properties $RPM_BUILD_ROOT/opt/zimbra/conf

%posttrans
su - zimbra -c "zmmailboxdctl restart"
su - zimbra -c "zmprov fc all"

%postun
su - zimbra -c "zmmailboxdctl restart"
su - zimbra -c "zmprov fc all"

%files
/opt/zimbra/conf/zm.sso.properties
/opt/zimbra/lib/ext/zm-sso/*.jar
/opt/zimbra/jetty_base/common/lib/accessors-smart-1.2.jar
/opt/zimbra/jetty_base/common/lib/antlr-2.7.7.jar
/opt/zimbra/jetty_base/common/lib/asm-5.0.4.jar
/opt/zimbra/jetty_base/common/lib/cas-client-core-3.6.1.jar
/opt/zimbra/jetty_base/common/lib/cas-client-support-saml-3.6.1.jar
/opt/zimbra/jetty_base/common/lib/checker-qual-2.11.1.jar
/opt/zimbra/jetty_base/common/lib/commons-lang3-3.11.jar
/opt/zimbra/jetty_base/common/lib/content-type-2.1.jar
/opt/zimbra/jetty_base/common/lib/cryptacular-1.2.4.jar
/opt/zimbra/jetty_base/common/lib/error_prone_annotations-2.3.4.jar
/opt/zimbra/jetty_base/common/lib/failureaccess-1.0.1.jar
/opt/zimbra/jetty_base/common/lib/hamcrest-core-1.3.jar
/opt/zimbra/jetty_base/common/lib/hibernate-commons-annotations-4.0.4.Final.jar
/opt/zimbra/jetty_base/common/lib/hibernate-core-4.3.5.Final.jar
/opt/zimbra/jetty_base/common/lib/hibernate-entitymanager-4.3.5.Final.jar
/opt/zimbra/jetty_base/common/lib/hibernate-jpa-2.1-api-1.0.0.Final.jar
/opt/zimbra/jetty_base/common/lib/HikariCP-3.4.5.jar
/opt/zimbra/jetty_base/common/lib/j2objc-annotations-1.3.jar
/opt/zimbra/jetty_base/common/lib/jackson-annotations-2.11.2.jar
/opt/zimbra/jetty_base/common/lib/jackson-core-2.11.2.jar
/opt/zimbra/jetty_base/common/lib/jackson-databind-2.11.2.jar
/opt/zimbra/jetty_base/common/lib/jandex-1.1.0.Final.jar
/opt/zimbra/jetty_base/common/lib/javassist-3.18.1-GA.jar
/opt/zimbra/jetty_base/common/lib/java-support-7.5.2.jar
/opt/zimbra/jetty_base/common/lib/javax.annotation-api-1.3.2.jar
/opt/zimbra/jetty_base/common/lib/javax.json-1.0.4.jar
/opt/zimbra/jetty_base/common/lib/javax.json-api-1.0.jar
/opt/zimbra/jetty_base/common/lib/javax.mail-1.6.2.jar
/opt/zimbra/jetty_base/common/lib/jboss-logging-3.1.3.GA.jar
/opt/zimbra/jetty_base/common/lib/jboss-logging-annotations-1.2.0.Beta1.jar
/opt/zimbra/jetty_base/common/lib/jboss-transaction-api_1.2_spec-1.0.0.Final.jar
/opt/zimbra/jetty_base/common/lib/jcip-annotations-1.0-1.jar
/opt/zimbra/jetty_base/common/lib/jcl-over-slf4j-1.7.30.jar
/opt/zimbra/jetty_base/common/lib/jcommander-1.48.jar
/opt/zimbra/jetty_base/common/lib/joda-time-2.10.6.jar
/opt/zimbra/jetty_base/common/lib/json-smart-2.3.jar
/opt/zimbra/jetty_base/common/lib/jsr305-3.0.2.jar
/opt/zimbra/jetty_base/common/lib/lang-tag-1.4.4.jar
/opt/zimbra/jetty_base/common/lib/ldaptive-1.0.13.jar
/opt/zimbra/jetty_base/common/lib/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar
/opt/zimbra/jetty_base/common/lib/metrics-core-3.1.5.jar
/opt/zimbra/jetty_base/common/lib/nimbus-jose-jwt-8.22.1.jar
/opt/zimbra/jetty_base/common/lib/oauth2-oidc-sdk-8.22.jar
/opt/zimbra/jetty_base/common/lib/opensaml-core-3.4.6.jar
/opt/zimbra/jetty_base/common/lib/opensaml-messaging-api-3.4.6.jar
/opt/zimbra/jetty_base/common/lib/opensaml-messaging-impl-3.4.6.jar
/opt/zimbra/jetty_base/common/lib/opensaml-profile-api-3.4.6.jar
/opt/zimbra/jetty_base/common/lib/opensaml-profile-impl-3.4.6.jar
/opt/zimbra/jetty_base/common/lib/opensaml-saml-api-3.4.6.jar
/opt/zimbra/jetty_base/common/lib/opensaml-saml-impl-3.4.6.jar
/opt/zimbra/jetty_base/common/lib/opensaml-security-api-3.4.6.jar
/opt/zimbra/jetty_base/common/lib/opensaml-security-impl-3.4.6.jar
/opt/zimbra/jetty_base/common/lib/opensaml-soap-api-3.4.6.jar
/opt/zimbra/jetty_base/common/lib/opensaml-soap-impl-3.4.6.jar
/opt/zimbra/jetty_base/common/lib/opensaml-storage-api-3.4.6.jar
/opt/zimbra/jetty_base/common/lib/opensaml-storage-impl-3.4.6.jar
/opt/zimbra/jetty_base/common/lib/opensaml-xmlsec-api-3.4.6.jar
/opt/zimbra/jetty_base/common/lib/opensaml-xmlsec-impl-3.4.6.jar
/opt/zimbra/jetty_base/common/lib/pac4j-cas-4.5.7.jar
/opt/zimbra/jetty_base/common/lib/pac4j-config-4.5.7.jar
/opt/zimbra/jetty_base/common/lib/pac4j-core-4.5.7.jar
/opt/zimbra/jetty_base/common/lib/pac4j-oidc-4.5.7.jar
/opt/zimbra/jetty_base/common/lib/pac4j-saml-opensamlv3-4.5.7.jar
/opt/zimbra/jetty_base/common/lib/serializer-2.7.2.jar
/opt/zimbra/jetty_base/common/lib/spring-core-5.2.20.RELEASE.jar
/opt/zimbra/jetty_base/common/lib/spring-jcl-5.2.20.RELEASE.jar
/opt/zimbra/jetty_base/common/lib/spring-jdbc-4.3.30.RELEASE.jar
/opt/zimbra/jetty_base/common/lib/spring-orm-4.3.30.RELEASE.jar
/opt/zimbra/jetty_base/common/lib/spring-tx-4.3.30.RELEASE.jar
/opt/zimbra/jetty_base/common/lib/stax2-api-3.1.4.jar
/opt/zimbra/jetty_base/common/lib/velocity-engine-core-2.3.jar
/opt/zimbra/jetty_base/common/lib/woodstox-core-5.0.3.jar
/opt/zimbra/jetty_base/common/lib/xalan-2.7.2.jar
/opt/zimbra/jetty_base/common/lib/xml-apis-1.3.04.jar
/opt/zimbra/jetty_base/common/lib/xmlsec-2.0.10.jar
/opt/zimbra/jetty_base/common/lib/xmlsectool-2.0.0.jar

%changelog
* Fri Apr 21 2023 Nguyen Van Nguyen <nguyennv1981@gmail.com> - 1.0.0-1
- Initial release 1.0.0.
* Fri Aug 25 2023 Nguyen Van Nguyen <nguyennv1981@gmail.com> - 1.0.1-1
- Release 1.0.1.
* Fri May 08 2023 Nguyen Van Nguyen <nguyennv1981@gmail.com> - 1.0.2-1
- Release 1.0.2.
