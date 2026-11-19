package com.hwsinha.payflow.ledger;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends Repository<LedgerEntry, Long> {
    LedgerEntry save(LedgerEntry entry);
    List<LedgerEntry> findByTransactionId(UUID transactionId);

    @Query("select coalesce(sum(e.amount), 0L) from LedgerEntry e where e.accountId = :accountId")
    long balanceOf(@Param("accountId") UUID accountId);
}
