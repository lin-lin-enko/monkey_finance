package com.lin.monkey_finance;

import org.springframework.boot.SpringApplication;

public class TestMonkeyFinanceApplication {

    public static void main(String[] args) {
        SpringApplication.from(MonkeyFinanceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
