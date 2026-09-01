CREATE TABLE monthly_budgets
(
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    budget_month  VARCHAR(7)     NOT NULL,
    monthly_limit DECIMAL(38, 18) NOT NULL,
    CONSTRAINT pk_monthly_budgets PRIMARY KEY (id),
    CONSTRAINT uk_monthly_budgets_budget_month UNIQUE (budget_month),
    CONSTRAINT ck_monthly_budgets_monthly_limit_positive CHECK (monthly_limit > 0)
);

CREATE TABLE cost_records
(
    id         BIGINT          NOT NULL AUTO_INCREMENT,
    service    VARCHAR(20)     NOT NULL,
    cost       DECIMAL(38, 18) NOT NULL,
    usage_date DATE            NOT NULL,
    source     VARCHAR(30)     NOT NULL,
    CONSTRAINT pk_cost_records PRIMARY KEY (id),
    CONSTRAINT ck_cost_records_service CHECK (service IN ('EC2', 'RDS', 'S3', 'OTHER')),
    CONSTRAINT ck_cost_records_cost_non_negative CHECK (cost >= 0),
    CONSTRAINT ck_cost_records_source CHECK (source IN ('MANUAL', 'AWS_COST_EXPLORER'))
);

CREATE INDEX idx_cost_records_usage_date
    ON cost_records (usage_date);

CREATE INDEX idx_cost_records_service_usage_date
    ON cost_records (service, usage_date);

CREATE INDEX idx_cost_records_import_lookup
    ON cost_records (service, usage_date, source);
