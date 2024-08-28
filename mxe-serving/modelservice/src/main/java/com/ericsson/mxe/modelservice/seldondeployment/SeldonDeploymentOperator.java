package com.ericsson.mxe.modelservice.seldondeployment;

import static com.ericsson.mxe.modelservice.seldondeployment.dto.SeldonDeploymentInfo.getSeldonDeploymentInfo;
import static com.ericsson.mxe.securitycommon.accesscontrol.Action.ALL;
import static com.ericsson.mxe.securitycommon.accesscontrol.Action.DELETE;
import static com.ericsson.mxe.securitycommon.accesscontrol.Action.MODIFY;
import static com.ericsson.mxe.securitycommon.accesscontrol.Action.READ;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.ericsson.mxe.backendservicescommon.dto.status.PackageStatus;
import com.ericsson.mxe.backendservicescommon.exception.MxeConflictException;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.backendservicescommon.exception.MxeResourceNotFoundException;
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.modelservice.MxeParameterValidator;
import com.ericsson.mxe.modelservice.MxeUtils;
import com.ericsson.mxe.modelservice.config.properties.SeldonProperties;
import com.ericsson.mxe.modelservice.dto.AutoscalingData;
import com.ericsson.mxe.modelservice.dto.AutoscalingMetric;
import com.ericsson.mxe.modelservice.dto.MxeModelData;
import com.ericsson.mxe.modelservice.dto.request.CreateModelDeploymentRequest;
import com.ericsson.mxe.modelservice.dto.request.PatchModelDeploymentRequest;
import com.ericsson.mxe.modelservice.dto.response.GetModelDeploymentResponse;
import com.ericsson.mxe.modelservice.dto.response.ModelDeploymentResponse;
import com.ericsson.mxe.modelservice.modelcatalog.ModelCatalogResolver;
import com.ericsson.mxe.modelservice.modelcatalog.dto.ModelPackageData;
import com.ericsson.mxe.modelservice.seldondeployment.dto.SeldonDeploymentInfo;
import com.ericsson.mxe.securitycommon.accesscontrol.AccessControl;
import com.ericsson.mxe.securitycommon.accesscontrol.Action;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1DeleteOptions;

@Service
public class SeldonDeploymentOperator {

    private static final Logger logger = LogManager.getLogger(SeldonDeploymentOperator.class);

    private static final String MODEL_SERVICE_CREATE_SUCCESS =
            "Model service \"%s\" has been created with model%s \"%s\"%s, with %s instance%s%s%s";
    private static final String MODEL_SERVICE_PATCH_SUCCESS =
            "Model service \"%s\" has been updated to use model%s \"%s\"%s, with %s instance%s%s%s";
    private static final String MODEL_SERVICE_PATCH_NOT_NEEDED =
            "Nothing to do, model service \"%s\" already contains model%s \"%s\"%s, and has %s instance%s%s%s";
    public static final String NO_PERMISSION_TO_MODEL_SERVICE = "There is no permission to %s model service %s%s";
    public static final String IN_DOMAIN = " in domain %s";
    public static final String TO_DOMAIN = " to domain %s";

    private static final String SELDON_GROUP = "machinelearning.seldon.io";
    private static final String SELDON_VERSION = "v1";
    private static final String SELDON_PLURAL = "seldondeployments";

    private final SeldonProperties seldonProperties;
    private final SeldonDeploymentTemplateHandler seldonDeploymentTemplateHandler;
    private final KubernetesService kubernetesService;

    public SeldonDeploymentOperator(final SeldonProperties seldonProperties,
            final SeldonDeploymentTemplateHandler seldonDeploymentTemplateHandler,
            final KubernetesService kubernetesService, final AccessControl accessControl) {
        this.seldonProperties = seldonProperties;
        this.seldonDeploymentTemplateHandler = seldonDeploymentTemplateHandler;
        this.kubernetesService = kubernetesService;
    }

    public GetModelDeploymentResponse getMxeModelDeployment(String serviceName, ModelCatalogResolver resolver) {
        Map modelServiceMap = findModelServiceWithAccessControl(serviceName, READ, READ);

        MxeSeldonDeploymentInfo mxeSeldonDeploymentInfo =
                getSeldonDeploymentInfo(modelServiceMap).toMxeSeldonDeploymentInfo(seldonProperties);

        return MxeUtils.mxeSeldonDeploymentInfoToGetModelDeploymentResponse(resolver, mxeSeldonDeploymentInfo);
    }

