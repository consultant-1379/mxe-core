package com.ericsson.mxe.modelservice.deployer;

import static com.ericsson.mxe.modelservice.seldondeployment.dto.SeldonDeploymentInfo.getSeldonDeploymentInfo;
import static com.ericsson.mxe.securitycommon.accesscontrol.Action.ALL;
import static com.ericsson.mxe.securitycommon.accesscontrol.Action.DELETE;
import static com.ericsson.mxe.securitycommon.accesscontrol.Action.READ;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.ericsson.mxe.backendservicescommon.exception.MxeConflictException;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.backendservicescommon.exception.MxeResourceNotFoundException;
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.modelservice.aop.annotation.LogExecutionTime;
import com.ericsson.mxe.modelservice.config.properties.SeldonProperties;
import com.ericsson.mxe.modelservice.controller.input.SeldonManifestHolder;
import com.ericsson.mxe.modelservice.controller.input.SeldonManifestRequest;
import com.ericsson.mxe.modelservice.controller.output.GetSeldonManifestResponse;
import com.ericsson.mxe.modelservice.controller.output.SeldonManifestResponse;
import com.ericsson.mxe.modelservice.controller.utils.MxeRestUtils;
import com.ericsson.mxe.modelservice.deployer.domain.MxeDeployerModelInfo;
import com.ericsson.mxe.modelservice.deployer.domain.MxeDeployerRequestData;
import com.ericsson.mxe.modelservice.deployer.utils.MxeDeployerConstants;
import com.ericsson.mxe.modelservice.deployer.utils.MxeDeployerUtils;
import com.ericsson.mxe.modelservice.dto.AutoscalingData;
import com.ericsson.mxe.modelservice.dto.AutoscalingMetric;
import com.ericsson.mxe.modelservice.modelcatalog.ModelCatalogResolver;
import com.ericsson.mxe.modelservice.modelcatalog.dto.ModelPackageData;
import com.ericsson.mxe.modelservice.seldondeployment.MxeSeldonDeploymentInfo;
import com.ericsson.mxe.modelservice.seldondeployment.dto.SeldonDeploymentInfo;
import com.ericsson.mxe.securitycommon.accesscontrol.AccessControl;
import com.ericsson.mxe.securitycommon.accesscontrol.Action;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1DeleteOptions;

@Service
public class MxeDeployerService {
    private static final Logger logger = LogManager.getLogger(MxeDeployerService.class);

    private static final String MODEL_SERVICE_CREATE_SUCCESS =
            "Model service \"%s\" has been created with model%s \"%s\"%s, with %s instance%s%s%s";
    private static final String MODEL_SERVICE_UPDATE_SUCCESS =
            "Model service \"%s\" has been updated with model%s \"%s\"%s, with %s instance%s%s%s";
    private static final String MODEL_SERVICE_DELETE_SUCCESS = "Model service \"%s\" has been deleted on cluster";
    private static final String MODEL_SERVICE_NOT_RUNNING = "Model service \"%s\" is not running on cluster";

    public static final String NO_PERMISSION_TO_MODEL_SERVICE = "There is no permission to %s model service %s%s";
    public static final String IN_DOMAIN = " in domain %s";
    public static final String TO_DOMAIN = " to domain %s";

    private final SeldonProperties seldonProperties;
    private final KubernetesService kubernetesService;

    private final ManifestPreprocessor manifestPreprocessor;
    private final ManifestValidatorService manifestValidatorService;

    public MxeDeployerService(final KubernetesService kubernetesService, final SeldonProperties seldonProperties,
            final ManifestPreprocessor manifestPreprocessor, final ManifestValidatorService manifestValidatorService) {
        this.kubernetesService = kubernetesService;
        this.seldonProperties = seldonProperties;
        this.manifestPreprocessor = manifestPreprocessor;
        this.manifestValidatorService = manifestValidatorService;
    }

