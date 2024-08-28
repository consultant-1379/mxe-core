package com.ericsson.mxe.modelcatalogueservice.persistence.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.ericsson.mxe.backendservicescommon.exception.MxeConflictException;
import com.ericsson.mxe.modelcatalogueservice.dto.response.ModelCatalogueServiceResponse;
import com.ericsson.mxe.modelcatalogueservice.persistence.domain.ModelEntity;
import com.ericsson.mxe.modelcatalogueservice.persistence.domain.ModelEntityId;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface ModelsRepository extends CrudRepository<ModelEntity, ModelEntityId> {
    default ModelCatalogueServiceResponse saveIfNotExists(final ModelEntity entity) {
        if (findById(entity.getId()).isPresent()) {
            throw new MxeConflictException("Model already exists: " + entity.getId());
        } else {
            save(entity);
            return new ModelCatalogueServiceResponse(
                    "Model \"" + entity.getImage() + "\" has been onboarded to cluster with ID \""
                            + entity.getId().getId() + "\" and version \"" + entity.getId().getVersion() + "\".");
        }
    }
}
