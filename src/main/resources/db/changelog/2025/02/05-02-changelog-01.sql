CREATE TABLE transactions_accept
(
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY UNIQUE,
    client_id UUID NOT NULL,
    from_account_id UUID NOT NULL,
    to_account_id UUID NOT NULL,
    transaction_id UUID NOT NULL,
    created_at timestamptz NOT NULL,
    transaction_amount DECIMAL NOT NULL,
    from_account_balance DECIMAL NOT NULL,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX transactions_created_idx ON transactions_accept (created_at);