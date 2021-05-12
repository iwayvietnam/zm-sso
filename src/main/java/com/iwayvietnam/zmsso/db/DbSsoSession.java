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

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.db.Db;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbResults;
import com.zimbra.cs.db.DbUtil;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Db Sso Session
 * @author Nguyen Van Nguyen <nguyennv1981@gmail.com>
 */
public final class DbSsoSession {
    private static final String SCRIPT_FILE = "sso_session.sql";
    private static final String SELECT_TABLE = "sso_session";
    private static final String SELECT_COLUMNS = "sso_token, account_id, account_name, protocol, origin_client_ip, remote_ip, user_agent, login_at, logout_at";
    private static final String INSERT_COLUMNS = "sso_token, account_id, account_name, protocol, origin_client_ip, remote_ip, user_agent, login_at";
    private static final String KEY_COLUMN = "sso_token";
    private static final String ORDER_COLUMN = "login_at";

    private final String ssoToken;
    private final String accountId;
    private final String accountName;
    private final String protocol;
    private final String originClientIp;
    private final String remoteIp;
    private final String userAgent;
    private final Timestamp loginAt;
    private final Timestamp logoutAt;

    DbSsoSession(String ssoToken, String accountId, String accountName, String protocol, String originClientIp, String remoteIp, String userAgent, Timestamp loginAt, Timestamp logoutAt) {
        this.ssoToken = ssoToken;
        this.accountId = accountId;
        this.accountName = accountName;
        this.protocol = protocol;
        this.originClientIp = originClientIp;
        this.remoteIp = remoteIp;
        this.userAgent = userAgent;
        this.loginAt = loginAt;
        this.logoutAt = logoutAt;
    }

    static DbSsoSession constructSsoSession(DbResults rs) {
        return new DbSsoSession(
            rs.getString("sso_token"),
            rs.getString("account_id"),
            rs.getString("account_name"),
            rs.getString("protocol"),
            rs.getString("origin_client_ip"),
            rs.getString("remote_ip"),
            rs.getString("user_agent"),
            (Timestamp) rs.getObject("login_at"),
            (Timestamp) rs.getObject("logout_at")
        );
    }

    public String getSsoToken() {
        return ssoToken;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getOriginClientIp() {
        return originClientIp;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Timestamp getLoginAt() {
        return loginAt;
    }

    public Timestamp getLogoutAt() {
        return logoutAt;
    }

    public static void createSsoSessionTable() throws ServiceException {
        final var cl = DbSsoSession.class.getClassLoader();
        final var inputStream = cl.getResourceAsStream(SCRIPT_FILE);
        try {
            if (inputStream != null) {
                ZimbraLog.extensions.info("Create sso session table");
                final var script = new String(IOUtils.toByteArray(inputStream));
                DbUtil.executeScript(DbPool.getConnection(), new StringReader(script));
            } else {
                throw ServiceException.NOT_FOUND(String.format("Script file '%s' not found in the classpath", SCRIPT_FILE));
            }
        } catch (final IOException e) {
            throw ServiceException.FAILURE(String.format("Script file '%s' cannot be loaded.", SCRIPT_FILE), e);
        }  catch (final SQLException e) {
            throw ServiceException.FAILURE("Create sso session table", e);
        }
    }

    public static void ssoSessionLogin(final Account account, final String ssoToken, final String protocol, final String origIp, final String remoteIp, final String userAgent) throws ServiceException {
        final var hashedToken = ByteUtil.getSHA256Digest(ssoToken.getBytes(), false);
        final var results = DbUtil.executeQuery(String.format("SELECT %s FROM %s WHERE %s = ?", KEY_COLUMN, SELECT_TABLE, KEY_COLUMN), hashedToken);
        if (!results.next() && !StringUtil.isNullOrEmpty(hashedToken)) {
            ZimbraLog.dbconn.debug("Insert sso session login for account %s with hashed token %s)", account.getId(), hashedToken);
            final var sql = String.format("INSERT INTO %s (%s) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", SELECT_TABLE, INSERT_COLUMNS);
            final var loginAt = new Timestamp(System.currentTimeMillis());
            DbUtil.executeUpdate(sql,
                hashedToken,
                account.getId(),
                account.getName(),
                protocol,
                origIp,
                remoteIp,
                userAgent,
                loginAt
            );
        }
    }

    public static String ssoSessionLogout(final String ssoToken) throws ServiceException {
        final var hashedToken = ByteUtil.getSHA256Digest(ssoToken.getBytes(), false);
        final var results = DbUtil.executeQuery(String.format("SELECT %s FROM %s WHERE %s = ?", SELECT_COLUMNS, SELECT_TABLE, KEY_COLUMN), hashedToken);
        if (results.next() && results.isNull("logout_at")) {
            ZimbraLog.dbconn.debug("Update sso session logout with hashed token %s", hashedToken);
            final var sql = String.format("UPDATE %s SET logout_at = ? WHERE %s = ?", SELECT_TABLE, KEY_COLUMN);
            final var logoutAt = new Timestamp(System.currentTimeMillis());
            DbUtil.executeUpdate(sql, logoutAt, hashedToken);
            return results.getString("account_id");
        }
        return null;
    }

    public static List<DbSsoSession> GetAllSsoSessions(final Integer offset, final Integer limit) throws ServiceException {
        final var sessions = new ArrayList<DbSsoSession>();
        final var joinQuery = new StringBuilder(String.format("SELECT %s AS ref_key FROM %s ORDER BY %s DESC", KEY_COLUMN, SELECT_TABLE, ORDER_COLUMN));
        if (Db.supports(Db.Capability.LIMIT_CLAUSE) && limit != null && limit > 0) {
            if (offset != null && offset > 0) {
                joinQuery.append(" ").append(Db.getInstance().limit(offset, limit));
            }
            else {
                joinQuery.append(" ").append(Db.getInstance().limit(limit));
            }
        }
        final var query = String.format(
            "SELECT %s FROM %s INNER JOIN (%s) AS joinQuery ON joinQuery.ref_key = %s.%s",
            SELECT_COLUMNS,
            SELECT_TABLE,
            joinQuery,
            SELECT_TABLE,
            KEY_COLUMN
        );
        final var rs = DbUtil.executeQuery(query);
        while (rs.next()) {
            sessions.add(constructSsoSession(rs));
        }
        return sessions;
    }

    public static Integer CountAllSsoSessions() throws ServiceException {
        final var rs = DbUtil.executeQuery(String.format("SELECT COUNT(*) FROM %s", SELECT_TABLE));
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }
}
