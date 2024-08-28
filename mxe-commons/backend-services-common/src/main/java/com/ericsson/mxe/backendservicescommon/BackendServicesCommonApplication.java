package com.ericsson.mxe.backendservicescommon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = {"com.ericsson.mxe.backendservicescommon.config.properties"})
public class BackendServicesCommonApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendServicesCommonApplication.class, args);
    }

}
