package com.lin.monkey_finance.domain.ledger.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Generated;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger_members", schema = "dev")
public class LedgerMember {

    @EmbeddedId
    private LedgerMemberId id;

    @Column(name = "is_default_ledger", nullable = false)
    private boolean isDefaultLedger = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false, length = 20)
    private AccessType accessType;

    @Generated
    @Column(name = "joined_at", nullable = false, updatable = false)
    private OffsetDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    public LedgerMember(){}

    public LedgerMember(UUID ledgerId, UUID userId, boolean isDefaultLedger, AccessType accessType, MemberStatus status){
        this.id = new LedgerMemberId(ledgerId, userId);
        this.isDefaultLedger = isDefaultLedger;
        this.accessType = accessType;
        this.status = status;
    }

    public LedgerMemberId getId() {
        return id;
    }

    public boolean isDefaultLedger() {
        return isDefaultLedger;
    }

    public void setDefaultLedger(boolean defaultLedger) {
        isDefaultLedger = defaultLedger;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }
}
