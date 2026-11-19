package com.hwsinha.payflow;

import com.hwsinha.payflow.ledger.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class LedgerRepositoryTest {

    @Autowired
    AccountRepository accounts;
    @Autowired
    LedgerTransactionRepository transactions;
    @Autowired
    LedgerEntryRepository entries;

    private UUID newAccount(String name) {
        return accounts.save(new Account(name, AccountType.USER_WALLET)).getId();
    }

    private UUID newTransaction(TransactionType type) {
        return transactions.save(new LedgerTransaction(type, "test")).getId();
    }

    private void post(UUID txId, UUID accountId, long amount) {
        entries.save(new LedgerEntry(txId, accountId, amount));
    }

    @Test
    void emptyAccountHasZeroBalance() {
        UUID a = newAccount("A");

        assertThat(entries.balanceOf(a)).isZero();
    }

    @Test
    void balanceIsSumOfEntries() {
        UUID a = newAccount("A");
        UUID b = newAccount("B");
        UUID tx1 = newTransaction(TransactionType.TRANSFER);
        UUID tx2 = newTransaction(TransactionType.TRANSFER);

        post(tx1, a, 10000);
        post(tx1, b, -10000);
        post(tx2, a, -2500);
        post(tx2, b, 2500);

        assertThat(entries.balanceOf(a)).isEqualTo(7500L);
    }

    @Test
    void zeroAmountIsRejected() {
        assertThatThrownBy(() -> new LedgerEntry(UUID.randomUUID(), UUID.randomUUID(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-zero");
    }

    @Test
    void balancesAreIsolatedPerAccount(){
        UUID a = newAccount("A");
        UUID b = newAccount("B");
        UUID c = newAccount("C");
        UUID tx1 = newTransaction(TransactionType.TRANSFER);
        UUID tx2 = newTransaction(TransactionType.TRANSFER);

        post(tx1, a, -10000);
        post(tx1, b, 10000);
        post(tx2, c, 4000);
        post(tx2, b, -4000);

        assertThat(entries.balanceOf(a)).isEqualTo(-10000L);
        assertThat(entries.balanceOf(b)).isEqualTo(6000L);
        assertThat(entries.balanceOf(c)).isEqualTo(4000L);
    }

    @Test
    void transferBalancesNetToZero(){
        UUID a = newAccount("A");
        UUID b = newAccount("B");
        UUID tx1 = newTransaction(TransactionType.TRANSFER);
        UUID tx2 = newTransaction(TransactionType.TRANSFER);

        post(tx1, a, -10000);
        post(tx1, b, 10000);
        assertThat((entries.balanceOf(a))+entries.balanceOf(b)).isEqualTo(0L);
        post(tx2, a, 4000);
        post(tx2, b, -4000);
        assertThat((entries.balanceOf(a))+entries.balanceOf(b)).isEqualTo(0L);
    }
}
