package com.lin.monkey_finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class MonkeyFinanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonkeyFinanceApplication.class, args);
    }

}
