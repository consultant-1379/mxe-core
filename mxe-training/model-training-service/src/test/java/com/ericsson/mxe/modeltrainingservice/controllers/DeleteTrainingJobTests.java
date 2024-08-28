package com.ericsson.mxe.modeltrainingservice.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.ericsson.mxe.modeltrainingservice.IntegrationTests;
import com.ericsson.mxe.modeltrainingservice.dto.TrainingJob;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingJobEntity;
import com.ericsson.mxe.modeltrainingservice.services.ModelTrainerService;

@IntegrationTests
public class DeleteTrainingJobTests extends TrainingJobTestBase {
    @MockBean
    ModelTrainerService modelTrainerService;

    @Test
    void shouldDeleteStoredTrainingJob() throws Exception {
        entityManager.persistAndFlush(trainingPackageEntity);
        for (TrainingJobEntity entity : trainingJobEntities) {
            entityManager.persistAndFlush(entity);
        }

        RequestBuilder deleteRequest = MockMvcRequestBuilders
                .delete("/v1/training-jobs?packageId=" + testImageId + "&packageVersion=" + testImageVersion);
        mockMvc.perform(deleteRequest).andExpect(status().isOk());

        final List<TrainingJob> queriedTrainingJobs = objectMapper.readValue(mockMvc
                .perform(get("/v1/training-jobs?packageId=" + testImageId + "&packageVersion=" + testImageVersion))
                .andReturn().getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, TrainingJob.class));

        assertThat(queriedTrainingJobs).isEmpty();
    }

    @Test
    void shouldNotDeleteNonExistingTrainingJob() throws Exception {
        entityManager.persistAndFlush(trainingPackageEntity);
        for (TrainingJobEntity entity : trainingJobEntities) {
            entityManager.persistAndFlush(entity);
        }

        RequestBuilder deleteRequest =
                MockMvcRequestBuilders.delete("/v1/training-jobs?packageId=faulty&packageVersion=" + testImageVersion);
        mockMvc.perform(deleteRequest).andExpect(status().isNotFound());

        final List<TrainingJob> queriedTrainingJobs = objectMapper.readValue(mockMvc
                .perform(get("/v1/training-jobs?packageId=" + testImageId + "&packageVersion=" + testImageVersion))
                .andReturn().getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, TrainingJob.class));

        assertThat(queriedTrainingJobs).hasSize(numOfTestJobEntities);
    }
}
