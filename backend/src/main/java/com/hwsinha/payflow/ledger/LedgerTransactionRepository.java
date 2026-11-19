package com.hwsinha.payflow.ledger;

import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface LedgerTransactionRepository extends Repository<LedgerTransaction, UUID> {
    LedgerTransaction save(LedgerTransaction tx);
}
