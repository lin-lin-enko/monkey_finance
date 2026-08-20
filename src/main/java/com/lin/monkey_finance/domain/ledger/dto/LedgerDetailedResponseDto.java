package com.lin.monkey_finance.domain.ledger.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class LedgerDetailedResponseDto{
    private UUID id;
    private String name;
    private String description;
    private UUID creatorId;
    private OffsetDateTime createdAt;
    private List<LedgerMemberResponseDto> members;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public UUID getCreatorId(){
        return creatorId;
    }

    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<LedgerMemberResponseDto> getMembers() {
        return members;
    }

    public void setMembers(List<LedgerMemberResponseDto> members) {
        this.members = members;
    }

    public LedgerDetailedResponseDto(UUID ledgerId, String ledgerName, String ledgerDescription, UUID ledgerCreatorId, OffsetDateTime ledgerCreatedAt, List<LedgerMemberResponseDto> members){
        this.setId(ledgerId);
        this.setName(ledgerName);
        this.setDescription(ledgerDescription);
        this.setCreatorId(ledgerCreatorId);
        this.setCreatedAt(ledgerCreatedAt);
        this.setMembers(members);
    }
}