package com.ericsson.mxe.modeltrainingservice.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.backendservicescommon.kubernetes.ServicePortResolverService;
import com.ericsson.mxe.modeltrainingservice.IntegrationTests;
import com.ericsson.mxe.modeltrainingservice.dto.TrainingPackage;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingPackageEntity;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingPackageEntityId;
import com.fasterxml.jackson.databind.ObjectMapper;

@IntegrationTests
class ListTrainingPackagesTests {
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

    private List<TrainingPackageEntity> trainingPackageEntities;

    @BeforeEach
    void init() {
        final String namespace = "namespace";

        when(kubernetesService.getNamespace()).thenReturn(namespace);
        when(servicePortResolverService.resolve(any(), any(), any())).thenReturn(Optional.of(22));

        trainingPackageEntities = new ArrayList<>();

        trainingPackageEntities.add(createTestEntity(PackageStatus.Available));
        trainingPackageEntities.add(createTestEntity(PackageStatus.Available));
        trainingPackageEntities.add(createTestEntity(PackageStatus.Available));
    }

    @Test
    void shouldListStoredEntities() throws Exception {
        for (TrainingPackageEntity entity : trainingPackageEntities) {
            entityManager.persistAndFlush(entity);
        }

        List<TrainingPackage> queriedTrainingPackages = objectMapper.readValue(
                mockMvc.perform(get("/v1/training-packages")).andReturn().getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, TrainingPackage.class));

        TrainingPackage[] trainingPackages = trainingPackageEntities.stream()
                .map(entity -> entityAndDtoMapper.map(entity, TrainingPackage.class)).toArray(TrainingPackage[]::new);

        assertThat(queriedTrainingPackages).containsExactlyInAnyOrder(trainingPackages);
    }

    @Test
    void shouldNotListAnythingWhenThereAreNoStoredEntities() throws Exception {
        List<TrainingPackage> queriedTrainingPackages = objectMapper.readValue(
                mockMvc.perform(get("/v1/training-packages")).andReturn().getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, TrainingPackage.class));

        assertThat(queriedTrainingPackages).isEmpty();
    }

    private TrainingPackageEntity createTestEntity(final PackageStatus status) {
        final TrainingPackageEntity trainingPackageEntity = new TrainingPackageEntity();
        final int id;

        if (Objects.nonNull(trainingPackageEntities)) {
            id = trainingPackageEntities.size() + 1;
        } else {
            id = 1;
        }

        trainingPackageEntity.setId(new TrainingPackageEntityId("id" + id, "version" + id));
        trainingPackageEntity.setTitle("title" + id);
        trainingPackageEntity.setAuthor("author" + id);
        trainingPackageEntity.setDescription("description" + id);
        trainingPackageEntity.setImage("id" + id + ":version" + id);
        trainingPackageEntity.setCreated(OffsetDateTime.parse("2019-01-01T12:50:43.953571+05:30"));
        trainingPackageEntity.setIcon("icon" + id);
        trainingPackageEntity.setStatus(status);
        trainingPackageEntity.setMessage(null);
        trainingPackageEntity.setErrorLog(null);
        trainingPackageEntity.setInternal(false);

        return trainingPackageEntity;
    }
}
