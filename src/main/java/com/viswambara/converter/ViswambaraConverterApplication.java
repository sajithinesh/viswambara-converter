package com.viswambara.converter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
@ConfigurationPropertiesScan
public class ViswambaraConverterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ViswambaraConverterApplication.class, args);
    }
}
