package com.ttgint.aggregate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication(scanBasePackages = "com.ttgint.*")
public class AggregateApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregateApplication.class, args);
    }

}
