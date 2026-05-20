package com.bolota.wiresentinelserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WireSentinelServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WireSentinelServerApplication.class, args);
    }

}
