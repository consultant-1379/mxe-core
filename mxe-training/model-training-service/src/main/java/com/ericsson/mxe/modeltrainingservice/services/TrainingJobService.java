package com.ericsson.mxe.modeltrainingservice.services;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.ericsson.mxe.backendservicescommon.exception.MxeBadRequestException;
import com.ericsson.mxe.backendservicescommon.exception.MxeConflictException;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.backendservicescommon.exception.MxeResourceNotFoundException;
import com.ericsson.mxe.modeltrainingservice.dto.TrainingJob;
import com.ericsson.mxe.modeltrainingservice.dto.request.CreateTrainingJobRequest;
import com.ericsson.mxe.modeltrainingservice.dto.request.UpdateTrainingJobRequest;
import com.ericsson.mxe.modeltrainingservice.dto.response.CreateTrainingJobResponse;
import com.ericsson.mxe.modeltrainingservice.dto.response.ModelTrainingServiceResponse;
import com.ericsson.mxe.modeltrainingservice.dto.status.TrainingJobStatus;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingJobEntity;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingPackageEntity;
import com.ericsson.mxe.modeltrainingservice.persistence.repositories.TrainingJobsRepository;
import com.ericsson.mxe.modeltrainingservice.persistence.repositories.TrainingPackagesRepository;
import com.ericsson.mxe.modeltrainingservice.services.minio.TrainingJobResultRepositoryService;
import jakarta.transaction.Transactional;

@Service
public class TrainingJobService {
    private static final Logger logger = LogManager.getLogger(TrainingJobService.class);

    private final ModelMapper entityAndDtoMapper;

    private final TrainingJobsRepository trainingJobsRepository;
    private final TrainingPackagesRepository trainingPackagesRepository;
    private final ModelTrainerService modelTrainerService;
    private final TrainingJobResultRepositoryService trainingJobResultRepositoryService;

    public TrainingJobService(@Qualifier("entityAndDtoMapper") final ModelMapper entityAndDtoMapper,
            final TrainingJobsRepository trainingJobsRepository,
            final TrainingPackagesRepository trainingPackagesRepository, final ModelTrainerService modelTrainerService,
            final TrainingJobResultRepositoryService trainingJobResultRepositoryService) {
        this.entityAndDtoMapper = entityAndDtoMapper;
        this.trainingJobsRepository = trainingJobsRepository;
        this.trainingPackagesRepository = trainingPackagesRepository;
        this.modelTrainerService = modelTrainerService;
        this.trainingJobResultRepositoryService = trainingJobResultRepositoryService;
    }

    public TrainingJob get(final String id) {
        return entityAndDtoMapper.map(
                trainingJobsRepository.findById(id).orElseThrow(
                        () -> new MxeResourceNotFoundException("Training job with id \"" + id + "\" not found")),
                TrainingJob.class);

    }

    public InputStream getResult(final String id) {
        final TrainingJob trainingJob = get(id);
        if (trainingJob.getStatus() != TrainingJobStatus.Running) {
            return trainingJobResultRepositoryService.getObject(id);
        } else {
            throw new MxeResourceNotFoundException("Training job is still running");
        }
    }

    public List<TrainingJob> getByPackageId(final String packageId) {
        return list().stream().filter(job -> job.getPackageId().contentEquals(packageId)).collect(Collectors.toList());

    }

    public List<TrainingJob> getByPackage(final String packageId, final String packageVersion) {
        return list().stream().filter(job -> job.getPackageId().contentEquals(packageId)
                && job.getPackageVersion().contentEquals(packageVersion)).collect(Collectors.toList());
    }

    public List<TrainingJob> list() {
        return StreamSupport.stream(trainingJobsRepository.findAll().spliterator(), false)
                .map(entity -> entityAndDtoMapper.map(entity, TrainingJob.class)).collect(Collectors.toList());
    }

