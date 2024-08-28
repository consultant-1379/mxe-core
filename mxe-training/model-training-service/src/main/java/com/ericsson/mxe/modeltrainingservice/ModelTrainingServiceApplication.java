package com.ericsson.mxe.modeltrainingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = {"com.ericsson.mxe.modeltrainingservice.config.properties"})
@ComponentScan(basePackages = {"com.ericsson.mxe"})
public class ModelTrainingServiceApplication {
    public static final String USERNAME_KEY = "x-auth-userid";
    public static final String USERID_KEY = "x-auth-subject";

    public static void main(String[] args) {
        SpringApplication.run(ModelTrainingServiceApplication.class, args);
    }
}
