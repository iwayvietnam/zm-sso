#
# ***** BEGIN LICENSE BLOCK *****
# Zm SSO is the Zimbra Collaboration Open Source Edition extension for single sign-on authentication to the Zimbra Web Client.
# Copyright (C) 2020-present iWay Vietnam and/or its affiliates. All rights reserved.

# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# any later version.

# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU Affero General Public License for more details.
# You should have received a copy of the GNU Affero General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>
# ***** END LICENSE BLOCK *****

# Zimbra Single Sign On

# Written by Nguyen Van Nguyen <nguyennv1981@gmail.com>
#

all: dist/zm-sso

dist/zm-sso:
	mvn clean package

clean:
	mvn clean

rpmbuild:
	rpmbuild --build-in-place --nodebuginfo -bb rpms/zm-sso.spec

install: dist/zm-sso
	mkdir -p /opt/zimbra/lib/ext/zm-sso
	cp target/*.jar /opt/zimbra/lib/ext/zm-sso
	cp target/dependencies/*.jar /opt/zimbra/lib/ext-common
	cp conf/zm.sso.properties /opt/zimbra/conf
	su - zimbra -c '/opt/zimbra/bin/zmmailboxdctl restart'
