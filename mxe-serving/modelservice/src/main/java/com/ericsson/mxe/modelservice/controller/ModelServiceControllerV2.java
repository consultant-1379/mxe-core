
package com.ericsson.mxe.modelservice.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.fileupload2.core.AbstractFileUpload;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.commons.fileupload2.jakarta.JakartaServletRequestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ericsson.mxe.backendservicescommon.dto.Views;
import com.ericsson.mxe.backendservicescommon.exception.MxeBadRequestException;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.modelservice.aop.annotation.LogExecutionTime;
import com.ericsson.mxe.modelservice.config.properties.DockerProperties;
import com.ericsson.mxe.modelservice.controller.input.SeldonManifestRequest;
import com.ericsson.mxe.modelservice.controller.output.GetSeldonManifestResponse;
import com.ericsson.mxe.modelservice.controller.output.SeldonManifestResponse;
import com.ericsson.mxe.modelservice.controller.utils.MxeRestUtils;
import com.ericsson.mxe.modelservice.deployer.MxeDeployerService;
import com.ericsson.mxe.modelservice.modelcatalog.DockerImage;
import com.ericsson.mxe.modelservice.modelcatalog.ModelCatalogResolver;
import com.ericsson.mxe.modelservice.modelcatalog.ModelCatalogService;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v2/model-services")
public class ModelServiceControllerV2 {
    private static final Logger logger = LogManager.getLogger(ModelServiceControllerV2.class);

    private static final String USERNAME_KEY = "x-auth-userid";
    private static final String USERID_KEY = "x-auth-subject";

    private final MxeDeployerService mxeDeployerService;
    private final ModelCatalogService modelCatalogService;
    private final DockerProperties dockerProperties;
    private final KubernetesService kubernetesService;

    public ModelServiceControllerV2(ModelCatalogService modelCatalogService, MxeDeployerService mxeDeployerService,
            KubernetesService kubernetesService, DockerProperties dockerProperties) {
        this.modelCatalogService = modelCatalogService;
        this.mxeDeployerService = mxeDeployerService;
        this.kubernetesService = kubernetesService;
        this.dockerProperties = dockerProperties;
    }

    @JsonView(Views.Default.class)
    @GetMapping()
    public List<GetSeldonManifestResponse> getDeploymentList() {
        ModelCatalogResolver resolver = createModelCatalogResolver();
        return mxeDeployerService.getMxeModelDeployments().stream()
                .map(item -> MxeRestUtils.mxeSeldonDeploymentInfoToGetSeldonManifestResponse(resolver, item))
                .collect(Collectors.toList());
    }

    @JsonView(Views.Default.class)
    @GetMapping("/{deploymentName}")
    public GetSeldonManifestResponse getDeployment(@PathVariable String deploymentName) {
        ModelCatalogResolver resolver = createModelCatalogResolver();
        return mxeDeployerService.getMxeModelDeployment(deploymentName, resolver);
    }

    @JsonView(Views.Default.class)
    @GetMapping(params = {"modelId", "modelVersion"})
    public List<GetSeldonManifestResponse> getDeploymentsByModel(@RequestParam String modelId,
            @RequestParam String modelVersion) {
        ModelCatalogResolver resolver = createModelCatalogResolver();
        DockerImage dockerImage =
                resolver.getImageForPackage(modelId, modelVersion).orElseThrow(() -> new MxeInternalException(
                        "Model with id \"" + modelId + "\" version \"" + modelVersion + "\" not found"));

        return mxeDeployerService.getMxeModelDeploymentsByImage(dockerImage.getTag(), resolver);
    }

    @JsonView(Views.ShowPermittedActions.class)
    @GetMapping(params = {"showPermittedActions"})
    public List<GetSeldonManifestResponse> getDeploymentList(@RequestParam boolean showPermittedActions) {
        return this.getDeploymentList();
    }

    @JsonView(Views.ShowPermittedActions.class)
    @GetMapping(value = "/{deploymentName}", params = {"showPermittedActions"})
    public GetSeldonManifestResponse getDeployment(@PathVariable String deploymentName,
            @RequestParam boolean showPermittedActions) {
        return this.getDeployment(deploymentName);
    }

    @JsonView(Views.ShowPermittedActions.class)
    @GetMapping(params = {"modelId", "modelVersion", "showPermittedActions"})
    public List<GetSeldonManifestResponse> getDeploymentsByModel(@RequestParam String modelId,
            @RequestParam String modelVersion, @RequestParam boolean showPermittedActions) {
        return this.getDeploymentsByModel(modelId, modelVersion);
    }

    @GetMapping("/{serviceName}/logs")
    public Map<String, String> getDeploymentLogs(@PathVariable String serviceName,
            @RequestParam(defaultValue = "50000") Integer limit,
            @RequestParam(name = "seconds", required = false) Integer lastSeconds,
            @RequestParam(name = "lines", required = false) Integer tailLines) {
        if (tailLines == null && lastSeconds == null) {
            tailLines = 100;
        }
        Set<String> kubernetesDeployments = mxeDeployerService.getKubernetesDeployments(serviceName);
        return kubernetesService.getLogsForDeployments(kubernetesDeployments, limit, lastSeconds, tailLines);
    }