    public List<GetModelDeploymentResponse> getMxeModelDeploymentsByImage(String image, ModelCatalogResolver resolver) {
        return getAllDeployments()
                .filter(service -> service.models.stream().anyMatch(m -> m.image.contentEquals(image)))
                .map(item -> MxeUtils.mxeSeldonDeploymentInfoToGetModelDeploymentResponse(resolver, item))
                .collect(Collectors.toList());
    }

    public List<MxeSeldonDeploymentInfo> getMxeModelDeployments() {
        return getAllDeployments().collect(Collectors.toList());
    }

    public ModelDeploymentResponse createMxeModelDeployment(final CreateModelDeploymentRequest request,
            final String userId, final String userName, final ModelCatalogResolver modelCatalogResolver) {
        final MxeSeldonDeploymentData deploymentData = MxeUtils
                .createModelDeploymentRequest2MxeSeldonDeploymentData(modelCatalogResolver, request, userId, userName);

        checkModelPackageStatus(deploymentData, modelCatalogResolver);

        final String body = seldonDeploymentTemplateHandler.fillTemplate(deploymentData);
        final Object map = mapBody(body);
        createNamespacedCustomObject(deploymentData.name, map);
        final ModelServiceData requestData = collectDataFromRequest(deploymentData, modelCatalogResolver);
        return getModelDeploymentResponse(deploymentData.name, requestData, MODEL_SERVICE_CREATE_SUCCESS);
    }

