package com.ericsson.mxe.modelcatalogueservice.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.time.OffsetDateTime;
import org.modelmapper.internal.util.Objects;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import com.ericsson.mxe.backendservicescommon.dto.status.PackageStatus;
import com.ericsson.mxe.backendservicescommon.exception.MxeBadRequestException;
import com.ericsson.mxe.backendservicescommon.exception.MxeConflictException;
import com.ericsson.mxe.backendservicescommon.exception.MxeForbiddenException;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.backendservicescommon.exception.MxeResourceNotFoundException;
import com.ericsson.mxe.modelcatalogueservice.config.properties.DockerProperties;
import com.ericsson.mxe.modelcatalogueservice.config.properties.DockerRegistryProperties;
import com.ericsson.mxe.modelcatalogueservice.controllers.ModelCatalogueServiceController;
import com.ericsson.mxe.modelcatalogueservice.dto.Model;
import com.ericsson.mxe.modelcatalogueservice.dto.request.CreateModelRequest;
import com.ericsson.mxe.modelcatalogueservice.dto.request.UpdateModelRequest;
import com.ericsson.mxe.modelcatalogueservice.dto.response.ModelCatalogueServiceResponse;
import com.ericsson.mxe.modelcatalogueservice.persistence.domain.ModelEntity;
import com.ericsson.mxe.modelcatalogueservice.persistence.domain.ModelEntityId;
import com.ericsson.mxe.modelcatalogueservice.persistence.repositories.ModelsRepository;
import com.ericsson.mxe.modelcatalogueservice.services.minio.ModelSourceRepositoryService;
import jakarta.validation.constraints.NotNull;

@Service
public class ModelService extends CatalogueService<CreateModelRequest, PackageStatus> {

    private static final Logger logger = LogManager.getLogger(ModelService.class);

    private final ModelsRepository modelsRepository;
    private final ModelDeploymentService modelDeploymentService;
    private final DockerRegistryProperties dockerRegistryProperties;
    private final ModelSourceRepositoryService modelSourceRepositoryService;

    public ModelService(final ModelsRepository modelsRepository, final ModelDeploymentService modelDeploymentService,
            final DockerProperties dockerProperties, final ModelSourceRepositoryService modelSourceRepositoryService) {
        this.modelsRepository = modelsRepository;
        this.modelDeploymentService = modelDeploymentService;
        this.dockerRegistryProperties = dockerProperties.getRegistry();
        this.modelSourceRepositoryService = modelSourceRepositoryService;
    }

    public List<Model> list() {
        return listAsStream().collect(Collectors.toList());
    }

    public List<Model> listById(final String id) {
        return listAsStream().filter(model -> model.getId().equals(id)).collect(Collectors.toList());
    }

    public Stream<Model> listAsStream() {
        return StreamSupport.stream(modelsRepository.findAll().spliterator(), false)
                .map(entity -> entityAndDtoMapper.map(entity, Model.class));
    }

    @Override
    @Transactional
    public ModelCatalogueServiceResponse create(String userId, String userName, final CreateModelRequest request,
            final PackageStatus status, final boolean internal) {
        if (request == null) {
            throw new MxeBadRequestException("Invalid request body");
        }

        // TODO: push docker registry credentials to the job in the future -
        // dockerRegistry

        final List<Model> models = list();
        final List<String> onboardedModelImages = models.stream().map(Model::getImage).collect(Collectors.toList());
        final String requestImage = request.getImage();
        if (onboardedModelImages.contains(requestImage)) {
            throw new MxeConflictException("Model with image \"" + requestImage + "\" has already been onboarded");
        }

        if (models.stream().anyMatch(m -> StringUtils.equals(m.getId(), request.getId())
                && StringUtils.equals(m.getVersion(), request.getVersion()))) {
            throw new MxeConflictException("Model with ID \"" + request.getId() + "\" and version \""
                    + request.getVersion() + "\" has already been onboarded");
        }

        final String dockerRegistrySecretName = StringUtils.firstNonEmpty(request.getDockerRegistrySecretName());

        final ModelEntity modelEntity = new ModelEntity(request.getTitle(), request.getAuthor(), requestImage,
                OffsetDateTime.now(), request.getIcon(), status, null, null, internal, userId, userName,
                new ModelEntityId(request.getId(), request.getVersion()), request.getDescription(),
                dockerRegistrySecretName, request.getSignedByPublicKey(), request.getSignedByName(),
                request.isStateful());

        return modelsRepository.saveIfNotExists(modelEntity);
    }

