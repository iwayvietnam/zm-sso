package com.iwayvietnam.zmsso;

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

public final class DbSsoSession {
    public static void createSsoSessionTable() throws ServiceException {
        ZimbraLog.extensions.debug("createSsoSessionTable()");
        File file = new File(LC.zimbra_db_directory.value() + "/sso_session.sql");
        DbPool.DbConnection conn = DbPool.getConnection();
        try {
            String script = new String(ByteUtil.getContent(file));
            DbUtil.executeScript(conn, new StringReader(script));
        } catch (SQLException | IOException ex) {
            throw ServiceException.FAILURE("createSsoSessionTable()", ex);
        }
    }

    public static void ssoSessionLogin(Account account, String ssoToken, String protocol, String origIp, String remoteIp, String userAgent) throws ServiceException {
        String hashedToken = ByteUtil.getSHA256Digest(ssoToken.getBytes(), false);
        ZimbraLog.extensions.debug(String.format("ssoSessionLogin(%s, %s)", account.getId(), hashedToken));
        DbResults results = DbUtil.executeQuery("SELECT account_id FROM sso_session WHERE sso_token = ?", hashedToken);
        if (!results.next() && !StringUtil.isNullOrEmpty(hashedToken)) {
            String sql = "INSERT INTO sso_session (sso_token, account_id, account_name, protocol, origin_client_ip, remote_ip, user_agent, login_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            int loginAt = (int) (System.currentTimeMillis() / 1000L);
            DbUtil.executeUpdate(sql, hashedToken, account.getId(), account.getName(), protocol, origIp, remoteIp, userAgent, loginAt);
        }
    }

    public static String ssoSessionLogout(String ssoToken) throws ServiceException {
        String hashedToken = ByteUtil.getSHA256Digest(ssoToken.getBytes(), false);
        ZimbraLog.extensions.debug(String.format("ssoSessionLogout(%s)", hashedToken));
        DbResults results = DbUtil.executeQuery("SELECT account_id, logout_at FROM sso_session WHERE sso_token = ?", hashedToken);
        if (results.next()) {
            if (results.isNull("logout_at")) {
                String sql = "UPDATE sso_session SET logout_at = ? WHERE sso_token = ?";
                int logoutAt = (int) (System.currentTimeMillis() / 1000L);
                DbUtil.executeUpdate(sql, logoutAt, hashedToken);
                return results.getString("account_id");
            }
        }
        return null;
    }
}
