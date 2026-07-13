package com.lin.monkey_finance.domain.ledger.dto;

import com.lin.monkey_finance.domain.ledger.model.AccessType;
import com.lin.monkey_finance.domain.ledger.model.MemberStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LedgerMemberResponseDto(
    UUID userId,
    String username,
    AccessType accessType,
    MemberStatus memberStatus,
    OffsetDateTime joinedAt
){}
