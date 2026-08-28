package com.lin.monkey_finance.domain.ledger.dto;

import com.lin.monkey_finance.domain.ledger.model.LedgerActionType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LedgerActivityLogResponseDto(
        UUID id,
        UUID ledgerId,
        UUID actorId,
        UUID targetId,
        LedgerActionType actionType,
        String description,
        OffsetDateTime createdAt
) {}
