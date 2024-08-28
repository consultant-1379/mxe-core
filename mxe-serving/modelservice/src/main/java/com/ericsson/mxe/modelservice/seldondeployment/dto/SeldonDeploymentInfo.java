package com.ericsson.mxe.modelservice.seldondeployment.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.modelservice.MxeParameterValidator;
import com.ericsson.mxe.modelservice.config.properties.SeldonProperties;
import com.ericsson.mxe.modelservice.dto.AutoscalingData;
import com.ericsson.mxe.modelservice.dto.AutoscalingMetric;
import com.ericsson.mxe.modelservice.dto.AutoscalingMetricName;
import com.ericsson.mxe.modelservice.dto.AutoscalingMetricResourceName;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentStatus;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentType;
import com.ericsson.mxe.modelservice.seldondeployment.MxeModelInfo;
import com.ericsson.mxe.modelservice.seldondeployment.MxeSeldonDeploymentInfo;
import com.ericsson.mxe.modelservice.seldondeployment.dto.SeldonDeploymentInfo.DeploymentSpec.PredictorSpec.PodTemplateSpec.HpaSpec;
import com.ericsson.mxe.modelservice.seldondeployment.dto.SeldonDeploymentInfo.DeploymentSpec.PredictorSpec.PodTemplateSpec.HpaSpec.Metric;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SeldonDeploymentInfo {

    private static final Logger logger = LogManager.getLogger(SeldonDeploymentInfo.class);

    static final String INVALID_UNIT_FOUND_IN_SELDON_DEPLOYMENT = "Invalid unit found in seldon deployment: %s";
    static final String INVALID_METRIC_NAME_FOUND_IN_SELDON_DEPLOYMENT =
            "Invalid metric name found in seldon deployment: %s";

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Metadata {
        public String name;
        public String resourceVersion;
        public Map<String, Object> annotations;
        public Map<String, Object> labels;
        public String creationTimestamp;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class DeploymentSpec {
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class PredictorSpec {
            @JsonIgnoreProperties(ignoreUnknown = true)
            static class PredictiveUnit {
                @JsonIgnoreProperties(ignoreUnknown = true)
                static class Endpoint {
                    public String type;
                }
                @JsonIgnoreProperties(ignoreUnknown = true)
                static class Parameter {
                    public String name;
                    public String value;
                    public String type;
                }

                public String name;
                public String type;
                public String implementation;
                public Endpoint endpoint;
                public List<PredictiveUnit> children;
                public List<Parameter> parameters;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            static class PodTemplateSpec {
                @JsonIgnoreProperties(ignoreUnknown = true)
                static class PodSpec {
                    @JsonIgnoreProperties(ignoreUnknown = true)
                    static class ContainerSpec {
                        public String image;
                    }

                    public Map metadata;
                    public List<ContainerSpec> containers;
                }

                @JsonIgnoreProperties(ignoreUnknown = true)
                static class HpaSpec {
                    @JsonIgnoreProperties(ignoreUnknown = true)
                    static class Metric {
                        @JsonIgnoreProperties(ignoreUnknown = true)
                        static class Resource {
                            public String name;
                            public String targetAverageValue;
                        }

                        public Resource resource;
                    }

                    public Map metadata;
                    public Integer minReplicas;
                    public Integer maxReplicas;
                    public List<Metric> metrics;
                }

                public Map metadata;
                public PodSpec spec;
                public HpaSpec hpaSpec;
            }

            public String name;
            public Integer replicas;
            public PredictiveUnit graph;
            public List<PodTemplateSpec> componentSpecs;
        }

        public String name;
        public Map<String, String> annotations;
        public List<PredictorSpec> predictors;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Status {
        public String state;
        public String description;
        public Map<String, Object> deploymentStatus;
    }

    public String apiVersion;
    public String kind;
    public Metadata metadata;
    public DeploymentSpec spec;
    public Status status;

    public static SeldonDeploymentInfo getSeldonDeploymentInfo(Map map) {
        return new ObjectMapper().convertValue(map, SeldonDeploymentInfo.class);
    }

    public MxeSeldonDeploymentInfo toMxeSeldonDeploymentInfo(final SeldonProperties seldonProperties) {
        validate(seldonProperties);
        MxeSeldonDeploymentInfo deployment = new MxeSeldonDeploymentInfo();

        deployment.name = metadata.name;
        deployment.type = getType();
        deployment.created = metadata.creationTimestamp;
        deployment.status = status != null ? getStatus(status.state) : getStatus(null);
        deployment.message = status != null ? status.description : null;
        deployment.replicas = getPredictor().replicas;
        deployment.autoScaling = getAutoscalingData();
        deployment.models = getModels();
        deployment.createdByUserId = getUserId();
        deployment.createdByUserName = getUserName();
        deployment.resourceVersion = metadata.resourceVersion;

        return deployment;
    }

    public Set<String> getDeployments() {
        return status.deploymentStatus.keySet();
    }

    private String getUserId() {
        try {
            return metadata.labels.get("mxe/createdbyuserid").toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String getUserName() {
        try {
            return metadata.labels.get("mxe/createdbyusername").toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String getDeploymentDomain() {
        try {
            return metadata.labels.get("mxe/domain").toString();
        } catch (Exception e) {
            return "";
        }
    }

    private void validate(final SeldonProperties seldonProperties) {
        if (!seldonProperties.getCrd().getName().equals(kind))
            throw new RuntimeException("Invalid kind");
        if (spec.predictors.size() != 1)
            throw new RuntimeException("Expected number of predictors is 1");
    }

    private MxeModelDeploymentType getType() {
        if (!metadata.labels.containsKey("mxe/deploymenttype")) {
            return MxeModelDeploymentType.UNKNOWN;
        }
        switch (metadata.labels.get("mxe/deploymenttype").toString().toLowerCase()) {
            case "mxe-single-model":
            case "model":
                return MxeModelDeploymentType.MODEL;
            case "static":
                return MxeModelDeploymentType.STATIC;
            case "dynamic":
                return MxeModelDeploymentType.DYNAMIC;
            default:
                log("Unknown SeldonDeployment type:");
                return MxeModelDeploymentType.UNKNOWN;
        }
    }

    private AutoscalingData getAutoscalingData() {
        HpaSpec hpaSpec = getPredictor().componentSpecs.get(0).hpaSpec;
        if (Objects.isNull(hpaSpec)) {
            return null;
        }
        AutoscalingData autoscalingData = new AutoscalingData();
        autoscalingData.minReplicas = hpaSpec.minReplicas;
        autoscalingData.maxReplicas = hpaSpec.maxReplicas;
        autoscalingData.metrics = getAutoscalingMetrics(hpaSpec);
        return autoscalingData;
    }

    private List<AutoscalingMetric> getAutoscalingMetrics(HpaSpec hpaSpec) {
        List<AutoscalingMetric> autoscalingMetrics = Lists.newArrayList();
        for (Metric metric : hpaSpec.metrics) {
            AutoscalingMetric autoscalingMetric = new AutoscalingMetric();
            autoscalingMetric.name = getAutoscalingMetricName(metric.resource.name);
            autoscalingMetric.targetAverageValue =
                    getAutoscalingMetricValue(autoscalingMetric.name, metric.resource.targetAverageValue);
            autoscalingMetrics.add(autoscalingMetric);
        }
        return autoscalingMetrics;
    }

    private AutoscalingMetricName getAutoscalingMetricName(String name) {
        return AutoscalingMetricName
                .getAutoscalingMetricNameByResourceName(AutoscalingMetricResourceName.valueOf(name));
    }

    static Integer getAutoscalingMetricValue(AutoscalingMetricName metricName, String targetAverageValue) {
        Pattern pattern = Pattern.compile("(\\d+)(.*)");
        Matcher matcher = pattern.matcher(targetAverageValue);
        if (!matcher.find()) {
            throw new RuntimeException();
        }
        Integer value = Integer.valueOf(matcher.group(1));
        String unit = matcher.group(2);
        switch (metricName) {
            case CPU_MILLI_CORES:
                value = getValueForCpu(value, unit);
                break;
            case MEMORY_MEGA_BYTES:
                value = getValueForMemory(value, unit);
                break;
            case UNKNOWN:
            default:
                throw new MxeInternalException(INVALID_METRIC_NAME_FOUND_IN_SELDON_DEPLOYMENT.formatted(metricName));
        }
        return value;
    }

    private static Integer getValueForCpu(Integer value, String unit) {
        switch (unit) {
            case "m":
                break;
            case "":
                value *= 1000;
                break;
            case "k":
                value *= 1000000;
                break;
            case "M":
                value *= 1000000000;
                break;
            default:
                throw new MxeInternalException(INVALID_UNIT_FOUND_IN_SELDON_DEPLOYMENT.formatted(unit));
        }
        return value;
    }

    private static Integer getValueForMemory(Integer value, String unit) {
        switch (unit) {
            case "Mi":
                break;
            default:
                throw new MxeInternalException(INVALID_UNIT_FOUND_IN_SELDON_DEPLOYMENT.formatted(unit));
        }
        return value;
    }

    private List<MxeModelInfo> getModels() {
        List<MxeModelInfo> res = new ArrayList<>();

        switch (getType()) {
            case MODEL:
                res.add(new MxeModelInfo(getImageFromComponent(0), getPredictor().graph.endpoint.type, null, null));
                break;
            case DYNAMIC:
            case STATIC:
                List<String> images = getImagesFromComponent();
                if (images.size() == 1) {
                    res.add(new MxeModelInfo(getImageFromComponent(0), getPredictor().graph.endpoint.type, null, null));
                    break;
                }
                List<String> graphChildrenEndpointTypes = getGraphChildrenEndpointTypes();
                Double weight0 = MxeParameterValidator.roundWeight(getRatioAForABTest());
                Double weight1 = MxeParameterValidator.roundWeight(weight0 == null ? null : 1 - weight0);
                res.add(new MxeModelInfo(images.get(0), graphChildrenEndpointTypes.get(0), null, weight0));
                res.add(new MxeModelInfo(images.get(1), graphChildrenEndpointTypes.get(1), null, weight1));
                break;
            default:
                // Error?
        }

        return res;
    }

    private Double getRatioAForABTest() {
        if ("RANDOM_ABTEST".equals(getPredictor().graph.implementation)) {
            if (getPredictor().graph.parameters == null)
                return null;
            for (DeploymentSpec.PredictorSpec.PredictiveUnit.Parameter param : getPredictor().graph.parameters) {
                if ("ratioA".equals(param.name)) {
                    try {
                        return Double.parseDouble(param.value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        log("Unable to get ratioA for AB test:");
        return null;
    }

    private void log(String message) {
        try {
            logger.error(message + "\n" + new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this));
        } catch (JsonProcessingException e) {
            logger.error(message + "\nCould not log JSON object", e);
        }
    }

    private String getImageFromComponent(int i) {
        String image = getPredictor().componentSpecs.get(i).spec.containers.get(0).image;
        return image != null ? image : "";
    }

    private List<String> getImagesFromComponent() {
        return getPredictor().componentSpecs.stream().map(componentsSpec -> componentsSpec.spec.containers.get(0).image)
                .map(image -> image != null ? image : "").collect(Collectors.toList());
    }

    private List<String> getGraphChildrenEndpointTypes() {
        return getPredictor().graph.children.stream().map(graphChildren -> graphChildren.endpoint.type)
                .collect(Collectors.toList());
    }

    private DeploymentSpec.PredictorSpec getPredictor() {
        return spec.predictors.get(0);
    }

    private MxeModelDeploymentStatus getStatus(String status) {
        // We assume that this will only happen directly after the resource was POST-ed,
        // before the resource manager added status
        if (status == null)
            return MxeModelDeploymentStatus.CREATING;

        switch (status) {
            case "Available":
                return MxeModelDeploymentStatus.RUNNING;
            case "Creating":
                return MxeModelDeploymentStatus.CREATING;
            case "Failed":
                return MxeModelDeploymentStatus.ERROR;
            default:
                return MxeModelDeploymentStatus.UNKNOWN;

        }
    }
}
