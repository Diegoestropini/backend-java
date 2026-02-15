CREATE TABLE branch (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    address VARCHAR(255) NOT NULL
);

CREATE TABLE financial_product (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('AHORRO', 'CREDITO', 'INVERSION'))
);

CREATE TABLE transaction_record (
    id BIGSERIAL PRIMARY KEY,
    tx_date DATE NULL,
    branch_id BIGINT NULL REFERENCES branch(id),
    product_id BIGINT NULL REFERENCES financial_product(id),
    raw_branch_code VARCHAR(20) NOT NULL,
    raw_product_code VARCHAR(20) NOT NULL,
    amount NUMERIC(18,2) NULL,
    tx_type VARCHAR(20) NULL CHECK (tx_type IN ('INGRESO', 'EGRESO')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDIENTE', 'CONCILIADA', 'RECHAZADA')),
    rejection_reason VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE daily_close (
    id BIGSERIAL PRIMARY KEY,
    close_date DATE NOT NULL,
    branch_id BIGINT NOT NULL REFERENCES branch(id),
    product_id BIGINT NOT NULL REFERENCES financial_product(id),
    total_income NUMERIC(18,2) NOT NULL,
    total_expense NUMERIC(18,2) NOT NULL,
    net_total NUMERIC(18,2) NOT NULL,
    tx_count BIGINT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_daily_close UNIQUE (close_date, branch_id, product_id)
);

CREATE INDEX idx_transaction_record_tx_date_status ON transaction_record(tx_date, status);
CREATE INDEX idx_transaction_record_branch_tx_date ON transaction_record(branch_id, tx_date);
CREATE INDEX idx_transaction_record_product_tx_date ON transaction_record(product_id, tx_date);
CREATE INDEX idx_transaction_record_raw_codes ON transaction_record(raw_branch_code, raw_product_code);
CREATE INDEX idx_daily_close_date ON daily_close(close_date);
