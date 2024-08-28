package com.ericsson.mxe.modelcatalogueservice.controllers;

import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.backendservicescommon.kubernetes.ServicePortResolverService;
import com.ericsson.mxe.modelcatalogueservice.IntegrationTests;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTests
@Disabled
public class OnboardModelTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    KubernetesService kubernetesService;
    @MockBean
    ServicePortResolverService servicePortResolverService;
    private V1Job job;

    @BeforeEach
    void init() throws ApiException {
        final String namespace = "namespace";

        when(kubernetesService.getNamespace()).thenReturn(namespace);
        when(servicePortResolverService.resolve(any(), any(), any())).thenReturn(Optional.of(22));
        when(kubernetesService.createNamespacedJob(any(), any())).thenReturn(job);
    }

    @Test
    void testMultipartOnboarding() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/v1/models")
                .file(new MockMultipartFile("onboardfile", repeat("1234567890".getBytes(), 100))))
                .andExpect(status().isOk());
    }

    private static InputStream repeat(byte[] sample, int times) {
        return new InputStream() {
            private long pos = 0;
            private final long total = (long) sample.length * times;

            public int read() throws IOException {
                return pos < total ? sample[(int) (pos++ % sample.length)] : -1;
            }
        };
    }

}
