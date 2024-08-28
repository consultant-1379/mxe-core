package com.ericsson.mxe.modelservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1DeleteOptions;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ModelServiceIntegrationTests extends ModelServiceIntegrationTestBase {

    @Test
    void testCreateModelDeployment() throws Exception {
        Map seldonDeploymentJson = getSeldonDeploymentFromFileWithReplicas(
                "seldonmanifest/enriched/createSingleStatelessModelEnriched.json", 1);

        mockMvc.perform(getCreateModelDeploymentRequest("create-single-stateless-model.yaml", EMPTY_DOMAIN))
                .andExpect(status().isOk());

        verify(kubernetesService).createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                seldonDeploymentJson);
    }

    @Test
    void testCreateModelDeploymentMtlsEnabled() throws Exception {
        Map seldonDeploymentJson = getSeldonDeploymentFromFileWithReplicas(
                "seldonmanifest/enriched/createSingleStatelessModelWithMtlsEnriched.json", 1);

        when(serviceMeshProperties.isMtlsEnabled()).thenReturn(true);
        mockMvc.perform(getCreateModelDeploymentRequest("create-single-stateless-model.yaml", EMPTY_DOMAIN))
                .andExpect(status().isOk());

        verify(kubernetesService).createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                seldonDeploymentJson);
    }

    @Test
    void testCreateModelDeploymentWithDeployerRepo() throws Exception {
        Map seldonDeploymentJson = getSeldonDeploymentFromFileWithReplicas(
                "seldonmanifest/enriched/createSingleStatelessModelEnriched.json", 1);


        mockMvc.perform(getCreateModelDeploymentRequest("create-single-stateless-model.yaml", EMPTY_DOMAIN,
                TEST_DEPLOY_REPO_URL)).andExpect(status().isOk());

        verify(kubernetesService).createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                seldonDeploymentJson);
    }


    @Test
    void testCreateModelDeploymentNA() throws Exception {

        mockMvc.perform(getCreateModelDeploymentRequest("create-single-stateless-model-na.yaml", EMPTY_DOMAIN))
                .andExpect(status().isBadRequest());

        verify(kubernetesService, never()).createNamespacedCustomObject(any(), any(), any(), any(), any(Map.class));
    }

    @Test
    void testCreateModelDeploymentInvalidReplicas() throws Exception {

        mockMvc.perform(
                getCreateModelDeploymentRequest("create-single-stateless-model-invalid-replicas.yaml", EMPTY_DOMAIN))
                .andExpect(status().is4xxClientError());

        verify(kubernetesService, never()).createNamespacedCustomObject(any(), any(), any(), any(), any(Map.class));
    }

    @Test
    void testCreateModelDeploymentWithInvalidMinReplicas() throws Exception {
        mockMvc.perform(getCreateModelDeploymentRequest(
                "create-single-stateless-model-autoscaling-invalid-minreplicas.yaml", EMPTY_DOMAIN))
                .andExpect(status().isBadRequest());
        verify(kubernetesService, never()).createNamespacedCustomObject(any(), any(), any(), any(), any(Map.class));
    }

    @Test
    void testCreateModelDeploymentWithInvalidMaxReplicas() throws Exception {
        mockMvc.perform(getCreateModelDeploymentRequest(
                "create-single-stateless-model-autoscaling-invalid-maxreplicas.yaml", EMPTY_DOMAIN))
                .andExpect(status().isBadRequest());
        verify(kubernetesService, never()).createNamespacedCustomObject(any(), any(), any(), any(), any(Map.class));
    }

    @Test
    void testCreateModelDeploymentInvalidImageCount() throws Exception {

        mockMvc.perform(
                getCreateModelDeploymentRequest("create-single-stateless-model-invalid-image-count.yaml", EMPTY_DOMAIN))
                .andExpect(status().is4xxClientError());

        verify(kubernetesService, never()).createNamespacedCustomObject(any(), any(), any(), any(), any(Map.class));
    }

    @Test
    void testCreateModelDeploymentWithInvalidReplicasValidHpaSpecPass() throws Exception {
        Map seldonDeploymentJson = getSeldonDeploymentFromFileWithReplicas(
                "seldonmanifest/enriched/createSingleStatelessModelAutoscalingEnriched.json", -1);
        mockMvc.perform(getCreateModelDeploymentRequest(
                "create-single-stateless-model-autoscaling-invalid-replicas.yaml", EMPTY_DOMAIN))
                .andExpect(status().isOk());

        verify(kubernetesService).createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                seldonDeploymentJson);
    }

    @Test
    void testCreateModelDeploymentWithReplica5() throws Exception {
        Map seldonDeploymentJson = getSeldonDeploymentFromFileWithReplicas(
                "seldonmanifest/enriched/createSingleStatelessModelEnriched.json", 5);

        mockMvc.perform(getCreateModelDeploymentRequest("create-single-stateless-model-replica5.yaml", EMPTY_DOMAIN))
                .andExpect(status().isOk());

        verify(kubernetesService).createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                seldonDeploymentJson);
    }

    @Test
    void testCreateModelDeploymentWithAutoscaling() throws Exception {
        Map seldonDeploymentJson = getSeldonDeploymentFromFileWithReplicas(
                "seldonmanifest/enriched/createSingleStatelessModelAutoscalingEnriched.json", 1);

        mockMvc.perform(getCreateModelDeploymentRequest("create-single-stateless-model-autoscaling.yaml", EMPTY_DOMAIN))
                .andExpect(status().isOk());
        verify(kubernetesService).createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                seldonDeploymentJson);
    }

    @Test
    void testCreateStaticDeploymentWithAutoscaling() throws Exception {
        Map seldonDeploymentJson = getSeldonDeploymentFromFileWithReplicas(
                "seldonmanifest/enriched/createABStatelessModelAutoscalingEnriched.json", 1);

        mockMvc.perform(getCreateModelDeploymentRequest("create-abtest-stateless-model-autoscaling.yaml", EMPTY_DOMAIN))
                .andExpect(status().isOk());
        verify(kubernetesService).createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                seldonDeploymentJson);
    }

    @Test
    void testCreateStaticDeploymentWithInvalidWeightFail() throws Exception {
        mockMvc.perform(
                getCreateModelDeploymentRequest("create-abtest-stateless-model-invalid-weight.yaml", EMPTY_DOMAIN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateStaticDeploymentWithInvalidReplicasFail() throws Exception {
        mockMvc.perform(
                getCreateModelDeploymentRequest("create-abtest-stateless-model-invalid-replicas.yaml", EMPTY_DOMAIN))
                .andExpect(status().isBadRequest());
        verify(kubernetesService, never()).createNamespacedCustomObject(any(), any(), any(), any(), any(Map.class));
    }

    @Test
    void testCreateStaticDeploymentWithInvalidImageCountFail() throws Exception {

        mockMvc.perform(
                getCreateModelDeploymentRequest("create-abtest-stateless-model-invalid-image-count.yaml", EMPTY_DOMAIN))
                .andExpect(status().isBadRequest());

        verify(kubernetesService, never()).createNamespacedCustomObject(any(), any(), any(), any(), any(Map.class));
    }


    @Test
    void testCreateStaticDeploymentWithInvalidMinReplicas() throws Exception {
        mockMvc.perform(getCreateModelDeploymentRequest(
                "create-abtest-stateless-model-autoscaling-invalid-minreplicas.yaml", EMPTY_DOMAIN))
                .andExpect(status().isBadRequest());
        verify(kubernetesService, never()).createNamespacedCustomObject(any(), any(), any(), any(), any(Map.class));
    }

    @Test
    void testCreateStaticDeploymentWithInvalidMaxReplicas() throws Exception {
        mockMvc.perform(getCreateModelDeploymentRequest(
                "create-abtest-stateless-model-autoscaling-invalid-maxreplicas.yaml", EMPTY_DOMAIN))
                .andExpect(status().isBadRequest());
        verify(kubernetesService, never()).createNamespacedCustomObject(any(), any(), any(), any(), any(Map.class));
    }

    @Test
    void testCreateStaticDeploymentWithInvalidReplicasValidHpaSpecPass() throws Exception {
        Map seldonDeploymentJson = getSeldonDeploymentFromFileWithReplicas(
                "seldonmanifest/enriched/createABStatelessModelAutoscalingEnriched.json", -1);
        mockMvc.perform(getCreateModelDeploymentRequest(
                "create-abtest-stateless-model-autoscaling-invalid-replicas.yaml", EMPTY_DOMAIN))
                .andExpect(status().isOk());
        verify(kubernetesService).createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                seldonDeploymentJson);
    }

    // @Test
    // void testCreateModelDeploymentWithoutReplicas() throws Exception {
    // Object obj =
    // getSeldonDeploymentFromFileWithReplicas("seldondeployment/createSeldonDeploymentSingleModel.json", 1);
    //
    // performRequest(post(URL), "dto/createModelDeploymentWithoutReplicas.json").andExpect(status().isOk());
    //
    // verify(kubernetesService).createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
    // obj);
    // }
    //
    @Test
    void testCreateModelDeploymentAlreadyExistent() throws Exception {

        Map seldonDeploymentJson = getSeldonDeploymentFromFileWithReplicas(
                "seldonmanifest/enriched/createSingleStatelessModelEnriched.json", 1);

        when(kubernetesService.createNamespacedCustomObjectDryRun(eq(SELDON_GROUP), eq(SELDON_VERSION), eq(NAMESPACE),
                eq(SELDON_PLURAL), any())).thenThrow(new ApiException(HttpStatus.CONFLICT.value(), ""));

        mockMvc.perform(getCreateModelDeploymentRequest("create-single-stateless-model.yaml", EMPTY_DOMAIN))
                .andExpect(status().isConflict());

        verify(kubernetesService, never()).createNamespacedCustomObject(any(), any(), any(), any(), any(Map.class));
    }

    //
    // @Test
    // void testCreateModelDeploymentWithNameAlreadyExistent() throws Exception {
    // when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
    // DEPLOYMENT_NAME_IMAGERECOGNITION)).thenReturn(
    // getSeldonDeploymentFromFileWithReplicas("seldondeployment/getSeldonSingleModelDeployment.json",
    // 5));
    //
    // when(kubernetesService.createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
    // getSeldonDeploymentFromFileWithReplicas(
    // "seldondeployment/createSeldonDeploymentSingleModelWithName.json", 5)))
    // .thenThrow(new ApiException(HttpStatus.CONFLICT.value(), ""));
    //
    // performRequest(post(URL), "dto/createModelDeploymentWithName.json").andExpect(status().isConflict());
    //
    // }
    //
    @Test
    void testCreateModelDeploymentWithUnknownPackage() throws Exception {
        mockMvc.perform(getCreateModelDeploymentRequest("create-single-stateless-model-na.yaml", EMPTY_DOMAIN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateModelDeploymentWithInvalidYamlFile() throws Exception {
        mockMvc.perform(
                getCreateModelDeploymentRequest("create-single-stateless-model-invalid-yaml.yaml", EMPTY_DOMAIN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateModelDeploymentWithInvalidFileExtension() throws Exception {
        mockMvc.perform(
                getCreateModelDeploymentRequest("create-single-stateless-model-invalid-yaml-extn.txt", EMPTY_DOMAIN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateModelDeploymentWithoutPayload() throws Exception {
        mockMvc.perform(getCreateModelDeploymentRequest(null, EMPTY_DOMAIN)).andExpect(status().isBadRequest());
    }

    @Test
    void testCreateModelDeploymentWithNameField() throws Exception {
        mockMvc.perform(
                getCreateModelDeploymentRequest("create-single-stateless-model-missing-name.yaml", EMPTY_DOMAIN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateModelDeploymentWithoutContainerImageFail() throws Exception {
        mockMvc.perform(
                getCreateModelDeploymentRequest("create-single-stateless-model-missing-image.yaml", EMPTY_DOMAIN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetModelDeployment() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                TEST_SINGLE_STATELESS_NAME)).thenReturn(
                        getSeldonDeploymentFromFile("seldonmanifest/clusterdata/getSeldonSingleModelDeployment.json"));

        performRequest(get(URL + "/" + TEST_SINGLE_STATELESS_NAME), null).andExpect(status().isOk()).andExpect(
                content().json(getStringFromResource("seldonmanifest/response/getModelDeployment.json"), true));
    }

    @Test
    void testGetModelDeploymentWithAutoscaling() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                TEST_SINGLE_STATELESS_NAME))
                        .thenReturn(getSeldonDeploymentFromFile(
                                "seldonmanifest/clusterdata/getSeldonSingleModelDeploymentWithAutoscaling.json"));

        performRequest(get(URL + "/" + TEST_SINGLE_STATELESS_NAME), null).andExpect(status().isOk()).andExpect(content()
                .json(getStringFromResource("seldonmanifest/response/getModelDeploymentWithAutoscaling.json"), true));
    }

    @Test
    void testGetStaticDeploymentWithAutoscaling() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                TEST_AB_STATELESS_NAME))
                        .thenReturn(getSeldonDeploymentFromFile(
                                "seldonmanifest/clusterdata/getSeldonABTestDeploymentWithAutoscaling.json"));

        performRequest(get(URL + "/" + TEST_AB_STATELESS_NAME), null).andExpect(status().isOk()).andExpect(content()
                .json(getStringFromResource("seldonmanifest/response/getStaticServiceWithAutoscaling.json"), true));
    }

    @Test
    void testGetModelDeploymentWithUnknownImageDoesNotFail() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                TEST_SINGLE_STATELESS_NAME)).thenReturn(
                        getSeldonDeploymentFromFile("seldonmanifest/clusterdata/getSeldonDeploymentUnknownImage.json"));

        performRequest(get(URL + "/" + TEST_SINGLE_STATELESS_NAME), null).andExpect(status().isOk());
    }

    @Test
    void testGetModelDeploymentNonExistent() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                TEST_SINGLE_STATELESS_NAME)).thenThrow(new ApiException(HttpStatus.NOT_FOUND.value(), ""));

        performRequest(get(URL + "/" + TEST_SINGLE_STATELESS_NAME), null).andExpect(status().isNotFound());
    }

    @Test
    void testGetModelDeployments() throws Exception {
        when(kubernetesService.listNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL))
                .thenReturn(getSeldonDeploymentFromFile("seldonmanifest/clusterdata/getSeldonDeployments.json"));

        performRequest(get(URL), null).andExpect(status().isOk()).andExpect(
                content().json(getStringFromResource("seldonmanifest/response/getModelDeployments.json"), true));
    }

    @Test
    void testGetModelDeploymentsNoStatus() throws Exception {
        when(kubernetesService.listNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL))
                .thenReturn(
                        getSeldonDeploymentFromFile("seldonmanifest/clusterdata/getSeldonDeploymentsNoStatus.json"));

        performRequest(get(URL), null).andExpect(status().isOk())
                .andExpect(content().json(getStringFromResource("dto/getModelDeploymentsNoStatus.json"), true));
    }

    @Test
    void testGetStaticDeployment() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                TEST_AB_STATELESS_NAME)).thenReturn(
                        getSeldonDeploymentFromFile("seldonmanifest/clusterdata/getSeldonABTestDeployment.json"));

        performRequest(get(URL + "/" + TEST_AB_STATELESS_NAME), null).andExpect(status().isOk()).andExpect(
                content().json(getStringFromResource("seldonmanifest/response/getStaticService.json"), true));
    }

    @Test
    void testGetStaticDeploymentNoRatioA() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                TEST_AB_STATELESS_NAME))
                        .thenReturn(getSeldonDeploymentFromFile(
                                "seldonmanifest/clusterdata/getSeldonABTestDeploymentInvalidRatioA.json"));

        performRequest(get(URL + "/" + TEST_AB_STATELESS_NAME), null).andExpect(status().isOk()).andExpect(
                content().json(getStringFromResource("seldonmanifest/response/getStaticServiceNoWeights.json"), true));
    }

    @Test
    void testDeleteModelDeployment() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                TEST_SINGLE_STATELESS_NAME)).thenReturn(
                        getSeldonDeploymentFromFile("seldonmanifest/clusterdata/getSeldonSingleModelDeployment.json"));
        performRequest(delete(URL + "/" + TEST_SINGLE_STATELESS_NAME), null).andExpect(status().isOk());

        verify(kubernetesService).deleteNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                TEST_SINGLE_STATELESS_NAME, new V1DeleteOptions());
    }

    @Test
    void testDeleteModelDeploymentNonExistent() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                TEST_SINGLE_STATELESS_NAME)).thenReturn(
                        getSeldonDeploymentFromFile("seldonmanifest/clusterdata/getSeldonSingleModelDeployment.json"));
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                TEST_SINGLE_STATELESS_NAME)).thenThrow(new ApiException(HttpStatus.NOT_FOUND.value(), ""));

        performRequest(delete(URL + "/" + TEST_SINGLE_STATELESS_NAME), null).andExpect(status().isNotFound());

        verify(kubernetesService, never()).deleteNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE,
                SELDON_PLURAL, TEST_SINGLE_STATELESS_NAME, new V1DeleteOptions());
    }

}

