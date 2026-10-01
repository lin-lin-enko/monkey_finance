package com.lin.monkey_finance.domain.ledger.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LedgerResponseDto(
    UUID id,
    String name,
    String description,
    UUID creatorId,
    OffsetDateTime createdAt
){}