    public ModelDeploymentResponse deleteMxeModelDeployment(final String serviceName) {
        try {
            Map serviceMap = findModelServiceWithAccessControl(serviceName, ALL, DELETE);


            final V1DeleteOptions options = new V1DeleteOptions();
            kubernetesService.deleteNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION,
                    kubernetesService.getNamespace(), SELDON_PLURAL, serviceName, options);

            return new ModelDeploymentResponse("Model service \"" + serviceName + "\" has been deleted on cluster");
        } catch (ApiException e) {
            if (e.getCode() == HttpStatus.NOT_FOUND.value()) {
                throw new MxeResourceNotFoundException(
                        "Model service \"" + serviceName + "\" is not running on cluster.");
            }
            throw new MxeInternalException(e.getMessage(), e);
        }
    }

    public ModelDeploymentResponse patchMxeModelDeployment(final String serviceName,
            final PatchModelDeploymentRequest request, final ModelCatalogResolver modelCatalogResolverAll,
            final ModelCatalogResolver modelCatalogResolverAccessible) {
        final Map modelServiceMap = findModelServiceWithAccessControl(serviceName, ALL, MODIFY);

        ModelServiceData serviceData = collectDataFromService(modelServiceMap, modelCatalogResolverAll);

        MxeSeldonDeploymentData deploymentData = MxeUtils.patchModelDeploymentRequest2MxeSeldonDeploymentData(
                modelCatalogResolverAccessible, serviceName, request, serviceData.ids.size());

        checkModelPackageStatus(deploymentData, modelCatalogResolverAccessible);

        ModelServiceData requestData = collectDataFromRequest(deploymentData, modelCatalogResolverAccessible);
        ModelServiceDataSet modelServiceDataSet = combineServiceAndRequestData(serviceName, serviceData, requestData);
        boolean isPatchNeeded = isPatchNeeded(modelServiceDataSet);
        if (isPatchNeeded) {
            updateRequestWithMissingData(deploymentData, modelServiceDataSet.requestData, modelCatalogResolverAll);
            final DocumentContext patchedModelService = getPatchedDocumentContext(modelServiceMap, deploymentData);
            patchNamespacedCustomObject(serviceName, patchedModelService);
        }
        String messageTemplate = isPatchNeeded ? MODEL_SERVICE_PATCH_SUCCESS : MODEL_SERVICE_PATCH_NOT_NEEDED;
        return getModelDeploymentResponse(serviceName, modelServiceDataSet.requestData, messageTemplate);
    }

    public Set<String> getKubernetesDeployments(String serviceName) {
        return getSeldonDeploymentInfo(findModelServiceWithAccessControl(serviceName, READ, READ)).getDeployments();
    }

    @SuppressWarnings("unchecked")
    private Stream<MxeSeldonDeploymentInfo> getAllDeployments() {
        Map<String, Object> resultMap;

        try {
            resultMap = (Map) kubernetesService.listNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION,
                    kubernetesService.getNamespace(), SELDON_PLURAL);
            List<Map> items = (List) resultMap.get("items");

            printJsonObject(Level.DEBUG, resultMap);

            return items.stream().map(SeldonDeploymentInfo::getSeldonDeploymentInfo)
                    .map(seldonDeploymentInfo -> seldonDeploymentInfo.toMxeSeldonDeploymentInfo(this.seldonProperties));
        } catch (ApiException e) {
            throw new MxeInternalException(e.getMessage(), e);
        }
    }

    private Object mapBody(final String body) {
        try {
            return new ObjectMapper().readValue(body, Map.class);
        } catch (IOException e) {
            throw new MxeInternalException(e.getMessage(), e);
        }
    }

    private void createNamespacedCustomObject(final String serviceName, final Object m) {
        try {
            kubernetesService.createNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION,
                    kubernetesService.getNamespace(), SELDON_PLURAL, m);
        } catch (ApiException e) {
            if (e.getCode() == HttpStatus.CONFLICT.value()) {
                throw new MxeConflictException("Model service \"" + serviceName + "\" is already running on cluster.");
            }
            throw new MxeInternalException(e.getMessage(), e);
        }
    }

    private void checkModelPackageStatus(final MxeSeldonDeploymentData mxeSeldonDeploymentData,
            final ModelCatalogResolver modelCatalogResolver) {
        final List<String> issues = Lists.newArrayList();

        for (MxeModelInfo mxeModelInfo : mxeSeldonDeploymentData.models) {
            final ModelPackageData modelPackageData =
                    modelCatalogResolver.getPackageForImage(mxeModelInfo.image).orElseThrow();

            if (modelPackageData.status != PackageStatus.Available) {
                issues.add(StringUtils.join("[", modelPackageData.id, ":", modelPackageData.status, "]"));
            }
        }

        if (CollectionUtils.isNotEmpty(issues)) {
            throw new MxeConflictException("Could not perform operation for model service "
                    + mxeSeldonDeploymentData.name + ", invalid model state (" + StringUtils.join(issues, ", ") + ")");
        }
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
        Map mxeModelDeployment = (Map) kubernetesService.getNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION,
                kubernetesService.getNamespace(), SELDON_PLURAL, name);
        return mxeModelDeployment;
    }

    private ModelServiceData collectDataFromService(Map modelServiceMap, ModelCatalogResolver modelCatalogResolver) {
        final MxeSeldonDeploymentInfo mxeSeldonDeploymentInfo =
                getSeldonDeploymentInfo(modelServiceMap).toMxeSeldonDeploymentInfo(seldonProperties);

        final List<ModelPackageData> serviceModelPackageDataList =
                getModelPackageData(mxeSeldonDeploymentInfo.models, modelCatalogResolver);

        List<Double> serviceWeights = Lists.newArrayList();
        if (mxeSeldonDeploymentInfo.models.size() == 2) {
            serviceWeights = mxeSeldonDeploymentInfo.models.stream().map(m -> m.weight).collect(Collectors.toList());
        }

        ModelServiceData serviceData = new ModelServiceData();
        serviceData.ids = serviceModelPackageDataList.stream().map(m -> m.id).collect(Collectors.toList());
        serviceData.versions = serviceModelPackageDataList.stream().map(m -> m.version).collect(Collectors.toList());
        serviceData.replicas = mxeSeldonDeploymentInfo.replicas;
        serviceData.autoScaling = mxeSeldonDeploymentInfo.autoScaling;
        serviceData.weights = serviceWeights;

        return serviceData;
    }

    private ModelServiceData collectDataFromRequest(MxeSeldonDeploymentData request,
            ModelCatalogResolver modelCatalogResolver) {
        final List<ModelPackageData> requestModelPackageDataList =
                getModelPackageData(request.models, modelCatalogResolver);

        ModelServiceData requestData = new ModelServiceData();
        requestData.ids = requestModelPackageDataList.stream().map(m -> m.id).collect(Collectors.toList());
        requestData.versions = requestModelPackageDataList.stream().map(m -> m.version).collect(Collectors.toList());
        requestData.replicas = request.replicas;
        requestData.autoScaling = request.autoScaling;
        requestData.weights = request.weights;

        return requestData;
    }

    private List<ModelPackageData> getModelPackageData(final List<MxeModelInfo> modelInfoList,
            ModelCatalogResolver modelCatalogResolver) {
        return modelInfoList.stream().map(m -> getModelPackageData(m, modelCatalogResolver))
                .collect(Collectors.toList());
    }

    private ModelPackageData getModelPackageData(MxeModelInfo modelInfo, ModelCatalogResolver modelCatalogResolver) {
        ModelPackageData modelPackageData = modelCatalogResolver.getPackageForImage(modelInfo.image)
                .orElseThrow(() -> new MxeInternalException("No package found for \"%s\"".formatted(modelInfo.image)));
        return modelPackageData;
    }

    private ModelServiceDataSet combineServiceAndRequestData(String serviceName, ModelServiceData serviceData,
            ModelServiceData requestData) {

        ModelServiceData updatedRequestData = new ModelServiceData();
        updatedRequestData.domain = Objects.isNull(requestData.domain) ? serviceData.domain : requestData.domain;
        updatedRequestData.ids = requestData.ids.isEmpty() ? serviceData.ids : requestData.ids;
        updatedRequestData.versions = requestData.ids.isEmpty() ? serviceData.versions : requestData.versions;
        updatedRequestData.replicas =
                Objects.isNull(requestData.replicas) ? serviceData.replicas : requestData.replicas;
        updatedRequestData.autoScaling = Objects.isNull(requestData.autoScaling) && Objects.isNull(requestData.replicas)
                ? serviceData.autoScaling
                : requestData.autoScaling;
        updatedRequestData.weights =
                requestData.weights.stream().allMatch(Objects::isNull) ? serviceData.weights : requestData.weights;

        ModelServiceDataSet modelServiceDataSet = new ModelServiceDataSet();
        modelServiceDataSet.serviceData = serviceData;
        modelServiceDataSet.requestData = updatedRequestData;

        logger.info("Patch on model service '{}': {}", serviceName, modelServiceDataSet);

        return modelServiceDataSet;
    }

    private boolean isPatchNeeded(ModelServiceDataSet modelServiceDataSet) {
        boolean ratioAChanged = false;
        if (modelServiceDataSet.requestData.ids.size() == 2 && !modelServiceDataSet.requestData.weights.get(0)
                .equals(modelServiceDataSet.serviceData.weights.get(0))) {
            ratioAChanged = true;
        }
        boolean modelsOrInstancesChanged =
                !modelServiceDataSet.requestData.domain.equals(modelServiceDataSet.serviceData.domain)
                        || (!modelServiceDataSet.requestData.replicas.equals(modelServiceDataSet.serviceData.replicas)
                                && modelServiceDataSet.requestData.autoScaling == null)
                        || (modelServiceDataSet.requestData.autoScaling == null
                                && modelServiceDataSet.serviceData.autoScaling != null)
                        || (modelServiceDataSet.requestData.autoScaling != null
                                && !modelServiceDataSet.requestData.autoScaling
                                        .equals(modelServiceDataSet.serviceData.autoScaling))
                        || !modelServiceDataSet.requestData.ids.equals(modelServiceDataSet.serviceData.ids)
                        || !modelServiceDataSet.requestData.versions.equals(modelServiceDataSet.serviceData.versions);

        return modelsOrInstancesChanged || ratioAChanged;
    }

    private void updateRequestWithMissingData(MxeSeldonDeploymentData request, ModelServiceData updatedRequestData,
            ModelCatalogResolver modelCatalogResolver) {
        List<MxeModelData> models = new ArrayList<>(updatedRequestData.ids.size());
        for (int i = 0; i < updatedRequestData.ids.size(); i++) {
            MxeModelData mxeModelData = new MxeModelData();
            mxeModelData.id = updatedRequestData.ids.get(i);
            mxeModelData.version = updatedRequestData.versions.get(i);
            models.add(mxeModelData);
        }
        List<MxeModelInfo> updatedMxeModelInfoList = models.stream()
                .map(m -> MxeParameterValidator.mxePackage2Model(modelCatalogResolver, m)).collect(Collectors.toList());
        request.models = updatedMxeModelInfoList;
        request.replicas = updatedRequestData.replicas;
        request.autoScaling = updatedRequestData.autoScaling;
        request.weights = updatedRequestData.weights;
    }

    private DocumentContext getPatchedDocumentContext(Map modelServiceMap, MxeSeldonDeploymentData request) {
        int numberOfModels = request.models.size();
        List<String> componentSpecsJsonList = Lists.newArrayList();
        String graphJson;
        if (numberOfModels == 1) {
            componentSpecsJsonList.add(seldonDeploymentTemplateHandler.fillModelSpecTemplate(request));
            graphJson = seldonDeploymentTemplateHandler.fillModelGraphTemplate();
        } else if (numberOfModels == 2) {
            componentSpecsJsonList = seldonDeploymentTemplateHandler.fillStaticSpecTemplate(request);
            graphJson = seldonDeploymentTemplateHandler.fillStaticGraphTemplate(request.weights.get(0));
        } else {
            throw new MxeInternalException("The request contains more than 2 models: %d".formatted(numberOfModels));
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Map> componentSpecsList = Lists.newArrayList();
        Map graphMap;
        try {
            for (String componentSpecsJson : componentSpecsJsonList) {
                Map componentSpecsMap = mapper.readValue(componentSpecsJson, Map.class);
                componentSpecsList.add(componentSpecsMap);
            }
            graphMap = mapper.readValue(graphJson, Map.class);
        } catch (Exception e) {
            throw new MxeInternalException("Couldn't read template into Map: " + e.getMessage());
        }

        final DocumentContext patchedMxeModelDeployment = JsonPath.parse(modelServiceMap);
        final int predictorsCount = JsonPath.read(modelServiceMap, "$.spec.predictors.length()");
        for (int index = 0; index < predictorsCount; index++) {
            patchedMxeModelDeployment.set("spec.predictors[" + index + "].replicas", request.replicas);
            patchedMxeModelDeployment.set("spec.predictors[" + index + "].componentSpecs", componentSpecsList);
            patchedMxeModelDeployment.set("spec.predictors[" + index + "].graph", graphMap);
        }

        // No need to have the status here
        patchedMxeModelDeployment.delete("status");
        // Remove resourceVersion in order to avoid Conflict when the resource was modified since it was GET'ed
        patchedMxeModelDeployment.delete("metadata.resourceVersion");

        return patchedMxeModelDeployment;
    }

    private void patchNamespacedCustomObject(final String serviceName,
            final DocumentContext patchedMxeModelDeployment) {
        try {
            kubernetesService.patchNamespacedCustomObject(SELDON_GROUP, SELDON_VERSION,
                    kubernetesService.getNamespace(), SELDON_PLURAL, serviceName, patchedMxeModelDeployment.json());
        } catch (ApiException e) {
            throw new MxeInternalException(e.getMessage(), e);
        }
    }

    private ModelDeploymentResponse getModelDeploymentResponse(String name, ModelServiceData requestData,
            String messageTemplate) {
        String idsVersions = getIdsAndVersionsString(requestData.ids, requestData.versions);
        String weights = getWeightsString(requestData.weights);
        String replicas = getReplicasString(requestData.replicas, requestData.autoScaling);
        String autoscaling = getAutoscalingDataString(requestData.replicas, requestData.autoScaling);
        boolean hasMoreThanOneReplicas = isReplicaNumberMoreThanOne(requestData.replicas, requestData.autoScaling);
        boolean moreThanOneModel = requestData.ids.size() > 1;

        String message = messageTemplate.formatted(name, moreThanOneModel ? "s" : "", idsVersions,
                moreThanOneModel ? " with weights " + weights : "", replicas, hasMoreThanOneReplicas ? "s" : "",
                autoscaling, getInDomainMessage(requestData.domain));
        return new ModelDeploymentResponse(message);
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
        return Joiner.on(',').join(weights);
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

    private String getInDomainMessage(String domain) {
        return getDomainMessage(domain, IN_DOMAIN);
    }

    private String getToDomainMessage(String domain) {
        return getDomainMessage(domain, TO_DOMAIN);
    }

    private String getDomainMessage(String domain, String message) {
        return StringUtils.isEmpty(domain) ? "" : message.formatted(domain);
    }

    private static class ModelServiceDataSet {
        ModelServiceData serviceData;
        ModelServiceData requestData;

        @Override
        public String toString() {
            return "ModelServiceDataSet [serviceData=" + serviceData + ", requestData=" + requestData + "]";
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

}
