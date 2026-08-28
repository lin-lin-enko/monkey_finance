package com.lin.monkey_finance.domain.ledger.model;

import com.lin.monkey_finance.domain.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger_activity_logs", schema = "dev")
public class LedgerActivityLog {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_id", nullable = false, updatable = false)
    private Ledger ledger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false, updatable = false)
    private User actor;

    @Column(name = "target_id", updatable = false)
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, updatable = false)
    private LedgerActionType actionType;

    @Size(max = 255, message = "Description can't be longer that 255 characters")
    @Column(updatable = false)
    private String description;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected LedgerActivityLog() {}

    public LedgerActivityLog(
        Ledger ledger,
        User actor,
        UUID targetId,
        LedgerActionType actionType,
        String description
    ){
        this.id = UUID.randomUUID();
        this.ledger = ledger;
        this.actor = actor;
        this.targetId = targetId;
        this.actionType = actionType;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public Ledger getLedger() {
        return ledger;
    }

    public User getActor() {
        return actor;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
