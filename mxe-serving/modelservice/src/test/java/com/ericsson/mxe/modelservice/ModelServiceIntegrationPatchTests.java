package com.ericsson.mxe.modelservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.kubernetes.client.openapi.ApiException;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ModelServiceIntegrationPatchTests extends ModelServiceIntegrationTestBase {

    @Test
    void testModelPatchWithVersion() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonSingleModelDeployment.json", TEST_SINGLE_STATELESS_NAME);

        mockMvc.perform(getPatchModelDeploymentRequest(TEST_SINGLE_STATELESS_NAME,
                "patch-single-stateless-model-version.yaml", EMPTY_DOMAIN)).andExpect(status().isOk());

        verifyPatch("seldonmanifest/clusterdata/patchedSeldonSingleModelDeploymentWithVersion.json",
                TEST_SINGLE_STATELESS_NAME);
    }

    @Test
    void testModelPatchWithVersionMtlsEnabled() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonSingleModelDeployment.json", TEST_SINGLE_STATELESS_NAME);

        when(serviceMeshProperties.isMtlsEnabled()).thenReturn(true);
        mockMvc.perform(getPatchModelDeploymentRequest(TEST_SINGLE_STATELESS_NAME,
                "patch-single-stateless-model-version.yaml", EMPTY_DOMAIN)).andExpect(status().isOk());

        verifyPatch("seldonmanifest/clusterdata/patchedSeldonSingleModelDeploymentWithVersionMtlsEnabled.json",
                TEST_SINGLE_STATELESS_NAME);
    }

    @Test
    void testModelPatchDeploymentNameMisMatch() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonSingleModelDeployment.json", TEST_SINGLE_STATELESS_NAME);

        mockMvc.perform(getPatchModelDeploymentRequest(TEST_SINGLE_STATELESS_NAME,
                "patch-single-stateless-model-na.yaml", EMPTY_DOMAIN)).andExpect(status().isBadRequest());

        verify(kubernetesService, never()).replaceNamespacedCustomObject(any(), any(), any(), any(), any(),
                any(Map.class));
    }

    @Test
    void testModelPatchDeploymentInvalidReplicas() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonSingleModelDeployment.json", TEST_SINGLE_STATELESS_NAME);
        mockMvc.perform(getPatchModelDeploymentRequest(TEST_SINGLE_STATELESS_NAME,
                "patch-single-stateless-model-invalid-replicas.yaml", EMPTY_DOMAIN)).andExpect(status().isBadRequest());

        verify(kubernetesService, never()).replaceNamespacedCustomObject(any(), any(), any(), any(), any(),
                any(Map.class));
    }

    @Test
    void testModelPatchDeploymentAutoscalingInvalidMinReplicas() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonSingleModelDeployment.json", TEST_SINGLE_STATELESS_NAME);
        mockMvc.perform(getPatchModelDeploymentRequest(TEST_SINGLE_STATELESS_NAME,
                "patch-single-stateless-model-autoscaling-invalid-minreplicas.yaml", EMPTY_DOMAIN))
                .andExpect(status().isBadRequest());

        verify(kubernetesService, never()).replaceNamespacedCustomObject(any(), any(), any(), any(), any(),
                any(Map.class));
    }

    @Test
    void testModelPatchDeploymentAutoscalingInvalidMaxReplicas() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonSingleModelDeployment.json", TEST_SINGLE_STATELESS_NAME);
        mockMvc.perform(getPatchModelDeploymentRequest(TEST_SINGLE_STATELESS_NAME,
                "patch-single-stateless-model-autoscaling-invalid-maxreplicas.yaml", EMPTY_DOMAIN))
                .andExpect(status().isBadRequest());

        verify(kubernetesService, never()).replaceNamespacedCustomObject(any(), any(), any(), any(), any(),
                any(Map.class));
    }

    @Test
    void testModelPatchReplicasWithAutoscalingInvalidReplicas() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonSingleModelDeployment.json", TEST_SINGLE_STATELESS_NAME);

        mockMvc.perform(getPatchModelDeploymentRequest(TEST_SINGLE_STATELESS_NAME,
                "patch-single-stateless-model-autoscaling-invalid-replicas.yaml", EMPTY_DOMAIN))
                .andExpect(status().isOk());

        verifyPatch("seldonmanifest/clusterdata/patchedSeldonSingleModelDeploymentWithAutoscalingInvalidReplicas.json",
                TEST_SINGLE_STATELESS_NAME);
    }

    @Test
    void testModelPatchWithVersionNA() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonSingleModelDeployment.json", TEST_DEPLOY_MODEL_NA);

        mockMvc.perform(getPatchModelDeploymentRequest(TEST_DEPLOY_MODEL_NA, "patch-single-stateless-model-na.yaml",
                EMPTY_DOMAIN)).andExpect(status().isBadRequest());

        verify(kubernetesService, never()).replaceNamespacedCustomObject(any(), any(), any(), any(), any(),
                any(Map.class));
    }

    @Test
    void testABTestPatchWithVersion() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonABTestModelDeployment.json", TEST_AB_STATELESS_NAME);

        mockMvc.perform(getPatchModelDeploymentRequest(TEST_AB_STATELESS_NAME, "patch-ab-stateless-model-version.yaml",
                EMPTY_DOMAIN)).andExpect(status().isOk());

        verifyPatch("seldonmanifest/clusterdata/patchedSeldonABModelDeploymentWithVersion.json",
                TEST_AB_STATELESS_NAME);

    }

    @Test
    void testABTestPatchWithVersionNA() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonABTestModelDeployment.json", TEST_AB_STATELESS_NAME);

        mockMvc.perform(getPatchModelDeploymentRequest(TEST_AB_STATELESS_NAME, "patch-ab-stateless-model-na.yaml",
                EMPTY_DOMAIN)).andExpect(status().isBadRequest());

        verify(kubernetesService, never()).replaceNamespacedCustomObject(any(), any(), any(), any(), any(),
                any(Map.class));
    }

    @Test
    void testModelPatchReplicasWithAutoscaling() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonSingleModelDeployment.json", TEST_SINGLE_STATELESS_NAME);

        mockMvc.perform(getPatchModelDeploymentRequest(TEST_SINGLE_STATELESS_NAME,
                "patch-single-stateless-model-autoscaling.yaml", EMPTY_DOMAIN)).andExpect(status().isOk());

        verifyPatch("seldonmanifest/clusterdata/patchedSeldonSingleModelDeploymentWithAutoscaling.json",
                TEST_SINGLE_STATELESS_NAME);
    }

    @Test
    void testModelPatchAutoscalingWithPreviousReplicas() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonSingleModelDeploymentWithAutoscaling.json",
                TEST_SINGLE_STATELESS_NAME);

        mockMvc.perform(getPatchModelDeploymentRequest(TEST_SINGLE_STATELESS_NAME,
                "patch-single-stateless-model-replica1.yaml", EMPTY_DOMAIN)).andExpect(status().isOk());

        verifyPatch("seldonmanifest/clusterdata/patchedSeldonSingleModelDeploymentWithReplica1.json",
                TEST_SINGLE_STATELESS_NAME);
    }

    @Test
    void testModelPatchAutoscalingWithNewReplicas() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonSingleModelDeploymentWithAutoscaling.json",
                TEST_SINGLE_STATELESS_NAME);

        mockMvc.perform(getPatchModelDeploymentRequest(TEST_SINGLE_STATELESS_NAME,
                "patch-single-stateless-model-replica5.yaml", EMPTY_DOMAIN)).andExpect(status().isOk());

        verifyPatch("seldonmanifest/clusterdata/patchedSeldonSingleModelDeploymentWithReplica5.json",
                TEST_SINGLE_STATELESS_NAME);
    }

    @Test
    void testModelPatchAutoscalingWithAutoscaling() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonSingleModelDeploymentWithAutoscaling.json",
                TEST_SINGLE_STATELESS_NAME);

        mockMvc.perform(getPatchModelDeploymentRequest(TEST_SINGLE_STATELESS_NAME,
                "patch-single-stateless-model-autoscaling2.yaml", EMPTY_DOMAIN)).andExpect(status().isOk());

        verifyPatch("seldonmanifest/clusterdata/patchedSeldonSingleModelDeploymentWithAutoscaling2.json",
                TEST_SINGLE_STATELESS_NAME);
    }

    @Test
    void testModelPatchAutoscalingWithVersion() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonSingleModelDeploymentWithAutoscaling.json",
                TEST_SINGLE_STATELESS_NAME);

        mockMvc.perform(getPatchModelDeploymentRequest(TEST_SINGLE_STATELESS_NAME,
                "patch-single-stateless-model-autoscaling-version.yaml", EMPTY_DOMAIN)).andExpect(status().isOk());

        verifyPatch("seldonmanifest/clusterdata/patchedSeldonSingleModelDeploymentWithAutoscalingWithVersion.json",
                TEST_SINGLE_STATELESS_NAME);
    }

    //
    // @Test
    // void testModelPatchReplicasNothingToDo() throws Exception {
    // preparePatch("seldondeployment/getSeldonSingleModelDeployment.json");
    //
    // String responseAsString = performRequest(patch(URL + DEPLOYMENT_NAME), "dto/patchSingleModelReplicas1.json")
    // .andReturn().getResponse().getContentAsString();
    // ModelDeploymentResponse modelDeploymentResponse =
    // new ObjectMapper().readValue(responseAsString, ModelDeploymentResponse.class);
    // String expectedRegExp = "Nothing to do.* 1 instance";
    // Pattern pattern = Pattern.compile(expectedRegExp);
    //
    // assertTrue(pattern.matcher(modelDeploymentResponse.message).matches(), "Expected return value was: \""
    // + expectedRegExp + "\", but found: \"" + modelDeploymentResponse.message + "\"");
    // }
    //
    // @Test
    // void testModelPatchAutoscalingNothingToDo() throws Exception {
    // preparePatch("seldondeployment/getSeldonSingleModelDeploymentWithAutoscaling.json");
    //
    // String responseAsString =
    // performRequest(patch(URL + DEPLOYMENT_NAME), "dto/patchSeldonSingeModelDeploymentWithAutoscaling2.json")
    // .andReturn().getResponse().getContentAsString();
    // ModelDeploymentResponse modelDeploymentResponse =
    // new ObjectMapper().readValue(responseAsString, ModelDeploymentResponse.class);
    // String expectedRegExp = "Nothing to do.* 1-3 instances, with autoscaling metrics cpu:12m";
    // Pattern pattern = Pattern.compile(expectedRegExp);
    //
    // assertTrue(pattern.matcher(modelDeploymentResponse.message).matches(), "Expected return value was: \""
    // + expectedRegExp + "\", but found: \"" + modelDeploymentResponse.message + "\"");
    // }
    //
    // @Test
    // void testModelPatchAutoscalingAndReplicasNothingToDo() throws Exception {
    // preparePatch("seldondeployment/getSeldonSingleModelDeploymentWithAutoscaling.json");
    //
    // String responseAsString = performRequest(patch(URL + DEPLOYMENT_NAME),
    // "dto/patchSeldonSingeModelDeploymentWithAutoscalingAndReplicas.json").andReturn().getResponse()
    // .getContentAsString();
    // ModelDeploymentResponse modelDeploymentResponse =
    // new ObjectMapper().readValue(responseAsString, ModelDeploymentResponse.class);
    // String expectedRegExp = "Nothing to do.* 1-3 instances, with autoscaling metrics cpu:12m";
    // Pattern pattern = Pattern.compile(expectedRegExp);
    //
    // assertTrue(pattern.matcher(modelDeploymentResponse.message).matches(), "Expected return value was: \""
    // + expectedRegExp + "\", but found: \"" + modelDeploymentResponse.message + "\"");
    // }
    //
    @Test
    void testModificationOfDomainWhenDomainWasNull() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonSingleModelDeployment.json", TEST_SINGLE_STATELESS_NAME);

        mockMvc.perform(getPatchModelDeploymentRequest(TEST_SINGLE_STATELESS_NAME,
                "patch-single-stateless-model-replica1.yaml", "newDomain")).andExpect(status().isOk());

        verifyPatch("seldonmanifest/clusterdata/patchedSeldonSingleModelDeploymentWithDomain.json",
                TEST_SINGLE_STATELESS_NAME);
    }

    @Test
    void testModificationOfDomainWhenDomainExisted() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonSingleModelDeploymentWithDomain.json",
                TEST_SINGLE_STATELESS_NAME);

        mockMvc.perform(getPatchModelDeploymentRequest(TEST_SINGLE_STATELESS_NAME,
                "patch-single-stateless-model-replica1.yaml", "newDomain")).andExpect(status().isOk());

        verifyPatch("seldonmanifest/clusterdata/patchedSeldonSingleModelDeploymentWithDomain.json",
                TEST_SINGLE_STATELESS_NAME);
    }

    @Test
    void testModificationOfDomainRetainWhenNotPassed() throws Exception {
        preparePatch("seldonmanifest/clusterdata/getSeldonSingleModelDeploymentWithDomain.json",
                TEST_SINGLE_STATELESS_NAME);

        mockMvc.perform(getPatchModelDeploymentRequest(TEST_SINGLE_STATELESS_NAME,
                "patch-single-stateless-model-replica1.yaml", EMPTY_DOMAIN)).andExpect(status().isOk());

        verifyPatch("seldonmanifest/clusterdata/patchedSeldonSingleModelDeploymentWithOldDomain.json",
                TEST_SINGLE_STATELESS_NAME);
    }

    private void preparePatch(String inputPath, String deploymentName) throws ApiException, IOException {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                deploymentName)).thenReturn(getSeldonDeploymentFromFile(inputPath));
    }

    private void verifyPatch(String resultPath, String deploymentName)
            throws ApiException, IOException, JsonParseException, JsonMappingException {
        Map seldonDeploymentJson = getSeldonDeploymentFromFile(resultPath);
        verify(kubernetesService, atLeastOnce()).getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE,
                SELDON_PLURAL, deploymentName);
        verify(kubernetesService).replaceNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                deploymentName, seldonDeploymentJson);
    }

}

