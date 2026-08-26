package com.lin.monkey_finance.domain.ledger.model;

import com.lin.monkey_finance.domain.user.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.OffsetDateTime;

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
    @Column(name = "invited_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime invitedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_user_id")
    private User invitedByUser;

    @Column(name = "joined_at")
    private OffsetDateTime joinedAt;

    @Column(name = "left_at")
    private OffsetDateTime leftAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    protected LedgerMember(){}

    public LedgerMember(
            Ledger ledger,
            User user,
            String username,
            boolean isDefaultLedger,
            AccessType accessType,
            User invitedByUser,
            MemberStatus status
    ){
        this.id = new LedgerMemberId(ledger.getId(), user.getId());
        this.ledger = ledger;
        this.user = user;
        this.username = username;
        this.isDefaultLedger = isDefaultLedger;
        this.accessType = accessType;
        this.invitedByUser = invitedByUser;
        this.status = status;
    }

    public void acceptInvitation(){
        this.status = MemberStatus.ACTIVE;
        this.joinedAt = OffsetDateTime.now();
    }

    public void leaveLedger(){
        this.status = MemberStatus.LEFT;
        this.leftAt = OffsetDateTime.now();
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

    public User getInvitedByUser() {
        return invitedByUser;
    }

    public void setInvitedByUser(User invitedByUser) {
        this.invitedByUser = invitedByUser;
    }

    public OffsetDateTime getInvitedAt(){
        return invitedAt;
    }

    public OffsetDateTime getLeftAt() { return leftAt; }
}
