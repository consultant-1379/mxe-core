package com.ericsson.mxe.modeltrainingservice.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.backendservicescommon.kubernetes.ServicePortResolverService;
import com.ericsson.mxe.modeltrainingservice.IntegrationTests;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Job;

@IntegrationTests
public class OnboardTrainingPackageTests {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    KubernetesService kubernetesService;
    @MockBean
    ServicePortResolverService servicePortResolverService;

    private String boundary;

    @BeforeEach
    void init() throws ApiException {
        final String namespace = "namespace";

        when(kubernetesService.getNamespace()).thenReturn(namespace);
        when(servicePortResolverService.resolve(any(), any(), any())).thenReturn(Optional.of(22));
        when(kubernetesService.createNamespacedJob(any(), any())).thenReturn(new V1Job());

        boundary = UUID.randomUUID().toString();
    }

    @Test
    public void shouldHandleProperMultipartFileRequest() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/training-packages")
                .contentType("multipart/form-data; boundary=" + boundary)
                .content(createFileContent(TrainingPackageServiceController.TRAINING_PACKAGE, "training-package.zip"));

        mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void shouldHandleMultipartFileRequestWithInvalidName() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/training-packages")
                .contentType("multipart/form-data; boundary=" + boundary)
                .content(createFileContent("invalid_name", "training-package.zip"));

        mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void shouldNotHandleNotMultipartFileRequest() throws Exception {
        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/v1/training-packages")
                .contentType(MediaType.APPLICATION_JSON).content(StringUtils.EMPTY);

        mockMvc.perform(builder).andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andDo(MockMvcResultHandlers.print());
    }

    private byte[] createFileContent(final String name, final String fileName) {
        byte[] data = new byte[100];
        Arrays.fill(data, (byte) 1);
        String contentType = "application/zip";

        String start = "--" + boundary + "\r\n Content-Disposition: form-data; name=\"" + name + "\"; filename=\""
                + fileName + "\"\r\n" + "Content-type: " + contentType + "\r\n\r\n";;

        String end = "\r\n--" + boundary + "--";
        return ArrayUtils.addAll(start.getBytes(), ArrayUtils.addAll(data, end.getBytes()));
    }

}
