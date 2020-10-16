Zimbra Single Sign On (Zm SSO)
=========================================
Zm SSO is the Zimbra Collaboration Open Source Edition extension for single sign-on authentication to the Zimbra Web Client.  
Copyright (C) 2020-present iWay Vietnam and/or its affiliates. All rights reserved.

* Using framework: [pac4j](https://www.pac4j.org) is an easy and powerful security engine for Java to authenticate users,
get their profiles and manage authorizations in order to secure web applications and web services.
* Supported authentication mechanisms: [SAML](http://www.pac4j.org/docs/clients/saml.html) -
[CAS](http://www.pac4j.org/docs/clients/cas.html) -
[OpenID Connect](http://www.pac4j.org/docs/clients/openid-connect.html)

## Building Java extension
### Requirement
* JDK 8 or newer.
* Apache Maven 3.5 or newer for Maven build.
* Apache Ant 1.x for Ant build.

### Setting up your build system
* On Fedora or CentOS 8.x or Red Hat EL 7.x
```shell script
dnf -y install java-8-openjdk java-8-openjdk-devel maven ant git
```
* On CentOS 7.x or Red Hat EL 7.x
```shell script
yum -y install java-8-openjdk java-8-openjdk-devel maven ant git
```
* On Debian or Ubuntu
```shell script
apt install -y openjdk-8-jdk maven ant git
```

### Clone code from git repository
```shell script
mkdir -p ~/projects/zimbra
cd ~/projects/zimbra
git clone git@gitlab.com:iway/zm-sso.git
```

### Install zimbra dependency to Maven repository
```shell script
cd ~/projects/zimbra/zm-sso
mvn install:install-file -Dfile=lib/zimbracommon.jar -DgroupId=com.zimbra -DartifactId=zimbraCommon -Dversion="8.8.15_GA_3928" -Dpackaging=jar
mvn install:install-file -Dfile=lib/zimbrasoap.jar -DgroupId=com.zimbra -DartifactId=zimbraSoap -Dversion="8.8.15_GA_3928" -Dpackaging=jar
mvn install:install-file -Dfile=lib/zimbrastore.jar -DgroupId=com.zimbra -DartifactId=zimbraStore -Dversion="8.8.15_GA_3928" -Dpackaging=jar
mvn install:install-file -Dfile=lib/zimbraclient.jar -DgroupId=com.zimbra -DartifactId=zimbraClient -Dversion="8.8.15_GA_3928" -Dpackaging=jar
```

### Build jar file
```shell script
cd ~/projects/zimbra/zm-sso
mvn clean package
```
The output should be like this:
```
--- maven-jar-plugin:3.0.2:jar (default-jar) @ zm-hab ---
Building jar: ~/projects/zimbra/zm-sso/target/zm-sso-1.0.0-1.jar
```

### Build rpm package
```shell script
cd ~/projects/zimbra/zm-sso
make rpmbuild
```
The output should be like this:
```
Wrote: ~/rpmbuild/RPMS/noarch/zm-sso-1.0.0-1.fc32.noarch.rpm
```

Licensing
=========
Zm SSO is licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3.
See [LICENSE](LICENSE) for the full license text.
