package com.ericsson.mxe.modelcatalogueservice.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ericsson.mxe.backendservicescommon.dto.Views;
import com.ericsson.mxe.backendservicescommon.dto.status.PackageStatus;
import com.ericsson.mxe.backendservicescommon.exception.MxeBadRequestException;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.modelcatalogueservice.ModelCatalogueServiceApplication;
import com.ericsson.mxe.modelcatalogueservice.dto.JobType;
import com.ericsson.mxe.modelcatalogueservice.dto.Model;
import com.ericsson.mxe.modelcatalogueservice.dto.request.CreateModelRequest;
import com.ericsson.mxe.modelcatalogueservice.dto.request.UpdateModelRequest;
import com.ericsson.mxe.modelcatalogueservice.dto.response.ModelCatalogueServiceResponse;
import com.ericsson.mxe.modelcatalogueservice.services.ModelService;
import com.ericsson.mxe.modelcatalogueservice.services.PackagerService;
import com.ericsson.mxe.modelcatalogueservice.services.minio.ModelSourceRepositoryService;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/v1/models")
@MultipartConfig
public class ModelCatalogueServiceController {

    public static final String ARCHIVE_FILE = "archivefile";
    public static final String MODEL_DATA = "modeldata";
    public static final String SOURCE_FILE = "sourcefile";

    private static final Logger logger = LogManager.getLogger(ModelCatalogueServiceController.class);

    private final ModelService modelService;
    private final PackagerService packagerService;
    private final ObjectMapper mapper;
    private final ModelSourceRepositoryService modelSourceRepositoryService;

    public ModelCatalogueServiceController(final ModelService modelService, final PackagerService packagerService,
            final ObjectMapper mapper, final ModelSourceRepositoryService modelSourceRepositoryService) {
        this.modelService = modelService;
        this.packagerService = packagerService;
        this.mapper = mapper;
        this.modelSourceRepositoryService = modelSourceRepositoryService;
    }

    @JsonView(Views.Default.class)
    @GetMapping()
    public List<Model> getModelList() {
        return this.modelService.list();
    }

    @JsonView(Views.Default.class)
    @GetMapping(params = {"id"})
    public List<Model> getModelList(@RequestParam String id) {
        return this.modelService.listById(id);
    }

    @JsonView(Views.ShowPermittedActions.class)
    @GetMapping(params = {"showPermittedActions"})
    public List<Model> getModelList(@RequestParam boolean showPermittedActions) {
        List<Model> models = this.modelService.list();
        return models;
    }

    @JsonView(Views.ShowPermittedActions.class)
    @GetMapping(params = {"id", "showPermittedActions"})
    public List<Model> getModelListById(@RequestParam String id, @RequestParam boolean showPermittedActions) {
        List<Model> models = this.modelService.listById(id);
        return models;
    }

    @PostMapping
    public ModelCatalogueServiceResponse postModel(final HttpServletRequest request) {
        String userId = request.getHeader(ModelCatalogueServiceApplication.USERID_KEY);
        String userName = request.getHeader(ModelCatalogueServiceApplication.USERNAME_KEY);
        try {
            if (JakartaServletFileUpload.isMultipartContent(request)) {
                final JakartaServletFileUpload upload = new JakartaServletFileUpload();
                final FileItemInputIterator fileItemIterator = upload.getItemIterator(request);

                if (fileItemIterator.hasNext()) {
                    final FileItemInput fileItemStream = fileItemIterator.next();
                    final String fieldName = fileItemStream.getFieldName();

                    switch (fieldName) {
                        case MODEL_DATA:
                            return handleDockerOnboarding(userId, userName, fileItemStream.getInputStream());
                        case SOURCE_FILE:
                            return packagerService.handleDataAndJob(userId, userName, modelService,
                                    fileItemStream.getName(), fileItemStream.getInputStream(),
                                    modelSourceRepositoryService, JobType.Source);
                        case ARCHIVE_FILE:
                            return packagerService.handleDataAndJob(userId, userName, modelService,
                                    fileItemStream.getName(), fileItemStream.getInputStream(),
                                    modelSourceRepositoryService, JobType.Archive);
                        default:
                            throw new MxeBadRequestException("Invalid request, only one field can be chosen from: "
                                    + StringUtils.join(modelService.getValidFormFields(), ","));
                    }
                } else {
                    throw new MxeBadRequestException("Invalid request, one field must be given: "
                            + StringUtils.join(modelService.getValidFormFields(), ","));
                }
            } else {
                throw new MxeBadRequestException("Request is not a multipart request!");
            }
        } catch (IOException | ApiException e) {
            throw new MxeInternalException(e);
        }
    }

    @PostMapping("/{id}/unknown")
    public ModelCatalogueServiceResponse replaceUnknown(@PathVariable @NotNull final String id,
            @RequestBody CreateModelRequest request) {
        return modelService.updateUnknown(id, request);
    }

    @PatchMapping("/{id}/{version}")
    public ModelCatalogueServiceResponse updateModel(@PathVariable @NotNull final String id,
            @PathVariable @NotNull final String version, @RequestBody UpdateModelRequest request) {
        return modelService.update(id, version, request);
    }

    @DeleteMapping("/{id}/{version}")
    public ModelCatalogueServiceResponse deleteModel(@PathVariable @NotNull final String id,
            @PathVariable @NotNull final String version) {
        return modelService.delete(id, version);
    }

    @DeleteMapping("/package/{packageId}")
    public ModelCatalogueServiceResponse deletePackage(@PathVariable @NotNull final String packageId) {
        return modelService.deletePackage(packageId);

    }

    private ModelCatalogueServiceResponse handleDockerOnboarding(String userId, String userName,
            InputStream inputStream) throws IOException {
        final CreateModelRequest request = mapper.readValue(inputStream, CreateModelRequest.class);
        logger.info("Docker onboarding with metadata: " + request);
        return modelService.create(userId, userName, request, PackageStatus.Available, false);
    }

}
