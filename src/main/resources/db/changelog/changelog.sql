--liquibase formatted sql

--changeset artq:1 runOnChange:true
CREATE TABLE IF NOT EXISTS settings
(
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255) NOT NULL UNIQUE,
    value VARCHAR(255) NOT NULL
);

INSERT INTO settings (name, value)
VALUES ('distanceRatioThreshold', '0.9')
ON CONFLICT (name) DO UPDATE SET value = EXCLUDED.value;

CREATE TABLE IF NOT EXISTS request_content
(
    id              BIGSERIAL PRIMARY KEY,
    loan_request_id VARCHAR(255) NOT NULL UNIQUE,
    content         JSONB        NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reg_person
(
    id              BIGSERIAL PRIMARY KEY,
    loan_request_id VARCHAR(255) NOT NULL,
    first_name      VARCHAR(255),
    middle_name     VARCHAR(255),
    last_name       VARCHAR(255),
    CONSTRAINT fk_reg_person_request_content FOREIGN KEY (loan_request_id) REFERENCES request_content (loan_request_id)
);

CREATE TABLE IF NOT EXISTS verified_name
(
    id              BIGSERIAL PRIMARY KEY,
    loan_request_id VARCHAR(255) NOT NULL,
    first_name      VARCHAR(255),
    other_name      VARCHAR(255),
    surname         VARCHAR(255),
    CONSTRAINT fk_verified_name_request_content FOREIGN KEY (loan_request_id) REFERENCES request_content (loan_request_id)
);

CREATE TABLE IF NOT EXISTS account_info
(
    id                      BIGSERIAL PRIMARY KEY,
    loan_request_id         VARCHAR(255) NOT NULL,
    account_number          VARCHAR(255),
    account_status          VARCHAR(50),
    current_balance         DECIMAL(15, 5),
    date_opened             DATE,
    days_in_arrears         INTEGER,
    delinquency_code        VARCHAR(10),
    highest_days_in_arrears INTEGER,
    is_your_account         BOOLEAN,
    last_payment_amount     DECIMAL(15, 5),
    last_payment_date       DATE,
    loaded_at               DATE,
    original_amount         DECIMAL(15, 5),
    overdue_balance         DECIMAL(15, 5),
    overdue_date            DATE,
    product_type_id         INTEGER,
    CONSTRAINT fk_account_info_request_content FOREIGN KEY (loan_request_id) REFERENCES request_content (loan_request_id)
);