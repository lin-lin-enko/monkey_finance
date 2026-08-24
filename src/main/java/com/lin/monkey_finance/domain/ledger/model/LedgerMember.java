package com.lin.monkey_finance.domain.ledger.model;

import com.lin.monkey_finance.domain.user.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger_members", schema = "dev")
public class LedgerMember {

    @EmbeddedId
    private LedgerMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ledgerId")
    @JoinColumn(name = "ledger_id")
    private Ledger ledger;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String username;

    @Column(name = "is_default_ledger", nullable = false)
    private boolean isDefaultLedger = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false, length = 20)
    private AccessType accessType;

    @Generated(event = EventType.INSERT)
    @Column(name = "joined_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    protected LedgerMember(){}

    public LedgerMember(UUID ledgerId, UUID userId, Ledger ledger, User user, String username, boolean isDefaultLedger, AccessType accessType, MemberStatus status){
        this.id = new LedgerMemberId(ledgerId, userId);
        this.ledger = ledger;
        this.user = user;
        this.username = username;
        this.isDefaultLedger = isDefaultLedger;
        this.accessType = accessType;
        this.status = status;
    }

    public LedgerMemberId getId() {
        return id;
    }

    public Ledger getLedger() {
        return ledger;
    }

    public void setLedger(Ledger ledger) {
        this.ledger = ledger;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
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
