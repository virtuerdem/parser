package com.ttgint.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;

@Slf4j
@EnableRetry
@EntityScan("com.ttgint.*")
@ComponentScan("com.ttgint.*")
@EnableJpaRepositories("com.ttgint.*")
@SpringBootApplication(scanBasePackages = "com.ttgint.*")
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

}
