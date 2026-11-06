package com.secondbrain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class SecondBrainApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondBrainApplication.class, args);
        System.out.println("Second Brain Knowledge Engine initialized successfully.");
    }
}