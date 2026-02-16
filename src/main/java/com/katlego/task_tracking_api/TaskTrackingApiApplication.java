package com.katlego.task_tracking_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaskTrackingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskTrackingApiApplication.class, args);
    }

}
