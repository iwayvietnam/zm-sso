Name:           zm-sso
Version:        2.0.0
Release:        1%{?dist}
Summary:        Zimbra Single Sign On (Zm SSO)

Group:          Applications/Internet
License:        AGPLv3
URL:            https://github.com/iwayvietnam/zm-sso
Source0:        https://github.com/iwayvietnam/zm-sso/archive/refs/tags/%{version}.tar.gz

Requires:       zimbra-store >= 10.0.0
BuildRequires:  java-17-openjdk-devel maven
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
mv /opt/zimbra/jetty_base/common/lib/commons-codec-1.14.jar /opt/zimbra/jetty_base/common/lib/commons-codec-1.14.jar.bak
su - zimbra -c "zmmailboxdctl restart"
su - zimbra -c "zmprov fc all"

%postun
mv /opt/zimbra/jetty_base/common/lib/commons-codec-1.14.jar.bak /opt/zimbra/jetty_base/common/lib/commons-codec-1.14.jar
su - zimbra -c "zmmailboxdctl restart"
su - zimbra -c "zmprov fc all"

%files
/opt/zimbra/conf/zm.sso.properties
/opt/zimbra/lib/ext/zm-sso/zm-sso-2.0.0-1.jar
/opt/zimbra/jetty_base/common/lib/accessors-smart-2.4.9.jar
/opt/zimbra/jetty_base/common/lib/bcprov-jdk18on-1.71.jar
/opt/zimbra/jetty_base/common/lib/bcutil-jdk15on-1.70.jar
/opt/zimbra/jetty_base/common/lib/cas-client-core-4.0.0.jar
/opt/zimbra/jetty_base/common/lib/cas-client-support-saml-4.0.0.jar
/opt/zimbra/jetty_base/common/lib/checker-qual-3.12.0.jar
/opt/zimbra/jetty_base/common/lib/commons-codec-1.15.jar
/opt/zimbra/jetty_base/common/lib/content-type-2.2.jar
/opt/zimbra/jetty_base/common/lib/cryptacular-1.2.5.jar
/opt/zimbra/jetty_base/common/lib/error_prone_annotations-2.11.0.jar
/opt/zimbra/jetty_base/common/lib/failureaccess-1.0.1.jar
/opt/zimbra/jetty_base/common/lib/HikariCP-5.0.1.jar
/opt/zimbra/jetty_base/common/lib/j2objc-annotations-1.3.jar
/opt/zimbra/jetty_base/common/lib/jakarta.json-1.1.6.jar
/opt/zimbra/jetty_base/common/lib/java-support-8.3.1.jar
/opt/zimbra/jetty_base/common/lib/jcip-annotations-1.0-1.jar
/opt/zimbra/jetty_base/common/lib/jcl-over-slf4j-2.0.3.jar
/opt/zimbra/jetty_base/common/lib/jcommander-1.78.jar
/opt/zimbra/jetty_base/common/lib/joda-time-2.12.1.jar
/opt/zimbra/jetty_base/common/lib/json-smart-2.4.9.jar
/opt/zimbra/jetty_base/common/lib/jsr305-3.0.2.jar
/opt/zimbra/jetty_base/common/lib/lang-tag-1.7.jar
/opt/zimbra/jetty_base/common/lib/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar
/opt/zimbra/jetty_base/common/lib/metrics-core-4.2.9.jar
/opt/zimbra/jetty_base/common/lib/nimbus-jose-jwt-9.37.3.jar
/opt/zimbra/jetty_base/common/lib/oauth2-oidc-sdk-10.1.jar
/opt/zimbra/jetty_base/common/lib/opensaml-core-4.2.0.jar
/opt/zimbra/jetty_base/common/lib/opensaml-messaging-api-4.2.0.jar
/opt/zimbra/jetty_base/common/lib/opensaml-messaging-impl-4.2.0.jar
/opt/zimbra/jetty_base/common/lib/opensaml-profile-api-4.2.0.jar
/opt/zimbra/jetty_base/common/lib/opensaml-profile-impl-4.2.0.jar
/opt/zimbra/jetty_base/common/lib/opensaml-saml-api-4.2.0.jar
/opt/zimbra/jetty_base/common/lib/opensaml-saml-impl-4.2.0.jar
/opt/zimbra/jetty_base/common/lib/opensaml-security-api-4.2.0.jar
/opt/zimbra/jetty_base/common/lib/opensaml-security-impl-4.2.0.jar
/opt/zimbra/jetty_base/common/lib/opensaml-soap-api-4.2.0.jar
/opt/zimbra/jetty_base/common/lib/opensaml-soap-impl-4.2.0.jar
/opt/zimbra/jetty_base/common/lib/opensaml-storage-api-4.2.0.jar
/opt/zimbra/jetty_base/common/lib/opensaml-storage-impl-4.2.0.jar
/opt/zimbra/jetty_base/common/lib/opensaml-xmlsec-api-4.2.0.jar
/opt/zimbra/jetty_base/common/lib/opensaml-xmlsec-impl-4.2.0.jar
/opt/zimbra/jetty_base/common/lib/pac4j-cas-clientv4-5.7.10.jar
/opt/zimbra/jetty_base/common/lib/pac4j-config-5.7.10.jar
/opt/zimbra/jetty_base/common/lib/pac4j-core-5.7.10.jar
/opt/zimbra/jetty_base/common/lib/pac4j-javaee-5.7.10.jar
/opt/zimbra/jetty_base/common/lib/pac4j-oidc-5.7.10.jar
/opt/zimbra/jetty_base/common/lib/pac4j-saml-5.7.10.jar
/opt/zimbra/jetty_base/common/lib/spring-beans-5.3.24.jar
/opt/zimbra/jetty_base/common/lib/spring-core-5.3.24.jar
/opt/zimbra/jetty_base/common/lib/spring-jdbc-6.0.8.jar
/opt/zimbra/jetty_base/common/lib/spring-orm-6.0.8.jar
/opt/zimbra/jetty_base/common/lib/spring-tx-6.0.8.jar
/opt/zimbra/jetty_base/common/lib/stax2-api-3.1.4.jar
/opt/zimbra/jetty_base/common/lib/velocity-engine-core-2.3.jar
/opt/zimbra/jetty_base/common/lib/woodstox-core-6.2.6.jar
/opt/zimbra/jetty_base/common/lib/xmlsec-2.3.0.jar
/opt/zimbra/jetty_base/common/lib/xmlsectool-3.0.0.jar

%changelog
* Fri Apr 21 2023 Nguyen Van Nguyen <nguyennv1981@gmail.com> - 1.0.0-1
- Initial release 1.0.0.
* Fri Aug 25 2023 Nguyen Van Nguyen <nguyennv1981@gmail.com> - 1.0.1-1
- Initial release 1.0.1.
* Wed May 06 2026 Nguyen Van Nguyen <nguyennv1981@gmail.com> - 2.0.0-1
- Initial release 2.0.0.
