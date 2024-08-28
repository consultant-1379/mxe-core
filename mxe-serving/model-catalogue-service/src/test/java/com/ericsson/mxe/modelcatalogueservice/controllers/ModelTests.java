package com.ericsson.mxe.modelcatalogueservice.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.ericsson.mxe.securitycommon.accesscontrol.AccessControl;

public class ModelTests extends ModelTestsBase {

    @MockBean
    private AccessControl accessControl;

    @BeforeEach
    void initModelTest() {
        when(accessControl.isAccessAllowed(anyString(), any(), any())).thenReturn(true);
    }

}
