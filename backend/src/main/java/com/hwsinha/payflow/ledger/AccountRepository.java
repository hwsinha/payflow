package com.hwsinha.payflow.ledger;

import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends Repository<Account, UUID> {
    Account save(Account account);
    Optional<Account> findById(UUID id);
}

