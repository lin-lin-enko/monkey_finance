package com.lin.monkey_finance.domain.account.dto;

import com.lin.monkey_finance.domain.account.model.AccountType;
import com.lin.monkey_finance.domain.account.model.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountCreateDto(
        @NotBlank
        @Size(min = 2, max = 50, message = "Account name must be 2 to 50 characters long")
        String name,

        @NotNull
        AccountType type,

        @Size(min = 2, max = 70, message = "Institution field must be 2 to 70 characters long")
        String institution,

        @NotNull
        BigDecimal balance,

        @NotNull
        Currency currency,

        @Size(min = 2, max = 255, message = "Description must be 2 to 255 characters long")
        String description
) {}
