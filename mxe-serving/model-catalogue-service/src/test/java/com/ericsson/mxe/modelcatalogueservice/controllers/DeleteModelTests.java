package com.ericsson.mxe.modelcatalogueservice.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import com.ericsson.mxe.backendservicescommon.dto.status.PackageStatus;
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.backendservicescommon.kubernetes.ServicePortResolverService;
import com.ericsson.mxe.modelcatalogueservice.IntegrationTests;
import com.ericsson.mxe.modelcatalogueservice.config.properties.ModelDeploymentServiceProperties;
import com.ericsson.mxe.modelcatalogueservice.dto.ModelDeployment;
import com.ericsson.mxe.modelcatalogueservice.dto.ModelDeploymentModelData;
import com.ericsson.mxe.modelcatalogueservice.persistence.domain.ModelEntity;
import com.ericsson.mxe.modelcatalogueservice.persistence.domain.ModelEntityId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@IntegrationTests
public class DeleteModelTests extends ModelTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockRestServiceServer mockServer;

    @MockBean
    KubernetesService kubernetesService;
    @MockBean
    ServicePortResolverService servicePortResolverService;
    @MockBean
    ModelDeploymentServiceProperties modelDeploymentServiceProperties;

    private String name1;
    private String version1;
    private String name2;
    private String version2;
    private URI modelDeploymentUrl;
    private List<ModelDeployment> modelDeployments;

    @BeforeEach
    void init() throws URISyntaxException {
        final String namespace = "namespace";
        final Integer port = 22;
        final String hostname = "hostname";

        when(kubernetesService.getNamespace()).thenReturn(namespace);
        when(servicePortResolverService.resolve(any(), any(), any())).thenReturn(Optional.of(port));
        when(modelDeploymentServiceProperties.getHostName()).thenReturn(hostname);

        modelDeploymentUrl = new URI("http://" + hostname + ":" + port.toString() + "/v1/model-services");

        modelDeployments = new ArrayList<>();

        name1 = "name1";
        version1 = "version1";
        name2 = "name2";
        version2 = "version2";

        final ModelEntity model1 = initEntity(new ModelEntityId(name1, version1), "title1", "author1", "description1",
                "image1", OffsetDateTime.parse("2019-01-01T12:50:43.953571+05:30"), "icon1", PackageStatus.Available,
                "message1", null, false, null);
        final ModelEntity model2 = initEntity(new ModelEntityId(name2, version2), "title2", "author2", "description2",
                "image2", OffsetDateTime.parse("2019-02-02T12:50:43.953571+05:30"), "icon2", PackageStatus.Packaging,
                "message1", null, false, null);
        final ModelEntity model3 = initEntity(new ModelEntityId("name3", "version3"), "title3", "author3",
                "description3", "image3", OffsetDateTime.parse("2019-03-03T12:50:43.953571+05:30"), "icon3",
                PackageStatus.Available, null, null, false, null);

        entityManager.persistAndFlush(model1);
        entityManager.persistAndFlush(model2);
        entityManager.persistAndFlush(model3);
    }

    @Test
    void shouldDeleteStoredModel() throws Exception {
        prepareMockServer();

        mockMvc.perform(delete("/v1/models/" + name1 + "/" + version1)).andExpect(status().isOk());

        assertThat(entityManager.find(ModelEntity.class, new ModelEntityId(name1, version1))).isNull();
    }

    @Test
    void shouldReturnNotFoundWhenModelIsCurrentlyOnboarded() throws Exception {
        prepareMockServer();

        mockMvc.perform(delete("/v1/models/" + name2 + "/" + version2)).andExpect(status().isConflict());
    }

    @Test
    void shouldReturnNotFoundWhenModelIsNotExists() throws Exception {
        prepareMockServer();

        final String notFoundName = "notFoundName";
        final String notFoundVersion = "notFoundVersion";

        mockMvc.perform(delete("/v1/models/" + notFoundName + "/" + notFoundVersion)).andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnForbiddenWhenModelIsDeployed() throws Exception {
        final List<ModelDeploymentModelData> modelDataList = new ArrayList<>();
        final ModelDeploymentModelData modelData = new ModelDeploymentModelData(name1, version1, "");
        modelDataList.add(modelData);
        final ModelDeployment modelDeployment = new ModelDeployment("", "", "", "", 0, modelDataList);
        this.modelDeployments.add(modelDeployment);

        prepareMockServer();

        mockMvc.perform(delete("/v1/models/" + name1 + "/" + version1)).andExpect(status().isConflict());
    }

    void prepareMockServer() throws JsonProcessingException {
        mockServer.expect(requestTo(modelDeploymentUrl)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(this.modelDeployments)));
    }
}
