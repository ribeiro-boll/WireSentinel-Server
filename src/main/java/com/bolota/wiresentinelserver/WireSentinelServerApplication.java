package com.bolota.wiresentinelserver;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDate;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class WireSentinelServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WireSentinelServerApplication.class, args);
    }

}
