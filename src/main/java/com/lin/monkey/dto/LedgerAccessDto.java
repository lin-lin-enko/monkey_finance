package com.lin.monkey.dto;

import com.lin.monkey.model.UserRoleInLedger;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LedgerAccessDto(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Ledger ID is required")
        UUID ledgerId,

        @NotNull(message = "User's role is required")
        UserRoleInLedger role
) {
}
