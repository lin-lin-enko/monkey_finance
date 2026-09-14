package com.lin.monkey_finance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder currencyConverterRestBuilder(
            @Value("${currency.api.url:https://v6.exchangerate-api.com/v6}") String apiUrl,
            @Value("${currency.api.key:test_api_key}") String apiKey
    ){
        return RestClient.builder().baseUrl(apiUrl + "/" + apiKey);
    }
}