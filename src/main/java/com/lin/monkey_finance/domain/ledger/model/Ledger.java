package com.lin.monkey_finance.domain.ledger.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ledgers", schema = "dev")
public class Ledger {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Size(min = 2, max = 50)
    @Column(nullable = false, length = 50)
    private String name;

    private String description;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    @Column(name = "creator_id", nullable = false, updatable = false)
    private UUID creatorId;

    @OneToMany(mappedBy = "ledger")
    private List<LedgerMembership> memberships = new ArrayList<>();

    public void addMember(LedgerMembership member){
        this.memberships.add(member);
        member.setLedger(this);
    }

    protected Ledger(){}

    public Ledger(String name, String description, UUID creatorId){
        this.name = name;
        this.description = description;
        this.creatorId = creatorId;
    }

    public UUID getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public List<LedgerMembership> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<LedgerMembership> memberships) {
        this.memberships = memberships;
    }
}
