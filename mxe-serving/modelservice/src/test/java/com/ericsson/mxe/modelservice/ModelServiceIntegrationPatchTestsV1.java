package com.ericsson.mxe.modelservice;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.IOException;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.ericsson.mxe.modelservice.dto.response.ModelDeploymentResponse;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiException;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ModelServiceIntegrationPatchTestsV1 extends ModelServiceIntegrationTestBaseV1 {

    @Test
    void testModelPatchWithVersion() throws Exception {
        preparePatch("seldondeployment/getSeldonSingleModelDeployment.json");

        performRequest(patch(URL + DEPLOYMENT_NAME), "dto/patchSingleModelVersion.json").andExpect(status().isOk());

        verifyPatch("seldondeployment/patchedSeldonSingeModelDeploymentWithVersion.json");
    }

    @Test
    void testModelPatchWithVersionNA() throws Exception {
        preparePatch("seldondeployment/getSeldonSingleModelDeployment.json");

        performRequest(patch(URL + DEPLOYMENT_NAME), "dto/patchSingleModelVersion_NA.json")
                .andExpect(status().isConflict());

        verify(kubernetesService, never()).patchNamespacedCustomObject(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testABTestPatchWithVersion() throws Exception {
        preparePatch("seldondeployment/getSeldonABTestModelDeployment.json");

        performRequest(patch(URL + DEPLOYMENT_NAME), "dto/patchABTestVersion.json").andExpect(status().isOk());

        verifyPatch("seldondeployment/patchedSeldonABTestDeployment.json");
    }

    @Test
    void testABTestPatchWithVersionNA() throws Exception {
        preparePatch("seldondeployment/getSeldonABTestModelDeployment.json");

        performRequest(patch(URL + DEPLOYMENT_NAME), "dto/patchABTestVersion_NA.json").andExpect(status().isConflict());

        verify(kubernetesService, never()).patchNamespacedCustomObject(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testModelPatchReplicasWithAutoscaling() throws Exception {
        preparePatch("seldondeployment/getSeldonSingleModelDeployment.json");

        performRequest(patch(URL + DEPLOYMENT_NAME), "dto/patchSeldonSingeModelDeploymentWithAutoscaling1.json")
                .andExpect(status().isOk());

        verifyPatch("seldondeployment/patchedSeldonSingeModelDeploymentWithAutoscaling.json");
    }

    @Test
    void testModelPatchAutoscalingWithPreviousReplicas() throws Exception {
        preparePatch("seldondeployment/getSeldonSingleModelDeploymentWithAutoscaling.json");

        performRequest(patch(URL + DEPLOYMENT_NAME), "dto/patchSingleModelReplicas1.json").andExpect(status().isOk());

        verifyPatch("seldondeployment/patchedSeldonSingeModelDeploymentWithReplicas1.json");
    }

    @Test
    void testModelPatchAutoscalingWithNewReplicas() throws Exception {
        preparePatch("seldondeployment/getSeldonSingleModelDeploymentWithAutoscaling.json");

        performRequest(patch(URL + DEPLOYMENT_NAME), "dto/patchSingleModelReplicas2.json").andExpect(status().isOk());

        verifyPatch("seldondeployment/patchedSeldonSingeModelDeploymentWithReplicas2.json");
    }

    @Test
    void testModelPatchAutoscalingWithAutoscaling() throws Exception {
        preparePatch("seldondeployment/getSeldonSingleModelDeploymentWithAutoscaling.json");

        performRequest(patch(URL + DEPLOYMENT_NAME), "dto/patchSeldonSingeModelDeploymentWithAutoscaling1.json")
                .andExpect(status().isOk());

        verifyPatch("seldondeployment/patchedSeldonSingeModelDeploymentWithAutoscaling.json");
    }

    @Test
    void testModelPatchAutoscalingWithVersion() throws Exception {
        preparePatch("seldondeployment/getSeldonSingleModelDeploymentWithAutoscaling.json");

        performRequest(patch(URL + DEPLOYMENT_NAME), "dto/patchSingleModelVersion.json").andExpect(status().isOk());

        verifyPatch("seldondeployment/patchedSeldonSingeModelDeploymentAutoscalingWithVersion.json");
    }

    @Test
    void testModelPatchReplicasNothingToDo() throws Exception {
        preparePatch("seldondeployment/getSeldonSingleModelDeployment.json");

        String responseAsString = performRequest(patch(URL + DEPLOYMENT_NAME), "dto/patchSingleModelReplicas1.json")
                .andReturn().getResponse().getContentAsString();
        ModelDeploymentResponse modelDeploymentResponse =
                new ObjectMapper().readValue(responseAsString, ModelDeploymentResponse.class);
        String expectedRegExp = "Nothing to do.* 1 instance";
        Pattern pattern = Pattern.compile(expectedRegExp);

        assertTrue(pattern.matcher(modelDeploymentResponse.message).matches(), "Expected return value was: \""
                + expectedRegExp + "\", but found: \"" + modelDeploymentResponse.message + "\"");
    }

    @Test
    void testModelPatchAutoscalingNothingToDo() throws Exception {
        preparePatch("seldondeployment/getSeldonSingleModelDeploymentWithAutoscaling.json");

        String responseAsString =
                performRequest(patch(URL + DEPLOYMENT_NAME), "dto/patchSeldonSingeModelDeploymentWithAutoscaling2.json")
                        .andReturn().getResponse().getContentAsString();
        ModelDeploymentResponse modelDeploymentResponse =
                new ObjectMapper().readValue(responseAsString, ModelDeploymentResponse.class);
        String expectedRegExp = "Nothing to do.* 1-3 instances, with autoscaling metrics cpu:12m";
        Pattern pattern = Pattern.compile(expectedRegExp);

        assertTrue(pattern.matcher(modelDeploymentResponse.message).matches(), "Expected return value was: \""
                + expectedRegExp + "\", but found: \"" + modelDeploymentResponse.message + "\"");
    }

    @Test
    void testModelPatchAutoscalingAndReplicasNothingToDo() throws Exception {
        preparePatch("seldondeployment/getSeldonSingleModelDeploymentWithAutoscaling.json");

        String responseAsString = performRequest(patch(URL + DEPLOYMENT_NAME),
                "dto/patchSeldonSingeModelDeploymentWithAutoscalingAndReplicas.json").andReturn().getResponse()
                        .getContentAsString();
        ModelDeploymentResponse modelDeploymentResponse =
                new ObjectMapper().readValue(responseAsString, ModelDeploymentResponse.class);
        String expectedRegExp = "Nothing to do.* 1-3 instances, with autoscaling metrics cpu:12m";
        Pattern pattern = Pattern.compile(expectedRegExp);

        assertTrue(pattern.matcher(modelDeploymentResponse.message).matches(), "Expected return value was: \""
                + expectedRegExp + "\", but found: \"" + modelDeploymentResponse.message + "\"");
    }

    private void preparePatch(String inputPath) throws ApiException, IOException {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                DEPLOYMENT_NAME)).thenReturn(getSeldonDeploymentFromFile(inputPath));
    }

    private void verifyPatch(String resultPath)
            throws ApiException, IOException, JsonParseException, JsonMappingException {
        String patchedDeployment = getStringFromResource(resultPath);
        verify(kubernetesService, atLeastOnce()).getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE,
                SELDON_PLURAL, DEPLOYMENT_NAME);
        verify(kubernetesService).patchNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                DEPLOYMENT_NAME, new ObjectMapper().readValue(patchedDeployment, Object.class));
    }

}

