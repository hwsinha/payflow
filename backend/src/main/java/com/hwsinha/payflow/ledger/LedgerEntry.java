package com.hwsinha.payflow.ledger;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, updatable = false)
    @NotNull
    private UUID transactionId;

    @Column(name = "account_id", nullable = false, updatable = false)
    @NotNull
    private UUID accountId;

    @Column(nullable = false, updatable = false)
    private long amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public LedgerEntry(UUID transactionId, UUID accountId, long amount) {
        if (amount == 0) {
            throw new IllegalArgumentException("amount must be non-zero");
        }
        this.transactionId = Objects.requireNonNull(transactionId);
        this.accountId = Objects.requireNonNull(accountId);
        this.amount = amount;
        this.createdAt = Instant.now();
    }
}
