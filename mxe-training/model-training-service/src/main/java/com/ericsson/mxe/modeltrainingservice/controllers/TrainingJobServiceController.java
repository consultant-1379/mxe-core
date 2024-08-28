package com.ericsson.mxe.modeltrainingservice.controllers;

import java.io.InputStream;
import java.util.List;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.modeltrainingservice.dto.TrainingJob;
import com.ericsson.mxe.modeltrainingservice.dto.request.CreateTrainingJobRequest;
import com.ericsson.mxe.modeltrainingservice.dto.request.UpdateTrainingJobRequest;
import com.ericsson.mxe.modeltrainingservice.dto.response.CreateTrainingJobResponse;
import com.ericsson.mxe.modeltrainingservice.dto.response.ModelTrainingServiceResponse;
import com.ericsson.mxe.modeltrainingservice.services.ModelTrainerService;
import com.ericsson.mxe.modeltrainingservice.services.TrainingJobService;
import com.google.common.base.Throwables;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/v1/training-jobs")
public class TrainingJobServiceController {
    private static final String TRAINING_JOB_RESULT_FILE_PREFIX = "training-job-result-";
    private static final String TRAINING_JOB_RESULT_FILE_POSTFIX = ".zip";

    private final TrainingJobService trainingJobService;

    private final ModelTrainerService modelTrainerService;


    public TrainingJobServiceController(final TrainingJobService trainingJobService,
            final ModelTrainerService modelTrainerService) {
        this.trainingJobService = trainingJobService;
        this.modelTrainerService = modelTrainerService;
    }

    @GetMapping()
    public List<TrainingJob> getTrainingJobList() {
        return trainingJobService.list();
    }

    @GetMapping("/{id}")
    public TrainingJob getTrainingJob(@PathVariable @NotNull final String id) {
        return trainingJobService.get(id);
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<?> getTrainingJobResult(@PathVariable @NotNull final String id) {
        final InputStream resultStream = trainingJobService.getResult(id);
        final InputStreamResource resultStreamResult = new InputStreamResource(resultStream);

        final String filename = TRAINING_JOB_RESULT_FILE_PREFIX + id + TRAINING_JOB_RESULT_FILE_POSTFIX;

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(filename).build());
        headers.setContentType(MediaType.parseMediaType("application/zip"));

        return ResponseEntity.ok().headers(headers).body(resultStreamResult);
    }

    @GetMapping(params = {"packageId"})
    public List<TrainingJob> getTrainingJobByPackageId(@RequestParam String packageId) {
        return trainingJobService.getByPackageId(packageId);
    }

    @GetMapping(params = {"packageId", "packageVersion"})
    public List<TrainingJob> getTrainingJobByPackage(@RequestParam String packageId,
            @RequestParam String packageVersion) {
        return trainingJobService.getByPackage(packageId, packageVersion);
    }

    @PostMapping()
    public CreateTrainingJobResponse postTrainingJob(@RequestBody @Validated CreateTrainingJobRequest request) {
        CreateTrainingJobResponse createResponse = trainingJobService.create(request);
        try {
            modelTrainerService.startJob(request.getPackageId() + ":" + request.getPackageVersion(), createResponse.id);
            return createResponse;
        } catch (Exception e) {
            trainingJobService.updateFailed(createResponse.id, e.getMessage(), Throwables.getStackTraceAsString(e));
            throw new MxeInternalException(e.getMessage(), e);
        }
    }

    @PatchMapping(path = "/{id}", consumes = "application/json")
    public ModelTrainingServiceResponse patchDeployment(@PathVariable @NotNull String id,
            @RequestBody @Validated UpdateTrainingJobRequest request) {
        return trainingJobService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ModelTrainingServiceResponse deleteTrainingJob(@PathVariable @NotNull String id) {
        return trainingJobService.delete(id);
    }

    @DeleteMapping(params = {"packageId", "packageVersion"})
    public ModelTrainingServiceResponse deleteTrainingJob(@RequestParam String packageId,
            @RequestParam String packageVersion) {
        return trainingJobService.delete(packageId, packageVersion);
    }
}
