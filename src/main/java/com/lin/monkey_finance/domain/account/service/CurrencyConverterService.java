package com.lin.monkey_finance.domain.account.service;


import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.account.dto.CurrencyExchangeResponseDto;
import com.lin.monkey_finance.domain.account.model.Currency;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CurrencyConverterService {

    // kind of like an intra-app browser
    private final RestClient.Builder restClientBuilder;

    public CurrencyConverterService(
        @Qualifier("currencyConverterRestBuilder") RestClient.Builder restClientBuilder
        ){
        // setting the api address of the currency converter site
        this.restClientBuilder = restClientBuilder;

    }

    // checks if there's already a cashed key "USD"
    // if yes, method won't work and spring will exchange
    // rates from cached data
    // if not, method will work and the result will be cached for 24 hours

    // value is cache name!! key is a certain key inside that cache
    @Cacheable(value = "exchangeRates", key = "#baseCurrency.name()")
    public CurrencyExchangeResponseDto getLatestRates(Currency baseCurrency){

        RestClient client = restClientBuilder.build();
        // makes GET request
        CurrencyExchangeResponseDto responseDto = client.get()
                // creates request address
                .uri("/latest/{baseCurrencyName}", baseCurrency.name())
                // sends a request
                .retrieve()
                // takes json-response and makes it into a dto
                .body(CurrencyExchangeResponseDto.class);
        if (responseDto != null && !"success".equalsIgnoreCase(responseDto.result()))
            throw new IllegalStateException("Error in response: " + responseDto.result());

        return responseDto;
    }

    public BigDecimal convertSum(BigDecimal amount, Currency from, Currency to){
        if (from == to || amount.compareTo(BigDecimal.ZERO) == 0){
            return amount;
        }
         CurrencyExchangeResponseDto responseDto = getLatestRates(from);

        if (responseDto == null || responseDto.conversionRates() == null)
            throw new IllegalStateException("Couldn't get exchange rates");

        // getting needed currency rate
        BigDecimal rate = responseDto.conversionRates().get(to.name());
        if (rate == null)
            throw new ResourceNotFoundException("No rate for this currency: " + to);

        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
