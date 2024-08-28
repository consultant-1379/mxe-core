package com.ericsson.mxe.examples.restclient;

import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableAsync
public class ThreadConfig {

    @Bean
    public TaskScheduler scheduled() {
        return new TaskSchedulerBuilder().poolSize(1).build();
    }
}
