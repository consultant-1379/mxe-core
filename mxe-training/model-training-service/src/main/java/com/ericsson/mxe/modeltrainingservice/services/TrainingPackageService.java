package com.ericsson.mxe.modeltrainingservice.services;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import com.ericsson.mxe.backendservicescommon.dto.status.PackageStatus;
import com.ericsson.mxe.backendservicescommon.exception.MxeBadRequestException;
import com.ericsson.mxe.backendservicescommon.exception.MxeConflictException;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.backendservicescommon.exception.MxeResourceNotFoundException;
import com.ericsson.mxe.modeltrainingservice.config.properties.DockerProperties;
import com.ericsson.mxe.modeltrainingservice.config.properties.DockerRegistryProperties;
import com.ericsson.mxe.modeltrainingservice.controllers.TrainingPackageServiceController;
import com.ericsson.mxe.modeltrainingservice.dto.TrainingJob;
import com.ericsson.mxe.modeltrainingservice.dto.TrainingPackage;
import com.ericsson.mxe.modeltrainingservice.dto.request.CreateTrainingPackageRequest;
import com.ericsson.mxe.modeltrainingservice.dto.request.UpdateTrainingPackageRequest;
import com.ericsson.mxe.modeltrainingservice.dto.response.ModelTrainingServiceResponse;
import com.ericsson.mxe.modeltrainingservice.dto.status.TrainingJobStatus;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingPackageEntity;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingPackageEntityId;
import com.ericsson.mxe.modeltrainingservice.persistence.repositories.TrainingPackagesRepository;
import com.google.common.collect.MoreCollectors;

@Service
public class TrainingPackageService extends CatalogueService<CreateTrainingPackageRequest, PackageStatus> {

    private static final Logger logger = LogManager.getLogger(TrainingPackageService.class);
    private final TrainingPackagesRepository trainingPackagesRepository;
    private final DockerRegistryProperties dockerRegistryProperties;
    private final TrainingJobService trainingJobService;

    public TrainingPackageService(final TrainingPackagesRepository trainingPackagesRepository,
            final DockerProperties dockerProperties, final TrainingJobService trainingJobService) {
        this.trainingPackagesRepository = trainingPackagesRepository;
        this.dockerRegistryProperties = dockerProperties.getRegistry();
        this.trainingJobService = trainingJobService;
    }

    public TrainingPackage getById(final String id, final String version) {
        final TrainingPackageEntityId trainingPackageEntityId = new TrainingPackageEntityId(id, version);
        return entityAndDtoMapper.map(
                trainingPackagesRepository.findById(trainingPackageEntityId)
                        .orElseThrow(() -> new MxeResourceNotFoundException(trainingPackageEntityId.toString())),
                TrainingPackage.class);
    }

