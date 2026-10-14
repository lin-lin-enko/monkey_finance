package com.lin.monkey_finance.domain.account.service;

import com.lin.monkey_finance.BaseIntegrationTest;
import com.lin.monkey_finance.domain.account.dto.CurrencyExchangeResponseDto;
import com.lin.monkey_finance.domain.account.model.Currency;
import com.lin.monkey_finance.domain.transaction.service.CurrencyConverterService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CurrencyConverterServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CurrencyConverterService converterService;

    @Autowired
    private CacheManager cacheManager;

    // spring component that builds http clients
    @Autowired
    @Qualifier("currencyConverterRestBuilder")
    private RestClient.Builder restClientBuilder;

    // an instrument that intercepts the requests from the rest client
    private MockRestServiceServer mockServer;

    @Value("${currency.api.url:https://v6.exchangerate-api.com/v6}")
    private String apiUrl;

    @Value("${currency.api.key:test_api_key}")
    private String apiKey;

    @BeforeAll
    void setUpMockServer(){
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    // before each test
    @BeforeEach
    void setUp(){
        // binding the interceptor to the builder. So all http requests
        // inside CurrencyConverterService will be intercepted by the mock server
        mockServer.reset();

        var cache = cacheManager.getCache("exchangeRates");
        assertThat(cache).isNotNull();

        cache.evictIfPresent("UAH");

        assertThat(cache.get("UAH")).as("Cache must be empty before each test").isNull();
    }

    @Test
    @DisplayName("Makes an initial http request (like when there's no cache) and caches data in redis")
    void getRatesAndCacheThem(){
        // mock json response
        String response = """
                {
                    "result": "success",
                    "base_code": "UAH",
                    "conversion_rates": {
                        "USD": 0.02240,
                        "EUR": 0.01925,
                        "CNY": 0.1506
                    }
                }
                """;
        // setting up the mock server to behave according to the test goal
        mockServer.expect(requestTo(apiUrl + "/" + apiKey + "/latest/UAH"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        CurrencyExchangeResponseDto responseDto = converterService.getLatestRates(Currency.UAH);


        // var instead of org.springframework.cache.Cache
        // improves readability but the variable is still strictly typed
        var cache = cacheManager.getCache("exchangeRates");
        assertThat(cache).isNotNull();

        Map<String, BigDecimal> rates = responseDto.conversionRates();
        // checking if the response (dto) is correct
        // never use if for tests!! cause it won't do anything
        // assertThat will not only check, but also fail the test if needed
        assertThat(rates)
                // checks if response has 3 objects
                .hasSize(3)
                .containsEntry("USD", new BigDecimal("0.02240"))
                .containsEntry("EUR", new BigDecimal("0.01925"))
                .containsEntry("CNY", new BigDecimal("0.1506"));

        CurrencyExchangeResponseDto cachedDto = cache.get("UAH", CurrencyExchangeResponseDto.class);
        assertThat(cachedDto).isNotNull();

        Map<String, BigDecimal> cachedRates = cachedDto.conversionRates(); // Map.class automatically casts cachedRates into a Map<,>
        assertThat(cachedRates).isNotNull();
        assertThat(cachedRates)
                .hasSize(3)
                .containsEntry("USD", new BigDecimal("0.02240"))
                .containsEntry("EUR", new BigDecimal("0.01925"))
                .containsEntry("CNY", new BigDecimal("0.1506"));

        // checking if the http request was done
        mockServer.verify();
    }

    @Test
    @DisplayName("Checks how the code behaves when there is cached data")
    void getCachedRates(){

        // checking if there is a container for exchangeRates cache
        var cache = cacheManager.getCache("exchangeRates");
        assertThat(cache).isNotNull();

        CurrencyExchangeResponseDto expectedDto = new CurrencyExchangeResponseDto(
                "success",
                "UAH",
                Map.of(
                        "USD", new BigDecimal("0.02240"),
                        "EUR", new BigDecimal("0.01925"),
                        "CNY", new BigDecimal("0.1506")
                )
        );

        // putting rates for UAH into cache to check if the request will be made
        cache.put("UAH", expectedDto);
        System.out.println("Cache name: " + cache.getName());
        System.out.println("Cache type: " + cache.getClass().getName());
        System.out.println("Cached value: " + cache.get("UAH"));

        // test will fail if the service makes a request, cause it wasn't written here
        CurrencyExchangeResponseDto responseDto = converterService.getLatestRates(Currency.UAH);
        Map<String, BigDecimal> rates = responseDto.conversionRates();

        assertThat(rates)
                .hasSize(3)
                .containsEntry("USD", new BigDecimal("0.02240"))
                .containsEntry("EUR", new BigDecimal("0.01925"))
                .containsEntry("CNY", new BigDecimal("0.1506"));

    }

    @Test
    @DisplayName("Testing if there are exceptions if something went wrong")
    void testWrongSituations(){
        // setting the server so something is wrong
        mockServer.expect(requestTo(apiUrl + "/" + apiKey + "/latest/UAH"))
                .andRespond(withServerError());

        // calling the method and catching the exception
        // checking if exception is from the converterService and thrown by getLatestRates()
        // and is a server error
        assertThatThrownBy(() -> converterService.getLatestRates(Currency.UAH))
                .isInstanceOf(HttpServerErrorException.class);

        mockServer.verify();

        var cache = cacheManager.getCache("exchangeRates");
        assertThat(cache).isNotNull();
        // checking if in case of an exception nothing was written into cache
        assertThat(cache.get("UAH")).isNull();
    }

    @Test
    @DisplayName("Check if there is exception when response result is error")
    void testWhenResponseResultError(){
        String expectedResponse = """
                {
                    "result": "error",
                    "error-type": "invalid-key"
                }
                """;

        mockServer.expect(requestTo(apiUrl + "/" + apiKey + "/latest/UAH"))
                .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> converterService.getLatestRates(Currency.UAH))
                .isInstanceOf(Exception.class);
        mockServer.verify();
    }
}
