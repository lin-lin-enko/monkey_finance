package com.lin.monkey_finance.domain.ledger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class LedgerMemberId implements Serializable {

    @Column(name = "ledger_id", nullable = false, updatable = false)
    private UUID ledgerId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    public LedgerMemberId(){}

    public LedgerMemberId(UUID ledgerId, UUID userId){
        this.ledgerId = ledgerId;
        this.userId = userId;
    }

    public UUID getLedgerId() {
        return ledgerId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LedgerMemberId that = (LedgerMemberId) obj;
        return Objects.equals(ledgerId, that.ledgerId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode(){
        return Objects.hash(ledgerId, userId);
    }
}
