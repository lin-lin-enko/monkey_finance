package com.lin.monkey_finance.domain.account.dto;

import com.lin.monkey_finance.domain.account.model.AccountType;
import com.lin.monkey_finance.domain.account.model.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountEditDto(
        @Size(min = 2, max = 50, message = "Account name must be 2 to 50 characters long")
        String name,

        AccountType type,

        @Size(min = 2, max = 70, message = "Institution field must be 2 to 70 characters long")
        String institution,

        BigDecimal balance,

        Currency currency,

        @Size(min = 2, max = 255, message = "Description must be 2 to 255 characters long")
        String description
) {}
