Zimbra Single Sign On (Zm SSO)
=========================================
Zm SSO là tiện ích mở rộng dành cho phiên bản mã nguồn mở của Zimbra Collaboration (Zimbra Collaboration Open Source Edition), cho phép xác thực đăng nhập một lần (single sign-on) vào Zimbra Web Client.

Bản quyền (C) 2020-nay thuộc về iWay Vietnam và/hoặc các đơn vị liên kết. Mọi quyền được bảo lưu.

* Sử dụng framework: [pac4j](https://www.pac4j.org) là một công cụ bảo mật mạnh mẽ và dễ sử dụng dành cho Java, giúp xác thực người dùng, lấy thông tin hồ sơ người dùng và quản lý quyền truy cập nhằm bảo mật các ứng dụng web cũng như dịch vụ web.
* Các cơ chế xác thực được hỗ trợ: [SAML](http://www.pac4j.org/docs/clients/saml.html) -
  [CAS](http://www.pac4j.org/docs/clients/cas.html) -
  [OpenID Connect](http://www.pac4j.org/docs/clients/openid-connect.html)

## Xây dựng tiện ích mở rộng Java
### Yêu cầu
* JDK 17 hoặc mới hơn.
* Apache Maven 3.5 hoặc mới hơn (đối với bản dựng Maven).
* Apache Ant 1.x (đối với bản dựng Ant).
* rpm-build (để xây dựng gói rpm).

### Thiết lập hệ thống xây dựng của bạn
* Trên Fedora, Rocky Linux hoặc AlmaLinux

```shell
$ dnf -y install java-17-openjdk java-17-openjdk-devel maven ant git make rpmdevtools rpm-build
```

* Trên Debian hoặc Ubuntu

```shell
$ apt install -y openjdk-17-jdk maven ant git make
```

### Sao chép mã từ kho lưu trữ Git

```shell
$ mkdir -p ~/projects/zimbra
$ cd ~/projects/zimbra
$ git clone https://github.com/iwayvietnam/zm-sso.git
```

### Xây dựng tệp jar bằng Maven
```shell
$ cd ~/projects/zimbra/zm-sso
$ mvn clean package
```
Kết quả đầu ra sẽ như sau:
```
--- jar:3.5.0:jar (default-jar) @ zm-sso ---
Building jar: ~/projects/zimbra/zm-sso/target/zm-sso-2.0.0-1.jar
```

### Tạo file jar bằng Ant
```shell
$ cd ~/projects/zimbra/zm-sso
$ ant jar
```

Kết quả đầu ra sẽ như sau:

```
[jar] Building jar: ~/projects/zimbra/zm-sso/dist/zm-sso-2.0.0-1.jar
```

### Xây dựng gói rpm

```shell
cd ~/projects/zimbra/zm-sso
make rpmbuild
```

Kết quả đầu ra sẽ như sau:

```
Wrote: ~/rpmbuild/RPMS/noarch/zm-sso-2.0.0-1.el9.noarch.rpm
```

## Cài đặt
### Cài đặt tiện ích mở rộng jar
* Sao chép tệp mở rộng .jar vào máy chủ Zimbra

```shell
cd ~/projects/zimbra/zm-sso
ssh root@zimbra.server "mkdir -p /opt/zimbra/lib/ext/zm-sso"
scp target/*.jar root@zimbra.server:/opt/zimbra/lib/ext/zm-sso
scp target/dependencies/*.jar root@zimbra.server:/opt/zimbra/jetty_base/common/lib
scp conf/zm.sso.properties root@zimbra.server:/opt/zimbra/conf
ssh root@zimbra.server "mv /opt/zimbra/jetty_base/common/lib/commons-codec-1.14.jar /opt/zimbra/jetty_base/common/lib/commons-codec-1.14.jar.bak"
```

* Khởi động lại hộp thư để tải tiện ích mở rộng.

```shell
ssh root@zimbra.server "su - zimbra -c '/opt/zimbra/bin/zmmailboxdctl restart'"
```

### Cài đặt gói rpm

```shell
ssh root@zimbra.server "mkdir -p /tmp/zimbra"
scp ~/rpmbuild/RPMS/noarch/zm-sso-2.0.0-1.el9.noarch.rpm root@zimbra.server:/tmp/zimbra
ssh root@zimbra.server "rpm -Uvh /tmp/zimbra/zm-sso-2.0.0-1.el9.noarch.rpm"
```

## Cấu hình
Các thiết lập được tải từ tệp **zm.sso.properties**. Vị trí của tệp này là **/opt/zimbra/conf/zm.sso.properties**

### Cấu hình client mặc định
* Sử dụng trình soạn thảo văn bản để mở tệp **zm.sso.properties** nằm trong thư mục **/opt/zimbra/conf**. Ví dụ: `vi /opt/zimbra/conf/zm.sso.properties`
* Chỉ định client pac4j mặc định bằng cách thiết lập giá trị cho khóa **sso.defaultClient**. Ví dụ: `sso.defaultClient = SAML2Client`

### Cấu hình điểm cuối callback (callback endpoint)
Để xử lý xác thực, cần có một điểm cuối callback (callback endpoint) nhằm tiếp nhận các lệnh gọi callback từ máy chủ định danh (identity server) và hoàn tất quy trình đăng nhập.

**Cấu hình**:
* Sử dụng trình soạn thảo văn bản để mở tệp **zm.sso.properties** nằm trong thư mục **/opt/zimbra/conf**.
* Chỉ định điểm cuối callback bằng cách thiết lập giá trị cho khóa **sso.callbackUrl**. Đường dẫn của điểm cuối có thể là:
* **/service/extension/sso/callback** (sử dụng client mặc định, được chỉ định trong `sso.defaultClient`). Ví dụ: `sso.callbackUrl = https://mail.zimbra-server.com/service/extension/sso/callback`
* **/service/extension/saml/callback** (sử dụng client SAML). Ví dụ: `saml.callbackUrl = https://mail.zimbra-server.com/service/extension/saml/callback`
* **/service/extension/cas/callback** (sử dụng client CAS). Ví dụ: `cas.callbackUrl = https://mail.zimbra-server.com/service/extension/cas/callback`
* **/service/extension/oidc/callback** (sử dụng client OpenID Connect). Ví dụ: `oidc.callbackUrl = https://mail.zimbra-server.com/service/extension/oidc/callback`
* Chỉ định việc lưu hồ sơ (profile) vào phiên làm việc (session) bằng cách thiết lập giá trị cho khóa **sso.saveInSession**.
* Chỉ định hỗ trợ nhiều hồ sơ (multi-profile) bằng cách thiết lập giá trị cho khóa **sso.multiProfile**.
* Chỉ định yêu cầu làm mới phiên làm việc (session) bằng cách thiết lập giá trị cho khóa **sso.renewSession**.

### Cấu hình điểm cuối đăng xuất (logout endpoint)
Để xử lý việc đăng xuất, cần có một điểm cuối đăng xuất (logout endpoint) nhằm thực hiện:

* Đăng xuất cục bộ (local logout) bằng cách xóa các hồ sơ pac4j khỏi phiên làm việc (session).
* Đăng xuất tập trung (central logout) bằng cách gọi đến điểm cuối đăng xuất của nhà cung cấp định danh (identity provider). Đây chính là quy trình Đăng xuất đơn nhất (Single LogOut - SLO).

**Cấu hình**:
* Sử dụng trình soạn thảo văn bản để mở tệp **zm.sso.properties** nằm trong thư mục **/opt/zimbra/conf**. * **sso.localLogout**: Cho biết liệu có cần thực hiện đăng xuất cục bộ (local logout) hay không.
* **sso.destroySession**: Xác định xem có cần hủy phiên làm việc trên web (web session) trong quá trình đăng xuất cục bộ hay không.
* **sso.centralLogout**: Xác định xem có cần thực hiện đăng xuất tập trung (central logout) hay không.
* **sso.postLogoutURL**: Xác định URL chuyển hướng sau khi đăng xuất, đưa người dùng từ máy chủ IdP quay trở lại Zimbra. Theo mặc định, ở bước cuối cùng của quy trình đăng xuất do SP (Service Provider) khởi tạo, người dùng sẽ thấy một trang trống.

### Cấu hình với bất kỳ nhà cung cấp danh tính (identity provider) SAML nào sử dụng giao thức SAML v2.0.
**Trước tiên**, nếu chưa có, bạn cần tạo một kho khóa (keystore) để phục vụ các thao tác ký và mã hóa. Ví dụ:

```shell
mkdir -p /opt/zimbra/conf/saml
keytool -genkeypair -alias samlkey -keypass samlpasswd -keystore /opt/zimbra/conf/saml/keystore.jks -storepass samlpasswd -keyalg RSA -keysize 2048 -validity 3650
```

**Cấu hình**:

* Sử dụng trình soạn thảo văn bản để mở tệp **zm.sso.properties** nằm trong thư mục **/opt/zimbra/conf**.
* **saml.keystorePath**: Xác định vị trí lưu trữ khóa (keystore). Đây là giá trị của tùy chọn `-keystore` được sử dụng khi tạo keystore, với tiền tố là **file:**.
Ví dụ: `saml.keystorePath = file:/opt/zimbra/conf/saml/keystore.jks`
* **saml.keystorePassword**: Xác định mật khẩu cho keystore. Đây là giá trị của tùy chọn `-storepass` được sử dụng khi tạo keystore.
* **saml.privateKeyPassword**: Xác định mật khẩu cho khóa (key). Đây là giá trị của tùy chọn `-keypass` được sử dụng khi tạo keystore.
* **saml.keystoreAlias**: Xác định bí danh (alias) cho keystore. Đây là giá trị của tùy chọn `-alias` được sử dụng khi tạo keystore.
* **saml.identityProviderMetadataPath**: Xác định vị trí tài nguyên trỏ đến siêu dữ liệu (metadata) của Nhà cung cấp danh tính (IdP).
Ví dụ: `saml.identityProviderMetadataPath = https://samltest.id/saml/idp`
* **saml.serviceProviderEntityId**: Xác định ID thực thể (entity ID) của ứng dụng của bạn (đóng vai trò là Nhà cung cấp dịch vụ - Service Provider).
Ví dụ: `saml.serviceProviderEntityId = https://mail.zimbra-server.com/service/extension/saml/metadata`

### Cấu hình đăng nhập thông qua máy chủ CAS.
* Sử dụng trình soạn thảo văn bản để mở tệp **zm.sso.properties** nằm trong thư mục **/opt/zimbra/conf**.
* **cas.loginUrl**: Xác định URL đăng nhập của máy chủ CAS. Ví dụ: `cas.loginUrl = https://cas.cas-server.com/cas/login`
* **cas.protocol**: Xác định giao thức CAS mà bạn muốn sử dụng. Ví dụ: `cas.protocol = CAS20`

### Cấu hình đăng nhập sử dụng giao thức OpenID Connect phiên bản 1.0. * Sử dụng trình soạn thảo văn bản để mở tệp **zm.sso.properties** nằm trong thư mục **/opt/zimbra/conf**.
* **oidc.discoveryUri**: Xác định URI khám phá (discovery URI) để truy xuất siêu dữ liệu (metadata) của nhà cung cấp OpenID (OP). Ví dụ: `oidc.discoveryUri = https://demo.c2id.com/.well-known/openid-configuration`
* **oidc.id**: Xác định định danh khách hàng (client identifier) ​​cho OpenID.
* **oidc.secret**: Xác định khóa bí mật khách hàng (client secret) cho OpenID.
* **oidc.scope**: Xác định phạm vi (scope) cho khách hàng OpenID.

### Thay thế các URL đăng nhập và đăng xuất
* Thực thi các lệnh sau với tư cách người dùng Zimbra để cấu hình tên miền (domain):

```shell
# SSO login by using default client. Specified in sso.defaultClient
zmprov md yourdomain.com zimbraWebClientLoginURL https://mail.zimbra-server.com/service/extension/sso/login
# or SSO login by using SAML client
# zmprov md yourdomain.com zimbraWebClientLoginURL https://mail.zimbra-server.com/service/extension/saml/login
# or SSO login by using CAS client
# zmprov md yourdomain.com zimbraWebClientLoginURL https://mail.zimbra-server.com/service/extension/cas/login
# or SSO login by using OpenID Connect client
# zmprov md yourdomain.com zimbraWebClientLoginURL https://mail.zimbra-server.com/service/extension/oidc/login
# Specified logout URL
zmprov md yourdomain.com zimbraWebClientLogoutURL https://mail.zimbra-server.com/service/extension/sso/logout
```

* Thực thi các lệnh sau với người dùng `zimbra` để thực hiện cấu hình toàn cục:

```shell
# SSO login by using default client. Specified in sso.defaultClient
zmprov mcf zimbraWebClientLoginURL https://mail.zimbra-server.com/service/extension/sso/login
# or SSO login by using SAML client
# zmprov mcf zimbraWebClientLoginURL https://mail.zimbra-server.com/service/extension/saml/login
# or SSO login by using CAS client
# zmprov mcf zimbraWebClientLoginURL https://mail.zimbra-server.com/service/extension/cas/login
# or SSO login by using OpenID Connect client
# zmprov mcf zimbraWebClientLoginURL https://mail.zimbra-server.com/service/extension/oidc/login
# Specified logout URL
zmprov mcf zimbraWebClientLogoutURL https://mail.zimbra-server.com/service/extension/sso/logout
```

* Thực thi lệnh sau với người dùng `zimbra` để khởi động lại máy chủ Zimbra: `zmcontrol restart`

### Nhập chứng chỉ SSL không đáng tin cậy vào tệp cacerts
Việc này chủ yếu nhằm cho phép sử dụng các chứng chỉ SSL không đáng tin cậy từ các nguồn dữ liệu bên ngoài.
* Xuất chứng chỉ SSL không đáng tin cậy ra tệp:

```shell
openssl s_client -servername idp.server.com -connect idp.server.com:443 </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' >/path/to/cert.pem
```

* Thực thi các lệnh sau với người dùng Zimbra:

```shell
zmcertmgr addcacert /path/to/cert.pem
zmmailboxdctl restart
```

### Thêm tên máy chủ của nhà cung cấp danh tính vào danh sách bỏ qua kiểm tra referer để phòng chống CSRF

```shell
zmprov -l mcf +zimbraCsrfAllowedRefererHosts idp.server.com
zmmailboxdctl restart
```

## Hướng dẫn
* [Đăng nhập một lần (Single Sign-On) với WSO2 Identity Server](docs/wso2-is.md)
* [Đăng nhập một lần (Single Sign-On) với Keycloak](docs/keycloak.md)
* [Đăng nhập một lần (Single Sign-On) với Azure AD B2C](docs/azure-ad-b2c.md)
* [Đăng nhập một lần (Single Sign-On) với Apereo Central Authentication Service](docs/cas.md)

Giấy phép
=========
Zm SSO được cấp phép theo Giấy phép Công cộng GNU Affero (GNU AFFERO GENERAL PUBLIC LICENSE), Phiên bản 3.
Xem [LICENSE](LICENSE) để biết toàn văn giấy phép.
