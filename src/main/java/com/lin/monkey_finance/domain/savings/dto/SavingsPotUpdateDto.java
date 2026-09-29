package com.lin.monkey_finance.domain.savings.dto;

import com.lin.monkey_finance.domain.account.model.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SavingsPotUpdateDto(
    @Size(min = 2, max = 50, message = "SavingsPot name must be 2 to 50 characters long")
    String name,

    @Size(min = 2, max = 255, message = "Description name must be 2 to 255 characters long")
    String description,

    Currency currency,

    BigDecimal targetAmount,

    LocalDate dueDate,

    BigDecimal percentageRate
){}
