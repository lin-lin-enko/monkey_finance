package com.lin.monkey_finance.domain.savings.dto;

import com.lin.monkey_finance.domain.account.model.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SavingsCreateDto(
    @NotBlank
    @Size(min = 2, max = 50, message = "Savings name must be 2 to 50 characters long")
    String name,

    @Size(min = 2, max = 255, message = "Description name must be 2 to 255 characters long")
    String description,

    @NotBlank
    Currency currency,

    @NotNull
    BigDecimal targetSum,

    OffsetDateTime dueDate,

    BigDecimal percentageRate
){}
