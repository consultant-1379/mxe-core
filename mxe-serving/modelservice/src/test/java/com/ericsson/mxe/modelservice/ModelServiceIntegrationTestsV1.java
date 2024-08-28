package com.ericsson.mxe.modelservice;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ModelServiceIntegrationTestsV1 extends ModelServiceIntegrationTestBaseV1 {

    @Test
    void testCreateModelDeployment() throws Exception {
        Object obj =
                getSeldonDeploymentFromFileWithReplicas("seldondeployment/createSeldonDeploymentSingleModel.json", 5);

        performRequest(post(URL), "dto/createModelDeployment.json").andExpect(status().isOk());

        verify(kubernetesService).createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                obj);
    }

    @Test
    void testCreateModelDeploymentNA() throws Exception {
        performRequest(post(URL), "dto/createModelDeployment_NA.json").andExpect(status().isConflict());

        verify(kubernetesService, never()).createNamespacedCustomObject(any(), any(), any(), any(), any());
    }

    @Test
    void testCreateModelDeploymentWithName() throws Exception {
        Object obj = getSeldonDeploymentFromFileWithReplicas(
                "seldondeployment/createSeldonDeploymentSingleModelWithName.json", 5);

        performRequest(post(URL), "dto/createModelDeploymentWithName.json").andExpect(status().isOk());

        verify(kubernetesService).createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                obj);
    }

    @Test
    void testCreateModelDeploymentWithAutoscaling() throws Exception {
        Object obj = getSeldonDeploymentFromFileWithReplicas(
                "seldondeployment/createSeldonDeploymentSingleModelWithAutoscaling.json", 5);

        performRequest(post(URL), "dto/createModelDeploymentWithAutoscaling.json").andExpect(status().isOk());

        verify(kubernetesService).createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                obj);
    }

    @Test
    void testCreateStaticDeploymentWithAutoscaling() throws Exception {
        Object obj = getSeldonDeploymentFromFileWithReplicas(
                "seldondeployment/createSeldonDeploymentABTestWithAutoscaling.json", 5);

        performRequest(post(URL), "dto/createABTestDeploymentWithAutoscaling.json").andExpect(status().isOk());

        verify(kubernetesService).createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                obj);
    }

    @Test
    void testCreateModelDeploymentWithoutReplicas() throws Exception {
        Object obj =
                getSeldonDeploymentFromFileWithReplicas("seldondeployment/createSeldonDeploymentSingleModel.json", 1);

        performRequest(post(URL), "dto/createModelDeploymentWithoutReplicas.json").andExpect(status().isOk());

        verify(kubernetesService).createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                obj);
    }

    @Test
    void testCreateModelDeploymentAlreadyExistent() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                DEPLOYMENT_NAME_IMAGERECOGNITION)).thenReturn(
                        getSeldonDeploymentFromFileWithReplicas("seldondeployment/getSeldonSingleModelDeployment.json",
                                5));

        when(kubernetesService.createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                getSeldonDeploymentFromFileWithReplicas("seldondeployment/createSeldonDeploymentSingleModel.json", 5)))
                        .thenThrow(new ApiException(HttpStatus.CONFLICT.value(), ""));

        performRequest(post(URL), "dto/createModelDeployment.json").andExpect(status().isConflict());

    }

    @Test
    void testCreateModelDeploymentWithNameAlreadyExistent() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                DEPLOYMENT_NAME_IMAGERECOGNITION)).thenReturn(
                        getSeldonDeploymentFromFileWithReplicas("seldondeployment/getSeldonSingleModelDeployment.json",
                                5));

        when(kubernetesService.createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                getSeldonDeploymentFromFileWithReplicas(
                        "seldondeployment/createSeldonDeploymentSingleModelWithName.json", 5)))
                                .thenThrow(new ApiException(HttpStatus.CONFLICT.value(), ""));

        performRequest(post(URL), "dto/createModelDeploymentWithName.json").andExpect(status().isConflict());

    }

    @Test
    void testCreateModelDeploymentWithUnknownPackage() throws Exception {
        performRequest(post(URL), "dto/createModelDeploymentUnknownPackage.json")
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void testCreateModelDeploymentWithInvalidJsonPayload() throws Exception {
        this.mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateModelDeploymentWithoutPayload() throws Exception {
        this.mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void testCreateModelDeploymentWithMissingJsonFields() throws Exception {
        performRequest(post(URL), "dto/createModelDeploymentWithMissingField.json").andExpect(status().isBadRequest());
    }

    @Test
    void testGetModelDeployment() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                DEPLOYMENT_NAME)).thenReturn(
                        getSeldonDeploymentFromFile("seldondeployment/getSeldonSingleModelDeployment.json"));

        performRequest(get(URL + DEPLOYMENT_NAME), null).andExpect(status().isOk())
                .andExpect(content().json(getStringFromResource("dto/getModelDeployment.json"), true));
    }

    @Test
    void testGetModelDeploymentWithAutoscaling() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                DEPLOYMENT_NAME))
                        .thenReturn(getSeldonDeploymentFromFile(
                                "seldondeployment/getSeldonSingleModelDeploymentWithAutoscaling.json"));

        performRequest(get(URL + DEPLOYMENT_NAME), null).andExpect(status().isOk())
                .andExpect(content().json(getStringFromResource("dto/getModelDeploymentWithAutoscaling.json"), true));
    }

    @Test
    void testGetStaticDeploymentWithAutoscaling() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                DEPLOYMENT_NAME)).thenReturn(
                        getSeldonDeploymentFromFile("seldondeployment/getSeldonABTestDeploymentWithAutoscaling.json"));

        performRequest(get(URL + DEPLOYMENT_NAME), null).andExpect(status().isOk())
                .andExpect(content().json(getStringFromResource("dto/getStaticServiceWithAutoscaling.json"), true));
    }

    @Test
    void testGetModelDeploymentWithUnknownImageDoesNotFail() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                DEPLOYMENT_NAME)).thenReturn(
                        getSeldonDeploymentFromFile("seldondeployment/getSeldonDeploymentUnknownImage.json"));

        performRequest(get(URL + DEPLOYMENT_NAME), null).andExpect(status().isOk());
    }

    @Test
    void testGetModelDeploymentNonExistent() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                DEPLOYMENT_NAME)).thenThrow(new ApiException(HttpStatus.NOT_FOUND.value(), ""));

        performRequest(get(URL + DEPLOYMENT_NAME), null).andExpect(status().isNotFound());
    }

    @Test
    void testGetModelDeployments() throws Exception {
        when(kubernetesService.listNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL))
                .thenReturn(getSeldonDeploymentFromFile("seldondeployment/getSeldonDeployments.json"));

        performRequest(get(URL), null).andExpect(status().isOk())
                .andExpect(content().json(getStringFromResource("dto/getModelDeployments.json"), true));
    }

    @Test
    void testGetModelDeploymentsNoStatus() throws Exception {
        when(kubernetesService.listNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL))
                .thenReturn(getSeldonDeploymentFromFile("seldondeployment/getSeldonDeploymentsNoStatus.json"));

        performRequest(get(URL), null).andExpect(status().isOk())
                .andExpect(content().json(getStringFromResource("dto/getModelDeploymentsNoStatus.json"), true));
    }

    @Test
    void testGetStaticDeployment() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                DEPLOYMENT_NAME))
                        .thenReturn(getSeldonDeploymentFromFile("seldondeployment/getSeldonABTestDeployment.json"));

        performRequest(get(URL + DEPLOYMENT_NAME), null).andExpect(status().isOk())
                .andExpect(content().json(getStringFromResource("dto/getStaticService.json"), true));
    }

    @Test
    void testGetStaticDeploymentNoRatioA() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                DEPLOYMENT_NAME)).thenReturn(
                        getSeldonDeploymentFromFile("seldondeployment/getSeldonABTestDeploymentInvalidRatioA.json"));

        performRequest(get(URL + DEPLOYMENT_NAME), null).andExpect(status().isOk())
                .andExpect(content().json(getStringFromResource("dto/getStaticServiceNoWeights.json"), true));
    }

    @Test
    void testDeleteModelDeployment() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                DEPLOYMENT_NAME)).thenReturn(
                        getSeldonDeploymentFromFile("seldondeployment/getSeldonSingleModelDeployment.json"));
        performRequest(delete(URL + DEPLOYMENT_NAME), null).andExpect(status().isOk());

        verify(kubernetesService).deleteNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                DEPLOYMENT_NAME, new V1DeleteOptions());
    }

    @Test
    void testDeleteModelDeploymentNonExistent() throws Exception {
        when(kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                DEPLOYMENT_NAME)).thenReturn(
                        getSeldonDeploymentFromFile("seldondeployment/getSeldonSingleModelDeployment.json"));
        when(kubernetesService.deleteNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                DEPLOYMENT_NAME, new V1DeleteOptions())).thenThrow(new ApiException(HttpStatus.NOT_FOUND.value(), ""));

        performRequest(delete(URL + DEPLOYMENT_NAME), null).andExpect(status().isNotFound());

        verify(kubernetesService).deleteNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION, NAMESPACE, SELDON_PLURAL,
                DEPLOYMENT_NAME, new V1DeleteOptions());
    }

}

