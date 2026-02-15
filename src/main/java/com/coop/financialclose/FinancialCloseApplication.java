package com.coop.financialclose;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FinancialCloseApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialCloseApplication.class, args);
    }
}
