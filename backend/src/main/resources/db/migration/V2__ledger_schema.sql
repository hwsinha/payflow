CREATE TABLE accounts (
                          id          UUID PRIMARY KEY,
                          name        VARCHAR(100) NOT NULL,
                          type        VARCHAR(20)  NOT NULL CHECK (type IN ('USER_WALLET', 'SYSTEM')),
                          currency    CHAR(3)      NOT NULL DEFAULT 'INR',
                          created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE transactions (
                              id           UUID PRIMARY KEY,
                              type         VARCHAR(20) NOT NULL CHECK (type IN ('TRANSFER', 'DEPOSIT', 'WITHDRAWAL', 'REVERSAL')),
                              description  VARCHAR(255),
                              created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ledger_entries (
                                id              BIGSERIAL PRIMARY KEY,
                                transaction_id  UUID        NOT NULL REFERENCES transactions (id),
                                account_id      UUID        NOT NULL REFERENCES accounts (id),
                                amount          BIGINT      NOT NULL CHECK (amount <> 0),
                                created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_entries_account ON ledger_entries (account_id);
CREATE INDEX idx_entries_transaction ON ledger_entries (transaction_id);

CREATE FUNCTION prevent_ledger_mutation() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'ledger_entries is append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER ledger_entries_no_update_delete
    BEFORE UPDATE OR DELETE ON ledger_entries
    FOR EACH ROW EXECUTE FUNCTION prevent_ledger_mutation();