    @LogExecutionTime
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SeldonManifestResponse createDeployment(final HttpServletRequest request) {
        logger.info("Creating Seldon Deployment using manifest file");

        String userId = request.getHeader(USERID_KEY);
        String userName = request.getHeader(USERNAME_KEY);

        if (JakartaServletFileUpload.isMultipartContent(request)) {
            SeldonManifestRequest seldonManifestRequest = MxeRestUtils.parseMultipartRequest(request);
            MxeRestUtils.validateMultipartPostRequest(seldonManifestRequest);

            ModelCatalogResolver resolverAll = createModelCatalogResolver();
            ModelCatalogResolver resolverAccessible = createModelCatalogResolverWithAccessControl();
            return mxeDeployerService.createMxeModelDeployment(seldonManifestRequest, userId, userName, resolverAll,
                    resolverAccessible);
        } else {
            throw new MxeBadRequestException("Request is not a multipart request!");
        }
    }

    @LogExecutionTime
    @PatchMapping("/{deploymentName}")
    public SeldonManifestResponse patchDeployment(final HttpServletRequest request,
            @PathVariable String deploymentName) {
        logger.info("Updating Seldon Deployment \"{}\" using manifest file", deploymentName);
        String userId = request.getHeader(USERID_KEY);
        String userName = request.getHeader(USERNAME_KEY);
        if (AbstractFileUpload.isMultipartContent(new JakartaServletRequestContext(request))) {
            SeldonManifestRequest seldonManifestRequest = MxeRestUtils.parseMultipartRequest(request);
            MxeRestUtils.validateMultipartPatchRequest(deploymentName, seldonManifestRequest);

            ModelCatalogResolver resolverAll = createModelCatalogResolver();
            ModelCatalogResolver resolverAccessible = createModelCatalogResolverWithAccessControl();

            return mxeDeployerService.patchMxeModelDeployment(seldonManifestRequest, userId, userName, resolverAll,
                    resolverAccessible);
        } else {
            throw new MxeBadRequestException("Request is not a multipart request!");
        }
    }

    @LogExecutionTime
    @DeleteMapping("/{deploymentName}")
    public SeldonManifestResponse deleteDeployment(@PathVariable String deploymentName) {
        logger.info("Deleting Seldon Deployment '{}'", deploymentName);

        return mxeDeployerService.deleteMxeModelDeployment(deploymentName);

    }

    @PostMapping(path = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SeldonManifestResponse validateManifest(final HttpServletRequest request) {
        String userId = request.getHeader(USERID_KEY);
        String userName = request.getHeader(USERNAME_KEY);
        logger.info("Validating Seldon Deployment using manifest file: userName {}", userName);

        if (AbstractFileUpload.isMultipartContent(new JakartaServletRequestContext(request))) {
            SeldonManifestRequest seldonManifestRequest = MxeRestUtils.parseMultipartRequest(request);
            MxeRestUtils.validateMultipartPostRequest(seldonManifestRequest);
            mxeDeployerService.validateMxeDeploymentManifest(seldonManifestRequest, true, HttpMethod.POST.name());

            return new SeldonManifestResponse("Valid Seldon Manifest file");
        } else {
            throw new MxeBadRequestException("Request is not a multipart request!");
        }
    }

    @PatchMapping(path = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SeldonManifestResponse validateManifestPatch(final HttpServletRequest request) {
        String userId = request.getHeader(USERID_KEY);
        String userName = request.getHeader(USERNAME_KEY);
        logger.info("Validating Seldon Deployment using manifest file: userName {}", userName);

        if (AbstractFileUpload.isMultipartContent(new JakartaServletRequestContext(request))) {
            SeldonManifestRequest seldonManifestRequest = MxeRestUtils.parseMultipartRequest(request);
            MxeRestUtils.validateMultipartPostRequest(seldonManifestRequest);
            mxeDeployerService.validateMxeDeploymentManifest(seldonManifestRequest, true, HttpMethod.PATCH.name());

            return new SeldonManifestResponse("Valid Seldon Manifest file");
        } else {
            throw new MxeBadRequestException("Request is not a multipart request!");
        }
    }

    /*
     * Private methods
     */
    private ModelCatalogResolver createModelCatalogResolver() {
        return createModelCatalogResolver(false);
    }

    private ModelCatalogResolver createModelCatalogResolverWithAccessControl() {
        // Access control based on domain removed in MXE 2.5
        return createModelCatalogResolver(false);
    }

    private ModelCatalogResolver createModelCatalogResolver(boolean withModelAccessControl) {
        return new ModelCatalogResolver(modelCatalogService.getModelCatalog(withModelAccessControl),
                this.dockerProperties);
    }
}
