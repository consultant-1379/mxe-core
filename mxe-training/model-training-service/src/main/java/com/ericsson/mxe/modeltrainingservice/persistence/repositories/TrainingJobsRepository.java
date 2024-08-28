package com.ericsson.mxe.modeltrainingservice.persistence.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.ericsson.mxe.backendservicescommon.exception.MxeConflictException;
import com.ericsson.mxe.modeltrainingservice.dto.response.ModelTrainingServiceResponse;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingJobEntity;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface TrainingJobsRepository extends CrudRepository<TrainingJobEntity, String> {
    default ModelTrainingServiceResponse saveIfNotExists(final TrainingJobEntity entity) {
        if (findById(entity.getId()).isPresent()) {
            throw new MxeConflictException("Training job already exists: " + entity.getId());
        } else {
            save(entity);
            ModelTrainingServiceResponse modelCatalogueServiceResponse = new ModelTrainingServiceResponse();
            modelCatalogueServiceResponse.message = "training job";
            return modelCatalogueServiceResponse;
        }
    }
}
