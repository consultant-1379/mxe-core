package com.ericsson.mxe;

import com.ericsson.mxe.keycloak.KeycloakInitApplication;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootTest(classes = {KeycloakInitApplication.class})
@AutoConfigureJsonTesters
public @interface IntegrationTests {
}

