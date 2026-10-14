package com.lin.monkey_finance.domain.transaction.dto;

import com.lin.monkey_finance.domain.account.model.Currency;
import com.lin.monkey_finance.domain.transaction.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionCreateDto(
        @NotBlank
        @Size(min = 2, max = 50, message = "Transaction name must be between 2 and 50 characters")
        String name,

        @Size(max = 255, message = "Description can't be longer than 255 characters")
        String description,

        @NotNull
        BigDecimal amount,

        Currency currency,

        @NotNull
        TransactionType type,

        OffsetDateTime occurredAt,

        @NotNull
        UUID categoryId,

        UUID subcategoryId,

        UUID savingsPotId
) {}
