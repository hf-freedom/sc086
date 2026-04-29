package com.sports.venue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VenueReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(VenueReservationApplication.class, args);
    }
}