    @LogExecutionTime
    public List<MxeSeldonDeploymentInfo> getMxeModelDeployments() {
        return getAllDeployments().collect(Collectors.toList());
    }

    public GetSeldonManifestResponse getMxeModelDeployment(String serviceName, ModelCatalogResolver resolver) {
        return getMxeModelDeployment(serviceName, resolver, false);
    }

    public GetSeldonManifestResponse getMxeModelDeployment(String serviceName, ModelCatalogResolver resolver,
            boolean additionalProps) {
        Map modelServiceMap = findModelServiceWithAccessControl(serviceName, READ, READ);

        MxeSeldonDeploymentInfo mxeSeldonDeploymentInfo =
                getSeldonDeploymentInfo(modelServiceMap).toMxeSeldonDeploymentInfo(seldonProperties);

        GetSeldonManifestResponse manifestResponse =
                MxeRestUtils.mxeSeldonDeploymentInfoToGetSeldonManifestResponse(resolver, mxeSeldonDeploymentInfo);
        manifestResponse.seldonDeploymentJson = modelServiceMap;
        if (additionalProps) {
            manifestResponse.resourceVersion = mxeSeldonDeploymentInfo.resourceVersion;
        }
        return manifestResponse;
    }

    public List<GetSeldonManifestResponse> getMxeModelDeploymentsByImage(String image, ModelCatalogResolver resolver) {
        return getAllDeployments()
                .filter(service -> service.models.stream().anyMatch(m -> m.image.contentEquals(image)))
                .map(item -> MxeRestUtils.mxeSeldonDeploymentInfoToGetSeldonManifestResponse(resolver, item))
                .collect(Collectors.toList());
    }

    @LogExecutionTime
    public SeldonManifestResponse createMxeModelDeployment(SeldonManifestRequest manifestRequest, final String userId,
            final String userName, final ModelCatalogResolver resolverAll,
            final ModelCatalogResolver modelCatalogResolver) {
        try {
            SeldonManifestHolder seldonManifestHolder = manifestRequest.getSeldonManifestHolder();
            final String deploymentName = seldonManifestHolder.getDeploymentName();

            validateMxeDeploymentManifest(manifestRequest, true, HttpMethod.POST.name());

            manifestPreprocessor.createDeploymentPreProcessing(manifestRequest, kubernetesService.getNamespace(),
                    userId, userName, resolverAll);
            MxeDeployerRequestData deployerRequestData = seldonManifestHolder.toMxeDeployerRequestData();
            logger.debug("MxeDeployerRequestData: {}", new ObjectMapper().writeValueAsString(deployerRequestData));


            MxeDeployerUtils.validateCreateRequest(deployerRequestData, modelCatalogResolver);

            createNamespacedCustomObject(deploymentName, manifestRequest.getSeldonManifestHolder().getJsonObject());
            final ModelServiceData requestData = collectDataFromRequest(deployerRequestData, modelCatalogResolver);
            return getSeldonDeploymentResponse(deploymentName, requestData, MODEL_SERVICE_CREATE_SUCCESS);

        } catch (JsonProcessingException e) {
            throw new MxeInternalException(e);
        }
    }