    @Transactional
    public CreateTrainingJobResponse create(final CreateTrainingJobRequest request) {
        if (request == null) {
            throw new MxeBadRequestException("Invalid request body");
        }
        if (list().stream()
                .anyMatch(job -> job.getPackageId().equals(request.getPackageId())
                        && job.getPackageVersion().equals(request.getPackageVersion())
                        && job.getStatus().equals(TrainingJobStatus.Running))) {
            throw new MxeConflictException("Training is already running with packageId \"" + request.getPackageId()
                    + "\" packageVersion \"" + request.getPackageVersion() + "\"");
        }

        TrainingPackageEntity trainingPackageEntity = StreamSupport
                .stream(trainingPackagesRepository.findAll().spliterator(), false)
                .filter(entity -> entity.getId().getId().equals(request.getPackageId())
                        && entity.getId().getVersion().equals(request.getPackageVersion()))
                .findFirst().orElseThrow(() -> new MxeResourceNotFoundException("Training package with id \""
                        + request.getPackageId() + "\" version \"" + request.getPackageVersion() + "\" not found!"));


        final TrainingJobEntity entity = new TrainingJobEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTrainingPackageEntity(trainingPackageEntity);
        entity.setStatus(TrainingJobStatus.Running);
        entity.setCreated(OffsetDateTime.now());

        trainingJobsRepository.saveIfNotExists(entity);

        return new CreateTrainingJobResponse(entity.getId());
    }

    @Transactional
    public ModelTrainingServiceResponse update(final String id, final UpdateTrainingJobRequest request) {
        if (request == null) {
            throw new MxeBadRequestException("Invalid request body");
        }

        TrainingJobEntity entity = trainingJobsRepository.findById(id)
                .orElseThrow(() -> new MxeResourceNotFoundException("Training job with id \"" + id + "\" not found!"));

        if (request.getStatus() != null) {
            // Once status is "completed", then it can not be changed anymore
            if (!TrainingJobStatus.Completed.equals(entity.getStatus())
                    || TrainingJobStatus.Completed.equals(request.getStatus())) {
                entity.setStatus(request.getStatus());
                entity.setCompleted(OffsetDateTime.now());
            } else {
                throw new UnsupportedOperationException();
            }
        }

        entity.setMessage(org.modelmapper.internal.util.Objects.firstNonNull(request.getMessage()));
        entity.setErrorLog(org.modelmapper.internal.util.Objects.firstNonNull(request.getErrorLog()));

        trainingJobsRepository.save(entity);

        return new ModelTrainingServiceResponse("Training job with \"" + id + "\" has been updated on cluster.");
    }

    public ModelTrainingServiceResponse updateFailed(final String id, String message, String errorLog) {
        UpdateTrainingJobRequest updateTrainingJobRequest =
                new UpdateTrainingJobRequest(TrainingJobStatus.Failed, message, errorLog);
        return update(id, updateTrainingJobRequest);
    }

    @Transactional
    public ModelTrainingServiceResponse delete(final String id) {
        trainingJobsRepository.findById(id)
                .orElseThrow(() -> new MxeResourceNotFoundException("Training job with id \"" + id + "\" not found!"));

        try {
            modelTrainerService.deleteJob(id);
            trainingJobsRepository.deleteById(id);
        } catch (Exception e) {
            logger.error("Exception in TrainingJobService during single training job deletion", e);
            throw new MxeInternalException("Failed to delete training job with id \"" + id + "\"");
        }

        return new ModelTrainingServiceResponse("Training job with id \"" + id + "\" has been removed from cluster.");
    }

    @Transactional
    public ModelTrainingServiceResponse delete(final String packageId, final String packageVersion) {
        final List<String> errorMessages = new ArrayList<>();

        final List<String> trainingJobsIds =
                getByPackage(packageId, packageVersion).stream().map(TrainingJob::getId).collect(Collectors.toList());
        if (trainingJobsIds.isEmpty()) {
            throw new MxeResourceNotFoundException("No training job found with packageId \"" + packageId
                    + "\" and packageVersion \"" + packageVersion + "\"");
        }
        for (final String trainingJobId : trainingJobsIds) {
            try {
                modelTrainerService.deleteJob(trainingJobId);
                trainingJobsRepository.deleteById(trainingJobId);
            } catch (Exception e) {
                logger.error("Exception in TrainingJobService during batch training job deletion", e);
                errorMessages.add("Failed to delete training job with id \"" + trainingJobId + "\"");
            }
        }

        if (errorMessages.size() > 0) {
            throw new MxeInternalException(errorMessages.stream().collect(Collectors.joining(System.lineSeparator())));
        }

        return new ModelTrainingServiceResponse(
                "Training jobs with packageId \"" + packageId + "\" and packageVersion \"" + packageVersion
                        + "\" have been removed from cluster.",
                "Removed training jobs: " + StringUtils.join(trainingJobsIds, ", "));
    }
}
