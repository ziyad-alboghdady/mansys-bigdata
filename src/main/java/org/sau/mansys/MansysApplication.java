package org.sau.mansys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching

public class MansysApplication {
    public static void main(String[] args) {
        SpringApplication.run(MansysApplication.class, args);
    }
}
