package com.lin.monkey_finance.domain.ledger.dto;

import com.lin.monkey_finance.domain.ledger.model.AccessType;

import java.time.OffsetDateTime;
import java.util.UUID;


// used to invite user to a ledger
public record LedgerMemberInvitationDto(
    UUID ledgerId,
    String ledgerName,
    String ledgerDescription,
    String invitedByUsername,
    UUID invitedByUserId,
    AccessType accessType,
    OffsetDateTime invitedAt
){}