    @Transactional
    public ModelCatalogueServiceResponse update(final String id, final String version,
            final UpdateModelRequest request) {
        if (request == null) {
            throw new MxeBadRequestException("Invalid request body");
        }

        final ModelEntity entity = modelsRepository.findById(new ModelEntityId(id, version)).get();

        if (request.getStatus() != null) {
            // Once status is "available", then it can not be changed anymore
            if (!PackageStatus.Available.equals(entity.getStatus())
                    || PackageStatus.Available.equals(request.getStatus())) {
                entity.setStatus(request.getStatus());
            } else {
                throw new UnsupportedOperationException();
            }
        }

        entity.setMessage(Objects.firstNonNull(request.getMessage()));
        entity.setErrorLog(Objects.firstNonNull(request.getErrorLog()));

        modelsRepository.save(entity);

        return new ModelCatalogueServiceResponse(
                "Model \"" + id + "\" version \"" + version + "\" has been updated on cluster.");
    }

    @Transactional(noRollbackFor = MxeForbiddenException.class)
    public ModelCatalogueServiceResponse updateUnknown(final String id, final CreateModelRequest request) {
        if (request == null) {
            throw new MxeBadRequestException("Invalid request body");
        }

        Optional<Model> modelToDelete = list().stream()
                .filter(model -> model.getId().equals(id) && model.getVersion().equals("unknown")).findFirst();

        modelToDelete.ifPresentOrElse(model -> {
            this.create(modelToDelete.get().getCreatedByUserId(), modelToDelete.get().getCreatedByUserName(), request,
                    PackageStatus.Packaging, true);
            try {
                final ModelEntityId entityId = new ModelEntityId(id, UNKNOWN);
                modelsRepository.deleteById(entityId);
            } catch (EmptyResultDataAccessException e) {
                throw new MxeResourceNotFoundException("Model \"" + id + "\" version \"" + UNKNOWN + "\" not found!");
            }
        }, () -> {
            throw new MxeResourceNotFoundException("Model \"" + id + "\" version \"" + UNKNOWN + "\" not found!");
        });

        return new ModelCatalogueServiceResponse("Temporary model \"" + id + "\" has been updated to \""
                + request.getId() + "\" with version \"" + request.getVersion() + "\" on cluster.");
    }

