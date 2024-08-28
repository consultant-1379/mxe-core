package com.ericsson.mxe.modelservice;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.validation.annotation.Validated;
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
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.modelservice.config.properties.DockerProperties;
import com.ericsson.mxe.modelservice.dto.request.CreateModelDeploymentRequest;
import com.ericsson.mxe.modelservice.dto.request.PatchModelDeploymentRequest;
import com.ericsson.mxe.modelservice.dto.response.GetModelDeploymentResponse;
import com.ericsson.mxe.modelservice.dto.response.ModelDeploymentResponse;
import com.ericsson.mxe.modelservice.modelcatalog.DockerImage;
import com.ericsson.mxe.modelservice.modelcatalog.ModelCatalogResolver;
import com.ericsson.mxe.modelservice.modelcatalog.ModelCatalogService;
import com.ericsson.mxe.modelservice.seldondeployment.SeldonDeploymentOperator;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
@RequestMapping("/v1/model-services")
public class ModelServiceController {
    private static final Logger logger = LogManager.getLogger(ModelServiceController.class);

    private final SeldonDeploymentOperator seldonDeploymentOperator;
    private final ModelCatalogService modelCatalogService;
    private final KubernetesService kubernetesService;
    private final DockerProperties dockerProperties;

    private static final String USERNAME_KEY = "x-auth-userid";
    private static final String USERID_KEY = "x-auth-subject";

    public ModelServiceController(SeldonDeploymentOperator seldonDeploymentOperator,
            ModelCatalogService modelCatalogService, KubernetesService kubernetesService, MxeUtils mxeUtils,
            DockerProperties dockerProperties) {
        logger.info("Loading ModelServiceController");
        this.seldonDeploymentOperator = seldonDeploymentOperator;
        this.modelCatalogService = modelCatalogService;
        this.kubernetesService = kubernetesService;
        this.dockerProperties = dockerProperties;
    }

    @JsonView(Views.Default.class)
    @GetMapping()
    public List<GetModelDeploymentResponse> getDeploymentList() {
        ModelCatalogResolver resolver = createModelCatalogResolver();
        return seldonDeploymentOperator.getMxeModelDeployments().stream()
                .map(item -> MxeUtils.mxeSeldonDeploymentInfoToGetModelDeploymentResponse(resolver, item))
                .collect(Collectors.toList());
    }

    @JsonView(Views.Default.class)
    @GetMapping("/{deploymentName}")
    public GetModelDeploymentResponse getDeployment(@PathVariable String deploymentName) {
        ModelCatalogResolver resolver = createModelCatalogResolver();
        return seldonDeploymentOperator.getMxeModelDeployment(deploymentName, resolver);
    }

    @JsonView(Views.Default.class)
    @GetMapping(params = {"modelId", "modelVersion"})
    public List<GetModelDeploymentResponse> getDeploymentsByModel(@RequestParam String modelId,
            @RequestParam String modelVersion) {
        ModelCatalogResolver resolver = createModelCatalogResolver();
        DockerImage dockerImage =
                resolver.getImageForPackage(modelId, modelVersion).orElseThrow(() -> new MxeInternalException(
                        "Model with id \"" + modelId + "\" version \"" + modelVersion + "\" not found"));

        return seldonDeploymentOperator.getMxeModelDeploymentsByImage(dockerImage.getTag(), resolver);
    }

    @JsonView(Views.ShowPermittedActions.class)
    @GetMapping(params = {"showPermittedActions"})
    public List<GetModelDeploymentResponse> getDeploymentList(@RequestParam boolean showPermittedActions) {
        return this.getDeploymentList();
    }

    @JsonView(Views.ShowPermittedActions.class)
    @GetMapping(value = "/{deploymentName}", params = {"showPermittedActions"})
    public GetModelDeploymentResponse getDeployment(@PathVariable String deploymentName,
            @RequestParam boolean showPermittedActions) {
        return this.getDeployment(deploymentName);
    }

    @JsonView(Views.ShowPermittedActions.class)
    @GetMapping(params = {"modelId", "modelVersion", "showPermittedActions"})
    public List<GetModelDeploymentResponse> getDeploymentsByModel(@RequestParam String modelId,
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
        Set<String> kubernetesDeployments = seldonDeploymentOperator.getKubernetesDeployments(serviceName);
        return kubernetesService.getLogsForDeployments(kubernetesDeployments, limit, lastSeconds, tailLines);
    }

    @PostMapping(consumes = "application/json")
    public ModelDeploymentResponse postDeployment(@RequestBody @Validated CreateModelDeploymentRequest request,
            @RequestHeader(value = USERID_KEY, required = false) String userId,
            @RequestHeader(value = USERNAME_KEY, required = false) String userName) {
        ModelCatalogResolver resolverAccessible = createModelCatalogResolverWithAccessControl();
        return seldonDeploymentOperator.createMxeModelDeployment(request, userId, userName, resolverAccessible);
    }

    @DeleteMapping("/{deploymentName}")
    public ModelDeploymentResponse deleteDeployment(@PathVariable String deploymentName) {
        return seldonDeploymentOperator.deleteMxeModelDeployment(deploymentName);
    }

    @PatchMapping(path = "/{deploymentName}", consumes = "application/json")
    public ModelDeploymentResponse patchDeployment(@PathVariable String deploymentName,
            @RequestBody @Validated PatchModelDeploymentRequest request) {
        ModelCatalogResolver resolverAll = createModelCatalogResolver();
        ModelCatalogResolver resolverAccessible = createModelCatalogResolverWithAccessControl();
        return seldonDeploymentOperator.patchMxeModelDeployment(deploymentName, request, resolverAll,
                resolverAccessible);
    }

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
