package com.ericsson.mxe.modeltrainingservice;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.ericsson.mxe.modeltrainingservice.config.TestDbConfig;
import jakarta.transaction.Transactional;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootTest(classes = {ModelTrainingServiceApplication.class, TestDbConfig.class})
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@Transactional
@AutoConfigureJsonTesters
@ActiveProfiles("test")
@AutoConfigureMockRestServiceServer
public @interface IntegrationTests {
}

