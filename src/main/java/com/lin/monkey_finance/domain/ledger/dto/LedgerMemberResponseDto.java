package com.lin.monkey_finance.domain.ledger.dto;

import com.lin.monkey_finance.domain.ledger.model.AccessType;
import com.lin.monkey_finance.domain.ledger.model.MemberStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

// used to get info about a user's membership
public record LedgerMemberResponseDto(
        UUID userId,
        String username,
        AccessType accessType,
        MemberStatus status,
        OffsetDateTime invitedAt,
        UUID invitedByUserId,
        String invitedByUsername,
        OffsetDateTime joinedAt,
        OffsetDateTime leftAt
) {}
