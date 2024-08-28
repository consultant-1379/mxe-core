package com.ericsson.mxe.modeltrainingservice.controllers;

import static com.ericsson.mxe.modeltrainingservice.ModelTrainingServiceApplication.USERID_KEY;
import static com.ericsson.mxe.modeltrainingservice.ModelTrainingServiceApplication.USERNAME_KEY;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ericsson.mxe.backendservicescommon.exception.MxeBadRequestException;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.modeltrainingservice.dto.JobType;
import com.ericsson.mxe.modeltrainingservice.dto.TrainingPackage;
import com.ericsson.mxe.modeltrainingservice.dto.request.CreateTrainingPackageRequest;
import com.ericsson.mxe.modeltrainingservice.dto.request.UpdateTrainingPackageRequest;
import com.ericsson.mxe.modeltrainingservice.dto.response.ModelTrainingServiceResponse;
import com.ericsson.mxe.modeltrainingservice.services.PackagerService;
import com.ericsson.mxe.modeltrainingservice.services.TrainingPackageService;
import com.ericsson.mxe.modeltrainingservice.services.minio.TrainingPackageRepositoryService;
import io.kubernetes.client.openapi.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/v1/training-packages")
public class TrainingPackageServiceController {

    public static final String TRAINING_PACKAGE = "trainingpackage";

    private final PackagerService packagerService;
    private final TrainingPackageService trainingPackageService;
    private final TrainingPackageRepositoryService trainingPackageRepositoryService;

    public TrainingPackageServiceController(final PackagerService packagerService,
            final TrainingPackageService trainingPackageService,
            final TrainingPackageRepositoryService trainingPackageRepositoryService) {
        this.packagerService = packagerService;
        this.trainingPackageService = trainingPackageService;
        this.trainingPackageRepositoryService = trainingPackageRepositoryService;
    }

    @GetMapping()
    public List<TrainingPackage> getTrainingPackageList() {
        return trainingPackageService.list();
    }

    @GetMapping("/{id}/{version}")
    public TrainingPackage getTrainingPackageById(@PathVariable @NotNull final String id,
            @PathVariable @NotNull final String version) {
        return trainingPackageService.getById(id, version);
    }

    @PostMapping()
    public ModelTrainingServiceResponse onboardTrainingPackage(final HttpServletRequest request) {
        // TODO: extract the model metadata file from the zip on the fly (use TeeinputStream?)
        String userId = request.getHeader(USERID_KEY);
        String userName = request.getHeader(USERNAME_KEY);

        try {
            if (JakartaServletFileUpload.isMultipartContent(request)) {
                final JakartaServletFileUpload upload = new JakartaServletFileUpload();
                final FileItemInputIterator fileItemIterator = upload.getItemIterator(request);

                if (fileItemIterator.hasNext()) {
                    final FileItemInput fileItemStream = fileItemIterator.next();
                    final String fieldName = fileItemStream.getFieldName();

                    if (TRAINING_PACKAGE.equals(fieldName)) {
                        return packagerService.handleDataAndJob(userId, userName, trainingPackageService,
                                fileItemStream.getName(), fileItemStream.getInputStream(),
                                trainingPackageRepositoryService, JobType.TrainingPackageSource);
                    } else {
                        throw new MxeBadRequestException("Invalid request, only one field can be chosen from: "
                                + StringUtils.join(trainingPackageService.getValidFormFields(), ","));
                    }
                } else {
                    throw new MxeBadRequestException("Invalid request, one field must be given: "
                            + StringUtils.join(trainingPackageService.getValidFormFields(), ","));
                }
            } else {
                throw new MxeBadRequestException("Request is not a multipart request!");
            }
        } catch (ApiException | IOException e) {
            throw new MxeInternalException(e);
        }
    }

    @PostMapping("/{id}/unknown")
    public ModelTrainingServiceResponse replaceUnknown(@PathVariable @NotNull final String id,
            @RequestBody final CreateTrainingPackageRequest request) {
        return trainingPackageService.updateUnknown(id, request);
    }

    @PatchMapping("/{id}/{version}")
    public ModelTrainingServiceResponse updateTrainingPackage(@PathVariable @NotNull final String id,
            @PathVariable @NotNull final String version, @RequestBody UpdateTrainingPackageRequest request) {
        return trainingPackageService.update(id, version, request);
    }

    @DeleteMapping("/{id}/{version}")
    public ModelTrainingServiceResponse deleteTrainingPackage(@PathVariable @NotNull final String id,
            @PathVariable @NotNull final String version) {
        return trainingPackageService.delete(id, version);
    }
}
