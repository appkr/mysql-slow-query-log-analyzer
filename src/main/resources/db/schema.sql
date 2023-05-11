CREATE TABLE IF NOT EXISTS Z_SLOW_QUERY_LOG_REPORTS
(
    id      BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    md5hash VARCHAR(40) NOT NULL,
    PRIMARY KEY (id),
    KEY IDX_MD5HASH(md5hash)
);

CREATE TABLE IF NOT EXISTS Z_SLOW_QUERY_LOGS
(
    id              BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    report_id       INT(10) UNSIGNED NOT NULL,
    log_time        TIMESTAMP(6) NOT NULL,
    query_user      VARCHAR(40) NOT NULL DEFAULT '',
    query_host      VARCHAR(40) NOT NULL DEFAULT '',
    query_id        VARCHAR(10) NOT NULL DEFAULT '',
    query_time      VARCHAR(40) NOT NULL DEFAULT 'PT0S',
    lock_time       VARCHAR(40) NOT NULL DEFAULT 'PT0S',
    rows_sent       INT(10) UNSIGNED NOT NULL DEFAULT 0,
    rows_examined   INT(10) UNSIGNED NOT NULL DEFAULT 0,
    qeury_executed  TEXT NOT NULL,
    PRIMARY KEY (id),
    KEY IDX_REPORT_ID(report_id)
);

CREATE TABLE IF NOT EXISTS Z_QUERY_EXECUTION_PLANS
(
    id                BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
    log_id            BIGINT(20) UNSIGNED NOT NULL,
    plan_id           VARCHAR(10) NOT NULL DEFAULT '',
    select_type       VARCHAR(40) NOT NULL DEFAULT '',
    plan_table        VARCHAR(100) NOT NULL DEFAULT '',
    partitions        VARCHAR(40) NOT NULL DEFAULT '',
    plan_type         VARCHAR(40) NOT NULL DEFAULT '',
    possible_keys     TEXT NOT NULL,
    selected_key      VARCHAR(255) NOT NULL DEFAULT '',
    key_len           VARCHAR(10) NOT NULL DEFAULT '0',
    used_join_columns VARCHAR(255) NOT NULL DEFAULT '',
    estimated_rows    VARCHAR(10) NOT NULL DEFAULT '0',
    filtered          VARCHAR(10) NOT NULL DEFAULT '0',
    extra             TEXT NOT NULL,
    PRIMARY KEY (id),
    KEY IDX_LOG_ID(log_id)
);
