CREATE TABLE IF NOT EXISTS sso_session (
    sso_token        VARCHAR(255) NOT NULL,
    account_id       VARCHAR(127) NOT NULL,
    account_name     VARCHAR(255) NOT NULL,
    protocol         VARCHAR(64),
    origin_client_ip VARCHAR(64),
    remote_ip        VARCHAR(64),
    user_agent       VARCHAR(255),
    login_at         TIMESTAMP,
    logout_at        TIMESTAMP,

    PRIMARY KEY (sso_token, account_id),
    UNIQUE INDEX i_sso_token (sso_token),
    INDEX i_account_id (account_id),
    CONSTRAINT fk_sso_session_mailbox_id FOREIGN KEY (account_id) REFERENCES mailbox(account_id) ON DELETE CASCADE
) ENGINE = InnoDB;