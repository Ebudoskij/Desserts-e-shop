--liquibase formatted sql

--changeset ebudoskij:create-login-info
CREATE TABLE login_info (
    user_id BIGINT NOT NULL,
    failed_attempts SMALLINT NOT NULL DEFAULT 0,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    lock_date TIMESTAMP WITH TIME ZONE,

    CONSTRAINT pk_login_info PRIMARY KEY (user_id),
    CONSTRAINT fk_login_info_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

--rollback DROP TABLE login_info;