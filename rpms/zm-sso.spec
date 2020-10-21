Name:           zm-sso
Version:        1.0.0
Release:        1%{?dist}
Summary:        Zimbra Single Sign On (Zm SSO)

Group:          Applications/Internet
License:        AGPLv3
URL:            https://gitlab.com/iway/zm-sso
Source0:        https://gitlab.com/iway/zm-sso/-/archive/%{version}/zm-sso-%{version}.tar.gz

Requires:       zimbra-store >= 8.8
BuildRequires:  java-1.8.0-openjdk-devel ant
BuildArch:      noarch

%description
Zm SSO is the Zimbra Collaboration Open Source Edition extension for single sign-on authentication to the Zimbra Web Client.

%prep
%setup -q

%build
make

%install
mkdir -p $RPM_BUILD_ROOT/opt/zimbra/lib/ext/zm-sso
cp -R dist/*.jar $RPM_BUILD_ROOT/opt/zimbra/lib/ext/zm-sso

%posttrans
su - zimbra -c "zmmailboxdctl restart"
su - zimbra -c "zmprov fc all"

%postun
su - zimbra -c "zmmailboxdctl restart"
su - zimbra -c "zmprov fc all"

%files
/opt/zimbra/lib/ext/zm-sso/*.jar

%changelog
* Wed Sep 23 2020 Nguyen Van Nguyen <nguyennv1981@gmail.com> - 1.0.0-1
- Initial release 1.0.0 from upstream.
