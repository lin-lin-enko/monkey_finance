package com.lin.monkey_finance.domain.ledger.event;

import com.lin.monkey_finance.domain.ledger.model.LedgerActionType;

import java.util.UUID;

public record LedgerActivityLogEvent(
    UUID ledgerId,
    UUID actorId,
    UUID targetId,
    LedgerActionType actionType,
    String description
){}