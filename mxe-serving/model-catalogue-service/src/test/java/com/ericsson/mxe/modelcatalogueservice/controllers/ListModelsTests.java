package com.ericsson.mxe.modelcatalogueservice.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.ericsson.mxe.backendservicescommon.dto.status.PackageStatus;
import com.ericsson.mxe.modelcatalogueservice.IntegrationTests;
import com.ericsson.mxe.modelcatalogueservice.dto.Model;
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.backendservicescommon.kubernetes.ServicePortResolverService;
import com.ericsson.mxe.modelcatalogueservice.persistence.domain.ModelEntity;
import com.ericsson.mxe.modelcatalogueservice.persistence.domain.ModelEntityId;
import com.fasterxml.jackson.databind.ObjectMapper;

@IntegrationTests
class ListModelsTests extends ModelTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    @Qualifier("entityAndDtoMapper")
    ModelMapper entityAndDtoMapper;

    @MockBean
    KubernetesService kubernetesService;
    @MockBean
    ServicePortResolverService servicePortResolverService;

    private List<ModelEntity> modelEntities;

    @BeforeEach
    void init() {
        final String namespace = "namespace";

        when(kubernetesService.getNamespace()).thenReturn(namespace);
        when(servicePortResolverService.resolve(any(), any(), any())).thenReturn(Optional.of(22));

        final ModelEntity model1 = initEntity(new ModelEntityId("name1", "version1"), "title1", "author1",
                "description1", "image1", OffsetDateTime.parse("2019-01-01T12:50:43.953571+05:30"), "icon1",
                PackageStatus.Available, "message1", null, false, null);
        final ModelEntity model2 = initEntity(new ModelEntityId("name2", "version2"), "title2", "author2",
                "description2", "image2", OffsetDateTime.parse("2019-02-02T12:50:43.953571+05:30"), "icon2",
                PackageStatus.Available, "message1", null, false, null);
        final ModelEntity model3 = initEntity(new ModelEntityId("name3", "version3"), "title3", "author3",
                "description3", "image3", OffsetDateTime.parse("2019-03-03T12:50:43.953571+05:30"), "icon3",
                PackageStatus.Available, null, null, false, null);

        modelEntities = new ArrayList<>();

        modelEntities.add(model1);
        modelEntities.add(model2);
        modelEntities.add(model3);
    }

    @Test
    void shouldListStoredEntities() throws Exception {
        for (ModelEntity entity : modelEntities) {
            entityManager.persistAndFlush(entity);
        }

        List<Model> returnModels = objectMapper.readValue(
                mockMvc.perform(get("/v1/models")).andReturn().getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Model.class));

        Model[] models =
                modelEntities.stream().map(entity -> entityAndDtoMapper.map(entity, Model.class)).toArray(Model[]::new);

        assertThat(returnModels).containsExactlyInAnyOrder(models);
    }

    @Test
    void shouldNotListAnythingWhenThereAreNoStoredEntities() throws Exception {
        List<Model> returnModels = objectMapper.readValue(
                mockMvc.perform(get("/v1/models")).andReturn().getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Model.class));

        assertThat(returnModels).isEmpty();
    }
}

