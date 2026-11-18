package com.hwsinha.payflow;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class LedgerSchemaTest {

    @Autowired
    JdbcTemplate jdbc;

    private UUID insertAccount(String name, String type) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO accounts (id, name, type) VALUES (?, ?, ?)", id, name, type);
        return id;
    }

    private UUID insertTransaction(String type) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO transactions (id, type) VALUES (?, ?)", id, type);
        return id;
    }

    private void insertEntry(UUID transactionId, UUID accountId, long amount) {
        jdbc.update(
                "INSERT INTO ledger_entries (transaction_id, account_id, amount) VALUES (?, ?, ?)",
                transactionId, accountId, amount);
    }

    @Test
    void transferEntriesAreStoredAndSumToZero() {
        UUID a = insertAccount("A", "USER_WALLET");
        UUID b = insertAccount("B", "USER_WALLET");
        UUID tx = insertTransaction("TRANSFER");

        insertEntry(tx, a, -10000);
        insertEntry(tx, b, 10000);

        Integer rows = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ledger_entries WHERE transaction_id = ?", Integer.class, tx);
        Long sum = jdbc.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM ledger_entries WHERE transaction_id = ?", Long.class, tx);

        assertThat(rows).isEqualTo(2);
        assertThat(sum).isZero();
    }

    @Test
    void updateIsRejected(){
        UUID a = insertAccount("A", "USER_WALLET");
        UUID b = insertAccount("B", "USER_WALLET");
        UUID tx = insertTransaction("TRANSFER");

        insertEntry(tx, a, -10000);
        insertEntry(tx, b, 10000);

        List<Map<String, Object>> before = jdbc.queryForList(
                "SELECT * FROM ledger_entries WHERE transaction_id = ? ORDER BY id", tx);

        assertThatThrownBy(() -> jdbc.update("UPDATE ledger_entries SET amount = ? WHERE transaction_id = ?", 999, tx))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("append-only");

        List<Map<String, Object>> after = jdbc.queryForList(
                "SELECT * FROM ledger_entries WHERE transaction_id = ? ORDER BY id", tx);

        assertThat(after).isEqualTo(before);
    }
    @Test
    void deleteIsRejected(){
        UUID a = insertAccount("A", "USER_WALLET");
        UUID b = insertAccount("B", "USER_WALLET");
        UUID tx = insertTransaction("TRANSFER");

        insertEntry(tx, a, -10000);
        insertEntry(tx, b, 10000);

        List<Map<String, Object>> before = jdbc.queryForList(
                "SELECT * FROM ledger_entries WHERE transaction_id = ? ORDER BY id", tx);

        assertThatThrownBy(() -> jdbc.update("DELETE FROM ledger_entries WHERE transaction_id = ?", tx))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("append-only");

        List<Map<String, Object>> after = jdbc.queryForList(
                "SELECT * FROM ledger_entries WHERE transaction_id = ? ORDER BY id", tx);

        assertThat(after).isEqualTo(before);
    }
}
