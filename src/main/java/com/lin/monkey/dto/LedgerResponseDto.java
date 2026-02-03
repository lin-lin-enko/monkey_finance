package com.lin.monkey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

import com.lin.monkey.model.Ledger;

public record LedgerResponseDto(
        UUID id,
        String name,
        String description,
        LocalDateTime createdAt,
        UUID ownerId
) {
    public static LedgerResponseDto fromLedger(Ledger ledger) {
        if (ledger == null) return null;
        return new LedgerResponseDto(
                ledger.getId(),
                ledger.getName(),
                ledger.getDescription(),
                ledger.getCreatedAt(),
                ledger.getOwnerId()
        );
    }
}