    @LogExecutionTime
    public SeldonManifestResponse patchMxeModelDeployment(SeldonManifestRequest manifestRequest, String userId,
            String userName, ModelCatalogResolver resolverAll, ModelCatalogResolver resolverAccessible) {

        try {
            SeldonManifestHolder seldonManifestHolder = manifestRequest.getSeldonManifestHolder();
            final String deploymentName = seldonManifestHolder.getDeploymentName();

            GetSeldonManifestResponse serviceData =
                    getMxeModelDeployment(deploymentName, resolverAll, /* additionalProps */ true);

            manifestPreprocessor.patchDeploymentPreProcessing(manifestRequest, kubernetesService.getNamespace(), userId,
                    userName, resolverAll, serviceData);

            MxeDeployerRequestData deployerRequestData = seldonManifestHolder.toMxeDeployerRequestData();
            logger.debug("MxeDeployerRequestData: {}", new ObjectMapper().writeValueAsString(deployerRequestData));

            MxeDeployerUtils.validatePatchRequest(deployerRequestData);

            validateMxeDeploymentManifest(manifestRequest, true, HttpMethod.PATCH.name(), serviceData.resourceVersion);

            replaceNamespacedCustomObject(deploymentName, manifestRequest.getSeldonManifestHolder().getJsonObject());
            final ModelServiceData requestData = collectDataFromRequest(deployerRequestData, resolverAll);
            return getSeldonDeploymentResponse(deploymentName, requestData, MODEL_SERVICE_UPDATE_SUCCESS);
        } catch (JsonProcessingException e) {
            throw new MxeInternalException(e);
        }
    }

    @LogExecutionTime
    public SeldonManifestResponse deleteMxeModelDeployment(final String deploymentName) {
        try {
            findModelServiceWithAccessControl(deploymentName, ALL, DELETE);

            final V1DeleteOptions options = new V1DeleteOptions();
            kubernetesService.deleteNamespacedCustomObject(MxeDeployerConstants.SELDON_GROUP,
                    MxeDeployerConstants.SELDON_VERSION, kubernetesService.getNamespace(),
                    MxeDeployerConstants.SELDON_PLURAL, deploymentName, options);

            return new SeldonManifestResponse(MODEL_SERVICE_DELETE_SUCCESS.formatted(deploymentName));
        } catch (ApiException e) {
            if (e.getCode() == HttpStatus.NOT_FOUND.value()) {
                throw new MxeResourceNotFoundException(MODEL_SERVICE_NOT_RUNNING.formatted(deploymentName));
            }
            throw new MxeInternalException(e.getMessage(), e);
        }
    }

    public void validateMxeDeploymentManifest(SeldonManifestRequest manifestRequest, boolean dryRun,
            String httpMethod) {
        validateMxeDeploymentManifest(manifestRequest, dryRun, httpMethod, null);
    }

    public void validateMxeDeploymentManifest(SeldonManifestRequest manifestRequest, boolean dryRun, String httpMethod,
            String resourceVersion) {
        SeldonManifestHolder seldonManifestHolder = manifestRequest.getSeldonManifestHolder();
        final String deploymentName = seldonManifestHolder.getDeploymentName();
        final String apiVersion = seldonManifestHolder.getApiVersion();
        final String kind = seldonManifestHolder.getKind();

        if (!StringUtils.isEmpty(resourceVersion)) {
            seldonManifestHolder.addResourceVersion(resourceVersion);
            manifestValidatorService.validateNamespacedCustomSeldonObject(deploymentName, apiVersion, kind,
                    seldonManifestHolder.getJsonObject(), dryRun, httpMethod);
            // Comments: Resource Version is required in the manifest to update via api
            // seldonManifestHolder.removeResourceVersion();
        } else {
            manifestValidatorService.validateNamespacedCustomSeldonObject(deploymentName, apiVersion, kind,
                    seldonManifestHolder.getJsonObject(), dryRun, httpMethod);
        }

    }

    public Set<String> getKubernetesDeployments(String serviceName) {
        return getSeldonDeploymentInfo(findModelServiceWithAccessControl(serviceName, READ, READ)).getDeployments();
    }

    /*
     * private utility methods
     */

