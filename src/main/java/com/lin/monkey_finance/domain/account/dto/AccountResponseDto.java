package com.lin.monkey_finance.domain.account.dto;

import com.lin.monkey_finance.domain.account.model.AccountType;
import com.lin.monkey_finance.domain.account.model.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponseDto(
   UUID id,
   String name,
   AccountType type,
   String institution,
   BigDecimal balance,
   Currency currency,
   String description,
   UUID ledgerId
) {}
