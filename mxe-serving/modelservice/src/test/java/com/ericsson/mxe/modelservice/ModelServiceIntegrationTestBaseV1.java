package com.ericsson.mxe.modelservice;

import com.ericsson.mxe.securitycommon.accesscontrol.AccessControl;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import java.io.IOException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public abstract class ModelServiceIntegrationTestBaseV1 extends ModelServiceTestsBaseV1 {

    @MockBean
    private AccessControl accessControl;

    @BeforeEach
    void init() throws IOException {
        when(accessControl.isAccessAllowed(anyString(), any(), any())).thenReturn(true);
        when(accessControl.isAccessAllowed(isNull(), any(), any())).thenReturn(true);
    }
}

