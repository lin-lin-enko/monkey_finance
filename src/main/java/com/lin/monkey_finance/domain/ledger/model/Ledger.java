package com.lin.monkey_finance.domain.ledger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledgers", schema = "dev")
public class Ledger {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Size(min = 2, max = 50)
    @Column(nullable = false, length = 50)
    private String name;

    private String description;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    @Column(name = "creator_id", nullable = false)
    private UUID creatorId;

    public Ledger(){}

    public Ledger(String name, String description, UUID creatorId){
        this.id = UUID.randomUUID();
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

    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }

    public UUID getCreatorId() {
        return creatorId;
    }
}
