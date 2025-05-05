package com.ttgint.nodius;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication(scanBasePackages = "com.ttgint.*")
public class NodiusApplication {

    public static void main(String[] args) {
        SpringApplication.run(NodiusApplication.class, args);
    }

}
