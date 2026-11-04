package com.formaccess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/**
 * Entry point. Run this class to start the whole application on http://localhost:8080
 *
 * @SpringBootApplication  = scan this package for @Controller/@Service/@Repository classes
 *                           and configure everything automatically.
 * @EnableScheduling       = allow @Scheduled methods (auto-close events, auto reminders).
 */
@SpringBootApplication
@EnableScheduling
public class FormAccessApplication {

    public static final String TIME_ZONE = "Asia/Kolkata";

    public static void main(String[] args) {
        // Every LocalDateTime in the app means IST, on every computer.
        TimeZone.setDefault(TimeZone.getTimeZone(TIME_ZONE));
        SpringApplication.run(FormAccessApplication.class, args);
    }
}
