package com.b4code.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class B4CodeBackendApplication {

    public static void main(String[] args) {
        // Load .env variables into System properties for Spring to pick up
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        
        SpringApplication.run(B4CodeBackendApplication.class, args);
    }

}
