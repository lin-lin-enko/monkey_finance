package com.lin.monkey_finance.domain.ledger.dto;

import com.lin.monkey_finance.domain.ledger.model.AccessType;

import java.time.OffsetDateTime;
import java.util.UUID;


//Used for adding a member to the ledger members table and requesting user to accept the invitation
public record LedgerMemberRequestDto(
    UUID userId,
    AccessType accessType,
    OffsetDateTime invitedAt
){}
