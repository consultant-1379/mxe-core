package com.ericsson.mxe.modeltrainingservice.persistence.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.ericsson.mxe.backendservicescommon.exception.MxeConflictException;
import com.ericsson.mxe.modeltrainingservice.dto.response.ModelTrainingServiceResponse;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingPackageEntity;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingPackageEntityId;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface TrainingPackagesRepository extends CrudRepository<TrainingPackageEntity, TrainingPackageEntityId> {
    default ModelTrainingServiceResponse saveIfNotExists(final TrainingPackageEntity entity) {
        if (findById(entity.getId()).isPresent()) {
            throw new MxeConflictException("Training package already exists: " + entity.getId());
        } else {
            save(entity);
            return new ModelTrainingServiceResponse(
                    "Training package \"" + entity.getImage() + "\" has been onboarded to cluster with ID \""
                            + entity.getId().getId() + "\" and version \"" + entity.getId().getVersion() + "\".");
        }
    }
}
