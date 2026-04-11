package com.lin.monkey.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledgers")
public class Ledger {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Ledger must have a name")
    @Size(min = 3, max = 60, message = "Name must be 3 to 60 symbols long")
    @Column(nullable = false, length = 60)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "creator_id", nullable = false, updatable = false)
    private UUID creatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", insertable = false, updatable = false, nullable = false)
    private User creator;

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }

    public User getCreator() {
        return creator;
    }

    // when there are 2 obj with the same id
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // if that's the same obj return true
        if (obj == null || getClass() != obj.getClass()) return false; // if null or different classes return false
        Ledger ledger = (Ledger) obj; // Object to Ledger
        return id.equals(ledger.id); // looking if id is the same
    }

    @Override
    public int hashCode() {
        return id.hashCode(); // hash from id
    }
}

