package com.lin.monkey_finance.domain.transaction.dto;

import com.lin.monkey_finance.domain.transaction.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionResponseDto(

        UUID id,

        String name,

        String description,

        BigDecimal amount,

        TransactionType type,

        UUID authorId,

        OffsetDateTime occurredAt,

        UUID categoryId,

        UUID subcategoryId,

        UUID ledgerId,

        UUID accountId,

        UUID savingsPotId,

        OffsetDateTime createdAt
) {}
