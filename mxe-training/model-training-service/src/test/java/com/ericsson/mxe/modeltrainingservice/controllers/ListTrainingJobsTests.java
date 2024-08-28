package com.ericsson.mxe.modeltrainingservice.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.ericsson.mxe.modeltrainingservice.IntegrationTests;
import com.ericsson.mxe.modeltrainingservice.dto.TrainingJob;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingJobEntity;

@IntegrationTests
class ListTrainingJobsTests extends TrainingJobTestBase {
    @Test
    void shouldListStoredEntities() throws Exception {
        entityManager.persistAndFlush(trainingPackageEntity);
        for (TrainingJobEntity entity : trainingJobEntities) {
            entityManager.persistAndFlush(entity);
        }
        final List<TrainingJob> queriedTrainingJobs = objectMapper.readValue(
                mockMvc.perform(get("/v1/training-jobs")).andReturn().getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, TrainingJob.class));

        final TrainingJob[] trainingJobs = trainingJobEntities.stream()
                .map(entity -> entityAndDtoMapper.map(entity, TrainingJob.class)).toArray(TrainingJob[]::new);

        assertThat(queriedTrainingJobs).containsExactlyInAnyOrder(trainingJobs);
    }

    @Test
    void shouldListStoredEntitiesByPackageId() throws Exception {
        entityManager.persistAndFlush(trainingPackageEntity);
        for (TrainingJobEntity entity : trainingJobEntities) {
            entityManager.persistAndFlush(entity);
        }
        final List<TrainingJob> queriedTrainingJobs = objectMapper.readValue(
                mockMvc.perform(get("/v1/training-jobs?packageId=" + testImageId)).andReturn().getResponse()
                        .getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, TrainingJob.class));

        final TrainingJob[] trainingJobs = trainingJobEntities.stream()
                .map(entity -> entityAndDtoMapper.map(entity, TrainingJob.class)).toArray(TrainingJob[]::new);

        assertThat(queriedTrainingJobs).containsExactlyInAnyOrder(trainingJobs);
    }

    @Test
    void shouldListStoredEntitiesByPackage() throws Exception {
        entityManager.persistAndFlush(trainingPackageEntity);
        for (TrainingJobEntity entity : trainingJobEntities) {
            entityManager.persistAndFlush(entity);
        }
        final List<TrainingJob> queriedTrainingJobs = objectMapper.readValue(mockMvc
                .perform(get("/v1/training-jobs?packageId=" + testImageId + "&packageVersion=" + testImageVersion))
                .andReturn().getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, TrainingJob.class));

        final TrainingJob[] trainingJobs = trainingJobEntities.stream()
                .map(entity -> entityAndDtoMapper.map(entity, TrainingJob.class)).toArray(TrainingJob[]::new);

        assertThat(queriedTrainingJobs).containsExactlyInAnyOrder(trainingJobs);
    }

    @Test
    void shouldNotListAnythingWhenThereAreNoStoredEntities() throws Exception {
        entityManager.clear();
        List<TrainingJob> queriedTrainingJobs = objectMapper.readValue(
                mockMvc.perform(get("/v1/training-jobs")).andReturn().getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, TrainingJob.class));

        assertThat(queriedTrainingJobs).isEmpty();
    }
}
