package com.ericsson.mxe.modeltrainingservice.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.ericsson.mxe.modeltrainingservice.IntegrationTests;
import com.ericsson.mxe.modeltrainingservice.dto.TrainingJob;
import com.ericsson.mxe.modeltrainingservice.dto.request.CreateTrainingJobRequest;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingJobEntity;

@IntegrationTests
public class CreateTrainingJobTests extends TrainingJobTestBase {
    @Test
    void shouldStorePostedTrainingJob() throws Exception {
        entityManager.persistAndFlush(trainingPackageEntity);

        CreateTrainingJobRequest request = new CreateTrainingJobRequest(testImageId, testImageVersion);

        mockMvc.perform(getRequest(request)).andExpect(status().isOk());

        final List<TrainingJob> queriedTrainingJobs = objectMapper.readValue(mockMvc
                .perform(get("/v1/training-jobs?packageId=" + testImageId + "&packageVersion=" + testImageVersion))
                .andReturn().getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, TrainingJob.class));

        assertThat(queriedTrainingJobs).isNotEmpty();
    }

    @Test
    void shouldNotStorePostedTrainingJobAlreadyRunningJob() throws Exception {
        entityManager.persistAndFlush(trainingPackageEntity);
        for (TrainingJobEntity entity : trainingJobEntities) {
            entityManager.persistAndFlush(entity);
        }

        CreateTrainingJobRequest request = new CreateTrainingJobRequest(testImageId, testImageVersion);

        mockMvc.perform(getRequest(request)).andExpect(status().isConflict());

        final List<TrainingJob> queriedTrainingJobs = objectMapper.readValue(mockMvc
                .perform(get("/v1/training-jobs?packageId=" + testImageId + "&packageVersion=" + testImageVersion))
                .andReturn().getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, TrainingJob.class));

        assertThat(queriedTrainingJobs).hasSize(numOfTestJobEntities);
    }

    private RequestBuilder getRequest(CreateTrainingJobRequest request) throws IOException {
        return MockMvcRequestBuilders.post("/v1/training-jobs").content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON);
    }
}
