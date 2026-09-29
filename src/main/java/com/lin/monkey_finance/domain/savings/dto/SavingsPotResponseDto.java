package com.lin.monkey_finance.domain.savings.dto;

import com.lin.monkey_finance.domain.account.model.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SavingsPotResponseDto(

        UUID id,
        String name,
        String description,
        Currency currency,
        BigDecimal targetAmount,
        LocalDate dueDate,
        BigDecimal percentageRate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
