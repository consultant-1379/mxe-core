package com.ericsson.mxe.modeltrainingservice.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.ericsson.mxe.backendservicescommon.dto.status.PackageStatus;
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.backendservicescommon.kubernetes.ServicePortResolverService;
import com.ericsson.mxe.modeltrainingservice.config.properties.ServiceMeshProperties;
import com.ericsson.mxe.modeltrainingservice.dto.status.TrainingJobStatus;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingJobEntity;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingPackageEntity;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingPackageEntityId;
import com.fasterxml.jackson.databind.ObjectMapper;

class TrainingJobTestBase {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected TestEntityManager entityManager;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    @Qualifier("entityAndDtoMapper")
    ModelMapper entityAndDtoMapper;

    @MockBean
    KubernetesService kubernetesService;
    @MockBean
    ServiceMeshProperties serviceMeshProperties;

    @MockBean
    ServicePortResolverService servicePortResolverService;

    protected List<TrainingJobEntity> trainingJobEntities;

    protected TrainingPackageEntity trainingPackageEntity;

    protected final String testImageId = "testimage";
    protected final String testImageVersion = "0.0.1";
    protected final int numOfTestJobEntities = 3;

    @BeforeEach
    void init() {
        final String namespace = "namespace";

        when(kubernetesService.getNamespace()).thenReturn(namespace);
        when(servicePortResolverService.resolve(any(), any(), any())).thenReturn(Optional.of(22));
        when(serviceMeshProperties.isMtlsEnabled()).thenReturn(false);

        trainingPackageEntity = createTestEntity();

        trainingJobEntities = new ArrayList<>();

        trainingJobEntities.add(createTestEntity(TrainingJobStatus.Running, trainingPackageEntity));
        trainingJobEntities.add(createTestEntity(TrainingJobStatus.Running, trainingPackageEntity));
        trainingJobEntities.add(createTestEntity(TrainingJobStatus.Running, trainingPackageEntity));
    }

    private TrainingJobEntity createTestEntity(final TrainingJobStatus status,
            final TrainingPackageEntity trainingPackageEntity) {
        final TrainingJobEntity trainingJobEntity = new TrainingJobEntity();
        final int id;

        if (Objects.nonNull(trainingJobEntities)) {
            id = trainingJobEntities.size() + 1;
        } else {
            id = 1;
        }

        trainingJobEntity.setId("id" + id);
        trainingJobEntity.setTrainingPackageEntity(trainingPackageEntity);
        trainingJobEntity.setStatus(status);
        trainingJobEntity.setMessage(null);
        trainingJobEntity.setErrorLog(null);
        trainingJobEntity.setCreated(OffsetDateTime.parse("2019-01-01T12:50:43.953571+05:30"));
        trainingJobEntity.setCompleted(OffsetDateTime.parse("2019-01-02T12:50:43.953571+05:30"));

        return trainingJobEntity;
    }

    private TrainingPackageEntity createTestEntity() {
        final TrainingPackageEntity trainingPackageEntity = new TrainingPackageEntity();

        trainingPackageEntity.setId(new TrainingPackageEntityId(testImageId, testImageVersion));
        trainingPackageEntity.setImage(testImageId + ":" + testImageVersion);
        trainingPackageEntity.setTitle("title");
        trainingPackageEntity.setAuthor("author");
        trainingPackageEntity.setDescription("description");
        trainingPackageEntity.setCreated(OffsetDateTime.parse("2019-01-01T12:50:43.953571+05:30"));
        trainingPackageEntity.setIcon("icon");
        trainingPackageEntity.setStatus(PackageStatus.Available);
        trainingPackageEntity.setMessage(null);
        trainingPackageEntity.setErrorLog(null);
        trainingPackageEntity.setInternal(false);

        return trainingPackageEntity;
    }
}
