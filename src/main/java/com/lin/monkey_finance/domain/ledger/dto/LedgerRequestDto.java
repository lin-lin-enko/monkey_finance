package com.lin.monkey_finance.domain.ledger.dto;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record LedgerRequestDto(
        @NotBlank
        @Size(min = 3, max = 25, message = "The name of the ledger must be 3 to 25 characters long")
        String name,
        @Size(max = 255, message = "Description can't be longer than 255 characters")
        String description
) {
}