package com.lxxself.magnetw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class MagnetwApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagnetwApplication.class, args);
    }

}
