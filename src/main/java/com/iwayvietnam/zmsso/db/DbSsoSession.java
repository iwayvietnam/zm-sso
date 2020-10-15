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
package com.iwayvietnam.zmsso.db;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbResults;
import com.zimbra.cs.db.DbUtil;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

/**
 * Db Sso Session
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public final class DbSsoSession {
    public static void createSsoSessionTable() throws ServiceException {
        ZimbraLog.dbconn.debug("Create sso session table");
        final File file = new File(LC.zimbra_db_directory.value() + "/sso_session.sql");
        final DbPool.DbConnection conn = DbPool.getConnection();
        try {
            final String script = new String(ByteUtil.getContent(file));
            DbUtil.executeScript(conn, new StringReader(script));
        } catch (SQLException | IOException ex) {
            throw ServiceException.FAILURE("Create sso session table", ex);
        }
    }

    public static void ssoSessionLogin(final Account account, final String ssoToken, final String protocol, final String origIp, final String remoteIp, final String userAgent) throws ServiceException {
        final String hashedToken = ByteUtil.getSHA256Digest(ssoToken.getBytes(), false);
        ZimbraLog.dbconn.debug(String.format("Insert sso session login for account %s with hashed token %s)", account.getId(), hashedToken));
        final DbResults results = DbUtil.executeQuery("SELECT account_id FROM sso_session WHERE sso_token = ?", hashedToken);
        if (!results.next() && !StringUtil.isNullOrEmpty(hashedToken)) {
            final String sql = "INSERT INTO sso_session (sso_token, account_id, account_name, protocol, origin_client_ip, remote_ip, user_agent, login_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            final int loginAt = (int) (System.currentTimeMillis() / 1000L);
            DbUtil.executeUpdate(sql, hashedToken, account.getId(), account.getName(), protocol, origIp, remoteIp, userAgent, loginAt);
        }
    }

    public static String ssoSessionLogout(final String ssoToken) throws ServiceException {
        final String hashedToken = ByteUtil.getSHA256Digest(ssoToken.getBytes(), false);
        ZimbraLog.dbconn.debug(String.format("Update sso session logout with hashed token %s", hashedToken));
        final DbResults results = DbUtil.executeQuery("SELECT account_id, logout_at FROM sso_session WHERE sso_token = ?", hashedToken);
        if (results.next()) {
            if (results.isNull("logout_at")) {
                final String sql = "UPDATE sso_session SET logout_at = ? WHERE sso_token = ?";
                final int logoutAt = (int) (System.currentTimeMillis() / 1000L);
                DbUtil.executeUpdate(sql, logoutAt, hashedToken);
                return results.getString("account_id");
            }
        }
        return null;
    }
}
