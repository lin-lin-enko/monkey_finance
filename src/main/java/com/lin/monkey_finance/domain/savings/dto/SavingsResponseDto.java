package com.lin.monkey_finance.domain.savings.dto;

import com.lin.monkey_finance.domain.account.model.Currency;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SavingsResponseDto(

        UUID id,
        UUID accountId,
        String name,
        String description,
        Currency currency,
        BigDecimal targetSum,
        OffsetDateTime dueDate,
        BigDecimal percentageRate,
        OffsetDateTime createdAt
) {}