    public List<TrainingPackage> list() {
        return StreamSupport.stream(trainingPackagesRepository.findAll().spliterator(), false)
                .map(entity -> entityAndDtoMapper.map(entity, TrainingPackage.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ModelTrainingServiceResponse create(String userId, String userName,
            final CreateTrainingPackageRequest request, final PackageStatus status, final boolean internal) {
        if (Objects.isNull(request)) {
            throw new MxeBadRequestException("Invalid request body");
        }

        final List<TrainingPackage> trainingPackages = list();
        final List<String> onboardedTrainingPackageImages =
                trainingPackages.stream().map(TrainingPackage::getImage).collect(Collectors.toList());
        final String requestImage = request.getImage();
        if (onboardedTrainingPackageImages.contains(requestImage)) {
            throw new MxeConflictException(
                    "Training Package with image \"" + requestImage + "\" has already been onboarded");
        }

        if (trainingPackages.stream()
                .anyMatch(trainingPackage -> StringUtils.equals(trainingPackage.getId(), request.getId())
                        && StringUtils.equals(trainingPackage.getVersion(), request.getVersion()))) {
            throw new MxeConflictException("Training Package with ID \"" + request.getId() + "\" and version \""
                    + request.getVersion() + "\" has already been onboarded");
        }

        final TrainingPackageEntity trainingPackageEntity = new TrainingPackageEntity();
        trainingPackageEntity.setId(new TrainingPackageEntityId(request.getId(), request.getVersion()));
        trainingPackageEntity.setTitle(request.getTitle());
        trainingPackageEntity.setAuthor(request.getAuthor());
        trainingPackageEntity.setDescription(request.getDescription());
        trainingPackageEntity.setImage(requestImage);
        trainingPackageEntity.setCreated(OffsetDateTime.now());
        trainingPackageEntity.setIcon(request.getIcon());
        trainingPackageEntity.setStatus(status);
        trainingPackageEntity.setMessage(null);
        trainingPackageEntity.setErrorLog(null);
        trainingPackageEntity.setInternal(internal);
        trainingPackageEntity.setCreatedByUserId(userId);
        trainingPackageEntity.setCreatedByUserName(userName);

        return trainingPackagesRepository.saveIfNotExists(trainingPackageEntity);
    }

    @Transactional
    public ModelTrainingServiceResponse update(final String id, final String version,
            final UpdateTrainingPackageRequest request) {
        if (Objects.isNull(request)) {
            throw new MxeBadRequestException("Invalid request body");
        }

        final TrainingPackageEntity entity = trainingPackagesRepository
                .findById(new TrainingPackageEntityId(id, version)).orElseThrow(() -> new MxeResourceNotFoundException(
                        "Training Package \"" + id + "\"" + version + "\" not found!"));

        if (Objects.nonNull(request.getStatus())) {
            // Once status is "available", then it can not be changed anymore
            if (!PackageStatus.Available.equals(entity.getStatus())
                    || PackageStatus.Available.equals(request.getStatus())) {
                entity.setStatus(request.getStatus());
            } else {
                throw new UnsupportedOperationException();
            }
        }

        entity.setMessage(org.modelmapper.internal.util.Objects.firstNonNull(request.getMessage()));
        entity.setErrorLog(org.modelmapper.internal.util.Objects.firstNonNull(request.getErrorLog()));

        trainingPackagesRepository.save(entity);

        return new ModelTrainingServiceResponse(
                "Training Package \"" + id + "\" version \"" + version + "\" has been updated on cluster.");
    }

    @Transactional
    public ModelTrainingServiceResponse updateUnknown(final String id, final CreateTrainingPackageRequest request) {
        if (Objects.isNull(request)) {
            throw new MxeBadRequestException("Invalid request body");
        }


        final Optional<TrainingPackage> modelToDelete = list().stream().filter(
                trainingPackage -> trainingPackage.getId().equals(id) && trainingPackage.getVersion().equals(UNKNOWN))
                .findFirst();

        modelToDelete.ifPresentOrElse(trainingPackage -> {
            create(modelToDelete.get().getCreatedByUserId(), modelToDelete.get().getCreatedByUserName(), request,
                    PackageStatus.Packaging, true);
            try {
                final TrainingPackageEntityId entityId = new TrainingPackageEntityId(id, UNKNOWN);
                trainingPackagesRepository.deleteById(entityId);
            } catch (EmptyResultDataAccessException e) {
                throw new MxeResourceNotFoundException(
                        "Training Package \"" + id + "\" version \"" + UNKNOWN + "\" not found!");
            }
        }, () -> {
            throw new MxeResourceNotFoundException(
                    "Training Package \"" + id + "\" version \"" + UNKNOWN + "\" not found!");
        });

        return new ModelTrainingServiceResponse("Temporary Training Package \"" + id + "\" has been updated to \""
                + request.getId() + "\" with version \"" + request.getVersion() + "\" on cluster.");
    }

    @Transactional
    public ModelTrainingServiceResponse delete(final String id, final String version) {
        final ModelTrainingServiceResponse modelCatalogueServiceResponse = new ModelTrainingServiceResponse();
        final Optional<TrainingPackage> trainingPackageToDelete = list().stream().filter(
                trainingPackage -> trainingPackage.getId().equals(id) && trainingPackage.getVersion().equals(version))
                .findFirst();

        trainingPackageToDelete.ifPresentOrElse(trainingPackage -> {
            if (PackageStatus.Packaging == trainingPackage.getStatus()) {
                throw new MxeConflictException("Training package \"" + id + "\" version \"" + version
                        + "\" is being onboarded, it can not be deleted at the moment. Please try it later!");
            }

            final List<TrainingJob> trainingJobsOfTrainingPackage = trainingJobService.getByPackage(id, version);
            final boolean trainingJobOfTrainingPackageIsRunning = trainingJobsOfTrainingPackage.stream()
                    .anyMatch(trainingJob -> TrainingJobStatus.Running == trainingJob.getStatus());
            if (trainingJobOfTrainingPackageIsRunning) {
                final String runningTrainingJobId = trainingJobsOfTrainingPackage.stream()
                        .filter(trainingJob -> TrainingJobStatus.Running == trainingJob.getStatus())
                        .map(TrainingJob::getId).collect(MoreCollectors.onlyElement());

                throw new MxeConflictException("Training package \"" + id + "\" version \"" + version
                        + "\" can not be deleted, training job is still running. Please try it later or delete running job directly!\nrunning job id: "
                        + runningTrainingJobId);
            } else if (!CollectionUtils.isEmpty(trainingJobsOfTrainingPackage)) {
                modelCatalogueServiceResponse.additionalInfo = trainingJobService.delete(id, version).additionalInfo;
            }

            if (Boolean.TRUE.equals(trainingPackage.isInternal())) {
                final String tag =
                        trainingPackage.getImage().substring(trainingPackage.getImage().lastIndexOf(":") + 1);
                final String image =
                        trainingPackage.getImage().substring(0, trainingPackage.getImage().lastIndexOf(":"));
                final RestTemplate restTemplate = new RestTemplate();
                final String manifestUrl =
                        "http://" + dockerRegistryProperties.getHostname() + "/v2/" + image + "/manifests/" + tag;
                final HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Collections.singletonList(
                        MediaType.parseMediaType("application/vnd.docker.distribution.manifest.v2+json")));
                headers.setBasicAuth(dockerRegistryProperties.getUsername(), dockerRegistryProperties.getPassword());
                final HttpEntity<String> entity = new HttpEntity<>(headers);

                try {
                    final HttpHeaders manifestHeaders =
                            restTemplate.exchange(manifestUrl, HttpMethod.GET, entity, Void.class).getHeaders();
                    final List<String> digests = manifestHeaders.get("Docker-Content-Digest");

                    if (digests != null && !digests.isEmpty()) {
                        final String digest = digests.get(0);

                        String deleteUrl = "http://" + dockerRegistryProperties.getHostname() + "/v2/" + image
                                + "/manifests/" + digest;

                        ResponseEntity<Void> response =
                                restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, Void.class);

                        if (response.getStatusCode() != HttpStatus.ACCEPTED) {
                            throw new MxeInternalException("Training package \"" + id + "\" version \"" + version
                                    + "\" cannot be deleted from the internal Docker registry!");
                        }
                    } else {
                        logger.error("Training package Docker-Content-Digest for \"" + id + "\" version \"" + version
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
                final TrainingPackageEntityId trainingPackageEntityId = new TrainingPackageEntityId(id, version);
                trainingPackagesRepository.deleteById(trainingPackageEntityId);
            } catch (EmptyResultDataAccessException e) {
                throw new MxeResourceNotFoundException(
                        "Training package \"" + id + "\" version \"" + version + "\" not found!");
            }
        }, () -> {
            throw new MxeResourceNotFoundException(
                    "Training package \"" + id + "\" version \"" + version + "\" not found!");
        });

        modelCatalogueServiceResponse.message =
                "Training package \"" + id + "\" version \"" + version + "\" has been deleted from cluster.";
        return modelCatalogueServiceResponse;
    }

    @Override
    @Transactional
    public String createDummyEntity(String userId, String userName, String name) {
        final List<String> existingNames = list().stream().map(TrainingPackage::getId).collect(Collectors.toList());
        final String dummyName = getUniqueName(name, existingNames);

        final TrainingPackageEntity trainingPackageEntity = new TrainingPackageEntity();
        trainingPackageEntity.setId(new TrainingPackageEntityId(dummyName, UNKNOWN));
        trainingPackageEntity.setTitle(StringUtils.EMPTY);
        trainingPackageEntity.setAuthor(StringUtils.EMPTY);
        trainingPackageEntity.setDescription(StringUtils.EMPTY);
        trainingPackageEntity.setImage(StringUtils.EMPTY);
        trainingPackageEntity.setCreated(OffsetDateTime.now());
        trainingPackageEntity.setIcon(StringUtils.EMPTY);
        trainingPackageEntity.setStatus(PackageStatus.Packaging);
        trainingPackageEntity.setMessage("Preparing for packaging");
        trainingPackageEntity.setErrorLog(null);
        trainingPackageEntity.setInternal(false);
        trainingPackageEntity.setCreatedByUserId(userId);
        trainingPackageEntity.setCreatedByUserName(userName);

        trainingPackagesRepository.saveIfNotExists(trainingPackageEntity);

        return dummyName;
    }

    @Override
    public List<String> getValidFormFields() {
        return Collections.singletonList(TrainingPackageServiceController.TRAINING_PACKAGE);
    }
}
