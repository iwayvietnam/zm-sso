Single sign on with Apereo Central Authentication Service (CAS)
===============================================================
[CAS](https://apereo.github.io/cas/) is an enterprise multilingual single sign-on solution for the web and attempts to be a comprehensive platform for your authentication and authorization needs.
The primary implementation of the protocol is an open-source Java server component by the same name hosted here, with support for a plethora of additional authentication protocols and features.

## Install CAS

### Requirement
* JDK version 11 or newer.

### Clone `cas overlay template` & `cas-management-overlay`
```shell
mkdir -p /opt/cas
cd /opt/cas
git clone https://github.com/apereo/cas-overlay-template.git
git clone https://github.com/apereo/cas-management-overlay.git
```

### Deployment configuration
* Using a text editor to open `/opt/cas/cas-overlay-template/gradle.properties` file.
* Set **cas.version** to the latest stable version (current is 6.3.4). Ex: `cas.version=6.3.4`.
* Using a text editor to open `/opt/cas/cas-overlay-template/build.gradle` file.
* Add following content under `dependencies` block
```
implementation "org.apereo.cas:cas-server-support-ldap:${casServerVersion}"
implementation "org.apereo.cas:cas-server-support-saml-idp:${casServerVersion}"
implementation "org.apereo.cas:cas-server-support-oidc:${casServerVersion}"
implementation "org.apereo.cas:cas-server-support-yaml-service-registry:${casServerVersion}"
```