    private SeldonManifestResponse getSeldonDeploymentResponse(String deploymentName, ModelServiceData requestData,
            String messageTemplate) {
        String idsVersions = getIdsAndVersionsString(requestData.ids, requestData.versions);
        String weights = getWeightsString(requestData.weights);
        String replicas = getReplicasString(requestData.replicas, requestData.autoScaling);
        String autoscaling = getAutoscalingDataString(requestData.replicas, requestData.autoScaling);
        boolean hasMoreThanOneReplicas = isReplicaNumberMoreThanOne(requestData.replicas, requestData.autoScaling);
        boolean moreThanOneModel = requestData.ids.size() > 1;

        String message = messageTemplate.formatted(deploymentName, moreThanOneModel ? "s" : "", idsVersions,
                moreThanOneModel ? " with weights " + weights : "", replicas, hasMoreThanOneReplicas ? "s" : "",
                autoscaling, getInDomainMessage(requestData.domain));

        return new SeldonManifestResponse(message);
    }

    private String getIdsAndVersionsString(List<String> ids, List<String> versions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            sb.append(ids.get(i)).append(":").append(versions.get(i)).append(",");
        }
        if (sb.length() > 0) {
            return sb.substring(0, sb.length() - 1);
        }
        return "";
    }

    private String getWeightsString(List<Double> weights) {
        if (weights != null) {
            return Joiner.on(',').join(weights);
        } else {
            return "";
        }
    }

    private boolean isReplicaNumberMoreThanOne(Integer replicas, AutoscalingData autoScalingData) {
        if (autoScalingData != null) {
            return true;
        } else {
            return replicas > 1;
        }
    }

    private String getReplicasString(Integer replicas, AutoscalingData autoScalingData) {
        if (autoScalingData != null) {
            return autoScalingData.minReplicas + "-" + autoScalingData.maxReplicas;
        } else {
            return replicas.toString();
        }
    }

    private String getAutoscalingDataString(Integer replicas, AutoscalingData autoScalingData) {
        if (autoScalingData != null) {
            List<String> metricStrings = Lists.newArrayList();
            for (AutoscalingMetric metric : autoScalingData.metrics) {
                metricStrings.add(metric.name.getResourceName() + ":" + metric.targetAverageValue
                        + metric.name.getResourceUnit());
            }
            return ", with autoscaling metrics " + Joiner.on(',').join(metricStrings);
        } else {
            return "";
        }
    }

    private ModelServiceData collectDataFromRequest(MxeDeployerRequestData request,
            ModelCatalogResolver modelCatalogResolver) {
        final List<ModelPackageData> requestModelPackageDataList =
                getModelPackageData(request.getModels(), modelCatalogResolver);

        ModelServiceData requestData = new ModelServiceData();
        requestData.domain = request.getDomain();
        requestData.ids = requestModelPackageDataList.stream().map(m -> m.id).collect(Collectors.toList());
        requestData.versions = requestModelPackageDataList.stream().map(m -> m.version).collect(Collectors.toList());
        requestData.replicas = request.getReplicas();
        requestData.autoScaling = request.getAutoScaling();
        requestData.weights =
                request.getModels().stream().map(m -> m.weight).filter(Objects::nonNull).collect(Collectors.toList());

        return requestData;
    }

    private List<ModelPackageData> getModelPackageData(final List<MxeDeployerModelInfo> modelInfoList,
            ModelCatalogResolver modelCatalogResolver) {
        return modelInfoList.stream().map(m -> getModelPackageData(m, modelCatalogResolver))
                .collect(Collectors.toList());
    }

    private ModelPackageData getModelPackageData(MxeDeployerModelInfo modelInfo,
            ModelCatalogResolver modelCatalogResolver) {
        ModelPackageData modelPackageData = modelCatalogResolver.getPackageForImage(modelInfo.image)
                .orElseThrow(() -> new MxeInternalException("No package found for \"%s\"".formatted(modelInfo.image)));
        return modelPackageData;
    }

    private Map findModelServiceWithAccessControl(final String serviceName, final Action action,
            final Action actionForPrintout) {
        // Access control based on domain removed in MXE 2.5
        return findModelService(serviceName);
    }

    private Map findModelService(final String name) {
        try {
            Map mxeModelDeployment = getModelService(name);
            printJsonObject(Level.DEBUG, mxeModelDeployment);
            return mxeModelDeployment;
        } catch (ApiException e) {
            if (e.getCode() == HttpStatus.NOT_FOUND.value()) {
                throw new MxeResourceNotFoundException("Model service \"" + name + "\" does not exist");
            }
            throw new MxeInternalException(e.getMessage(), e);
        }
    }

    private Map getModelService(final String name) throws ApiException {
        Map mxeModelDeployment = (Map) kubernetesService.getNamespacedCustomObject(MxeDeployerConstants.SELDON_GROUP,
                MxeDeployerConstants.SELDON_VERSION, kubernetesService.getNamespace(),
                MxeDeployerConstants.SELDON_PLURAL, name);
        return mxeModelDeployment;
    }

    @SuppressWarnings("unchecked")
    private Stream<MxeSeldonDeploymentInfo> getAllDeployments() {
        Map<String, Object> resultMap;

        try {
            resultMap = (Map) kubernetesService.listNamespacedCustomObject(MxeDeployerConstants.SELDON_GROUP,
                    MxeDeployerConstants.SELDON_VERSION, kubernetesService.getNamespace(),
                    MxeDeployerConstants.SELDON_PLURAL);
            List<Map> items = (List) resultMap.get("items");

            printJsonObject(Level.DEBUG, resultMap);

            return items.stream().map(SeldonDeploymentInfo::getSeldonDeploymentInfo)
                    .map(seldonDeploymentInfo -> seldonDeploymentInfo.toMxeSeldonDeploymentInfo(this.seldonProperties));
        } catch (ApiException e) {
            throw new MxeInternalException(e.getMessage(), e);
        }
    }

    /*
     * Private Methods - message construction
     */
    private String getInDomainMessage(String domain) {
        return getDomainMessage(domain, IN_DOMAIN);
    }

    private String getToDomainMessage(String domain) {
        return getDomainMessage(domain, TO_DOMAIN);
    }

    private String getDomainMessage(String domain, String message) {
        return StringUtils.isEmpty(domain) ? "" : message.formatted(domain);
    }


    private static void printJsonObject(final Level level, final Object object) {
        printJsonObject(level, "{}", object);
    }

    private static void printJsonObject(final Level level, final String message, final Object object) {
        try {
            logger.log(level, message, new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object));
        } catch (Exception e) {
            logger.error("Could not log JSON object", e);
        }
    }

    private static class ModelServiceData {
        String domain;
        List<String> ids;
        List<String> versions;
        Integer replicas;
        AutoscalingData autoScaling;
        List<Double> weights;

        @Override
        public String toString() {
            return "ModelServiceData [domain=" + domain + ", ids=" + ids + ", versions=" + versions + ", replicas="
                    + replicas + ", autoScaling=" + autoScaling + ", weights=" + weights + "]";
        }
    }

    private void createNamespacedCustomObject(final String serviceName, final Object m) {
        try {
            kubernetesService.createNamespacedCustomObject(MxeDeployerConstants.SELDON_GROUP,
                    MxeDeployerConstants.SELDON_VERSION, kubernetesService.getNamespace(),
                    MxeDeployerConstants.SELDON_PLURAL, m);
        } catch (ApiException e) {
            if (e.getCode() == HttpStatus.CONFLICT.value()) {
                throw new MxeConflictException("Model service \"" + serviceName + "\" is already running on cluster.");
            }
            throw new MxeInternalException(e.getMessage(), e);
        }
    }

    private void replaceNamespacedCustomObject(final String serviceName, final Object m) {
        try {
            kubernetesService.replaceNamespacedCustomObject(MxeDeployerConstants.SELDON_GROUP,
                    MxeDeployerConstants.SELDON_VERSION, kubernetesService.getNamespace(),
                    MxeDeployerConstants.SELDON_PLURAL, serviceName, m);
        } catch (ApiException e) {
            throw new MxeInternalException(e.getMessage(), e);
        }
    }
}