    @Transactional
    public ModelCatalogueServiceResponse delete(final String id, final String version) {
        Optional<Model> modelToDelete = list().stream()
                .filter(model -> model.getId().equals(id) && model.getVersion().equals(version)).findFirst();

        final ModelCatalogueServiceResponse modelCatalogueServiceResponse = new ModelCatalogueServiceResponse();

        modelToDelete.ifPresentOrElse(model -> {
            if (PackageStatus.Packaging == model.getStatus()) {
                throw new MxeConflictException("Model \"" + id + "\" version \"" + version
                        + "\" is being onboarded, it can not be deleted at the moment. Please try it later!");
            }
            if (modelDeploymentService.modelIsDeployed(id, version)) {
                throw new MxeConflictException("Model \"" + id + "\" version \"" + version
                        + "\" has an associated running model service. Please delete all the associated model services with the command mxe-service delete, and delete the model afterwards!");
            }

            if (model.isInternal() != null && model.isInternal()) {
                final String tag = model.getImage().substring(model.getImage().lastIndexOf(":") + 1);
                final String image = model.getImage().substring(0, model.getImage().lastIndexOf(":"));

                RestTemplate restTemplate = new RestTemplate();

                String manifestUrl =
                        "http://" + this.dockerRegistryProperties.getHostname() + "/v2/" + image + "/manifests/" + tag;

                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Collections.singletonList(
                        MediaType.parseMediaType("application/vnd.docker.distribution.manifest.v2+json")));
                headers.setBasicAuth(this.dockerRegistryProperties.getUsername(),
                        this.dockerRegistryProperties.getPassword());
                HttpEntity<String> entity = new HttpEntity<>(headers);

                try {
                    HttpHeaders manifestHeaders =
                            restTemplate.exchange(manifestUrl, HttpMethod.GET, entity, Void.class).getHeaders();

                    final List<String> digests = manifestHeaders.get("Docker-Content-Digest");

                    if (digests != null && !digests.isEmpty()) {
                        final String digest = digests.get(0);

                        String deleteUrl = "http://" + this.dockerRegistryProperties.getHostname() + "/v2/" + image
                                + "/manifests/" + digest;

                        ResponseEntity<Void> response =
                                restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);

                        if (response.getStatusCode() != HttpStatus.ACCEPTED) {
                            throw new MxeInternalException("Model \"" + id + "\" version \"" + version
                                    + "\" cannot be deleted from the internal Docker registry!");
                        }
                    } else {
                        logger.error("Model Docker-Content-Digest for \"" + id + "\" version \"" + version
                                + "\" not found in the internal Docker registry!");
                    }
                } catch (RestClientResponseException e) {
                    if (e.getRawStatusCode() != HttpStatus.NOT_FOUND.value()) {
                        throw new MxeInternalException("Model \"" + id + "\" version \"" + version
                                + "\" could not be get or deleted from Docker registry!");
                    }
                    logger.error("Model \"" + id + "\" version \"" + version
                            + "\" not found in the internal Docker registry!");
                }
            }

            try {
                final ModelEntityId entityId = new ModelEntityId(id, version);
                modelsRepository.deleteById(entityId);
            } catch (EmptyResultDataAccessException e) {
                throw new MxeResourceNotFoundException("Model \"" + id + "\" version \"" + version + "\" not found!");
            }
        }, () -> {
            throw new MxeResourceNotFoundException("Model \"" + id + "\" version \"" + version + "\" not found!");
        });

        modelCatalogueServiceResponse.message =
                "Model \"" + id + "\" version \"" + version + "\" has been deleted from cluster.";
        return modelCatalogueServiceResponse;
    }

    @Override
    @Transactional
    public String createDummyEntity(String userId, String userName, String name) {
        final List<String> existingNames = list().stream().map(Model::getId).collect(Collectors.toList());
        final String dummyEntityName = getUniqueName(name, existingNames);

        final ModelEntity modelEntity = new ModelEntity(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
                OffsetDateTime.now(), null, PackageStatus.Packaging, null, null, false, userId, userName,
                new ModelEntityId(dummyEntityName, UNKNOWN), StringUtils.EMPTY, null, null, null, false);

        modelsRepository.saveIfNotExists(modelEntity);

        return dummyEntityName;
    }

    @Override
    public List<String> getValidFormFields() {
        return Arrays.asList(ModelCatalogueServiceController.ARCHIVE_FILE, ModelCatalogueServiceController.MODEL_DATA,
                ModelCatalogueServiceController.SOURCE_FILE);
    }

    public ModelCatalogueServiceResponse deletePackage(@NotNull String packageId) {
        modelSourceRepositoryService.removeObject(packageId);
        logger.info("Package {} has been deleted from the temporary repository.", packageId);
        final ModelCatalogueServiceResponse modelCatalogueServiceResponse = new ModelCatalogueServiceResponse();
        modelCatalogueServiceResponse.message = "Package " + packageId + " has been deleted from cluster.";
        return modelCatalogueServiceResponse;
    }

}
