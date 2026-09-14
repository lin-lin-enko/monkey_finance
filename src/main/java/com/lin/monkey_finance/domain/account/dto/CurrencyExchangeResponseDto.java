package com.lin.monkey_finance.domain.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

public record CurrencyExchangeResponseDto (
    String result,

    @JsonProperty("base_code")
    String baseCode,

    @JsonProperty("conversion_rates")
    Map<String, BigDecimal> conversionRates
){}
