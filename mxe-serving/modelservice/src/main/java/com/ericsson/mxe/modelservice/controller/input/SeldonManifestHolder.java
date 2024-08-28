package com.ericsson.mxe.modelservice.controller.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ericsson.mxe.backendservicescommon.exception.MxeBadRequestException;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.modelservice.MxeParameterValidator;
import com.ericsson.mxe.modelservice.config.properties.DockerProperties;
import com.ericsson.mxe.modelservice.config.properties.ServiceMeshProperties;
import com.ericsson.mxe.modelservice.deployer.domain.MxeDeployerModelInfo;
import com.ericsson.mxe.modelservice.deployer.domain.MxeDeployerRequestData;
import com.ericsson.mxe.modelservice.dto.AutoscalingData;
import com.ericsson.mxe.modelservice.dto.AutoscalingMetric;
import com.ericsson.mxe.modelservice.dto.AutoscalingMetricName;
import com.ericsson.mxe.modelservice.dto.AutoscalingMetricResourceName;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentStatus;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentType;
import com.ericsson.mxe.modelservice.modelcatalog.ModelCatalogResolver;
import com.ericsson.mxe.modelservice.modelcatalog.dto.ModelPackageData;


public class SeldonManifestHolder {
    private static final Logger logger = LogManager.getLogger(SeldonManifestHolder.class);

    static final String INVALID_UNIT_FOUND_IN_SELDON_DEPLOYMENT = "Invalid unit found in seldon deployment: %s";
    static final String INVALID_METRIC_NAME_FOUND_IN_SELDON_DEPLOYMENT =
            "Invalid metric name found in seldon deployment: %s";

    private LinkedHashMap<String, Object> jsonObject;

    public SeldonManifestHolder(LinkedHashMap<String, Object> jsonObject) {
        this.jsonObject = jsonObject;
    }

    public Map<String, Object> getJsonObject() {
        return jsonObject;
    }

    // Patch operation - dry run of manifest requires resourceVersion
    public void addResourceVersion(String resourceVersion) {
        Optional.ofNullable((Map) jsonObject.get("metadata")).ifPresent(meta -> {
            meta.put("resourceVersion", resourceVersion);
        });
    }

    public void removeResourceVersion() {
        Optional<Map> metadata = Optional.ofNullable((Map) jsonObject.get("metadata"));
        if (metadata.isPresent()) {
            metadata.get().remove("resourceVersion");
        }
    }

    // Convert to Deployer data
    public MxeDeployerRequestData toMxeDeployerRequestData() {
        // validate();
        MxeDeployerRequestData deployment = new MxeDeployerRequestData();

        deployment.setDomain(getDeploymentDomain());
        deployment.setName(getDeploymentName());
        deployment.setType(getType());
        deployment.setCreated(getCreationTimeStamp());
        deployment.setStatus(getStatus());
        deployment.setMessage(getStatusDescription());
        deployment.setReplicas(getReplicas());
        deployment.setAutoScaling(getAutoscalingData());
        deployment.setModels(getModels());
        deployment.setWeights(null);// TO_DO: Static-ABTest
        deployment.setCreatedByUserId(getUserId());
        deployment.setCreatedByUserName(getUserName());

        return deployment;
    }

    // Prepopulate the mxe default spec into manifest (Overrides only if not present)
    public void enrichDeploymentSpec(DockerProperties dockerProperties, ServiceMeshProperties serviceMeshProperties) {
        String deploymentName = getDeploymentName();
        String namespace = getNamespace();

        Map<String, Object> spec = (Map) this.getJsonObject().get("spec");
        spec.put("name", deploymentName);


        Map<String, Object> annotations =
                Optional.ofNullable((Map<String, Object>) spec.get("annotations")).orElse(new HashMap<>());
        annotations.putIfAbsent("deployment_version", "v1");
        annotations.putIfAbsent("project_name", deploymentName);
        annotations.putIfAbsent("seldon.io/rest-read-timeout", "0");
        spec.put("annotations", annotations);

        Optional.of((List<Map>) spec.get("predictors")).ifPresent(predictors -> {
            predictors.forEach(updatePredictorSpec(dockerProperties, serviceMeshProperties));
        });
    }

    public void enrichContainerImageFromCatalog(ModelCatalogResolver resolver) {

        getJsonElementPredictorComponentSpecs().ifPresent(componentSpecs -> {
            componentSpecs.forEach(componentSpec -> {
                Optional.ofNullable((Map) componentSpec.get("spec"))
                        .map(podSpec -> (List<Map>) podSpec.get("containers")).ifPresent(containers -> {
                            containers.stream().filter(container -> {
                                String imgName = (String) container.get("name");
                                return !mxeInternalModelImage(imgName);
                            }).forEach(container -> {
                                String image = Optional.ofNullable((String) container.get("image")).orElse(null);
                                Pair<String, String> modelPair = getModelIdVersionPair(image, resolver);
                                String modelId = modelPair.getKey();
                                String modelVersion = modelPair.getValue();

                                resolver.getImageForPackage(modelId, modelVersion).ifPresentOrElse(dockerImage -> {
                                    container.put("image", dockerImage.getTag());
                                    // REVISIT for - imagepullSecrets for external model package data
                                }, () -> {
                                    throw new MxeBadRequestException(
                                            "Could not perform operation for model service, Model with name \"%s\" is not onboarded"
                                                    .formatted(image));
                                });

                            });
                        });
            });
        });

    }

    private Pair<String, String> getModelIdVersionPair(String containerImage, ModelCatalogResolver resolver) {
        if (Optional.ofNullable(containerImage).isEmpty()) {
            throw new MxeBadRequestException("Invalid Manifest file: Container image is not present");
        }

        int lastIdx = containerImage.lastIndexOf("/");
        if (lastIdx > -1) {
            logger.debug("The image full path is passed in the manifest yaml - tag '{}'", containerImage);
            final ModelPackageData modelPackageData = resolver.getPackageForImage(containerImage).orElseThrow(() -> {
                throw new MxeBadRequestException(
                        "Could not perform operation for model service, Model with image \"%s\" is not onboarded"
                                .formatted(containerImage));
            });
            return Pair.of(modelPackageData.id, modelPackageData.version);
        } else {
            logger.debug("The model id, model version - '{}' is passed in the manifest yaml", containerImage);
            String[] modelIdVersion = containerImage.split(":");
            if (modelIdVersion.length == 2) {
                return Pair.of(modelIdVersion[0], modelIdVersion[1]);
            } else {
                throw new MxeBadRequestException(
                        "Could not perform operation for model service, Model with name \"%s\" is not onboarded"
                                .formatted(containerImage));
            }
        }
    }

    public String getApiVersion() {
        return Optional.ofNullable(this.getJsonObject().get("apiVersion")).map(Object::toString).orElse(null);
    }

    public String getKind() {
        return Optional.ofNullable(this.getJsonObject().get("kind")).map(Object::toString).orElse(null);
    }

    public String getDeploymentName() {
        return Optional.ofNullable((Map) this.getJsonObject().get("metadata")).map(meta -> meta.get("name"))
                .map(Object::toString).orElse(null);
    }

    public String getDeploymentType() {
        Integer modelCount = getModels().size();
        switch (modelCount) {
            case 1:
                return MxeModelDeploymentType.MODEL.name().toLowerCase();
            case 2:
                return MxeModelDeploymentType.STATIC.name().toLowerCase();
            default:
                logger.info("Unknown SeldonDeployment type: ");
                return MxeModelDeploymentType.UNKNOWN.name().toLowerCase();
        }
    }

    private String getDeploymentDomain() {
        return Optional.ofNullable((Map) this.getJsonObject().get("metadata")).map(meta -> (Map) meta.get("labels"))
                .map(meta -> (String) meta.get("mxe/domain")).map(Object::toString).orElse("");
    }

    private String getCreationTimeStamp() {
        return Optional.ofNullable((Map) this.getJsonObject().get("metadata"))
                .map(meta -> meta.get("creationTimestamp")).map(Object::toString).orElse(null);
    }


    private MxeModelDeploymentType getType() {
        return Optional.ofNullable((Map) this.getJsonObject().get("metadata")).map(meta -> (Map) meta.get("labels"))
                .map(meta -> (String) meta.get("mxe/deploymenttype")).map(type -> {
                    switch (type.toLowerCase()) {
                        case "mxe-single-model":
                        case "model":
                            return MxeModelDeploymentType.MODEL;
                        case "static":
                            return MxeModelDeploymentType.STATIC;
                        case "dynamic":
                            return MxeModelDeploymentType.DYNAMIC;
                        default:
                            logger.info("Unknown SeldonDeployment type:");
                            return MxeModelDeploymentType.UNKNOWN;
                    }
                }).orElse(MxeModelDeploymentType.UNKNOWN);
    }

    private String getStatusDescription() {
        return Optional.ofNullable((Map) this.getJsonObject().get("status"))
                .map(statusElement -> (String) statusElement.get("description")).orElse(null);
    }

    private MxeModelDeploymentStatus getStatus() {
        return Optional.ofNullable((Map) this.getJsonObject().get("status"))
                .map(statusElement -> (String) statusElement.get("state"))
                .map(SeldonManifestHolder::getMxeDeploymentStatus).orElse(MxeModelDeploymentStatus.CREATING);
    }

    private static MxeModelDeploymentStatus getMxeDeploymentStatus(String status) {

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

    private String getUserId() {
        return Optional.ofNullable((Map) this.getJsonObject().get("metadata")).map(meta -> (Map) meta.get("labels"))
                .map(meta -> (String) meta.get("mxe/createdbyuserid")).map(Object::toString).orElse(null);
    }

    private String getUserName() {
        return Optional.ofNullable((Map) this.getJsonObject().get("metadata")).map(meta -> (Map) meta.get("labels"))
                .map(meta -> (String) meta.get("mxe/createdbyusername")).map(Object::toString).orElse(null);
    }

    private String getNamespace() {
        return Optional.ofNullable((Map) this.getJsonObject().get("metadata")).map(meta -> meta.get("namespace"))
                .map(Object::toString).orElse(null);
    }

    private AutoscalingData getAutoscalingData() {
        Map hpaSpec = getHpaSpec(0);
        if (Objects.isNull(hpaSpec)) {
            return null;
        }
        AutoscalingData autoscalingData = new AutoscalingData();
        autoscalingData.minReplicas = (Integer) hpaSpec.get("minReplicas");
        autoscalingData.maxReplicas = (Integer) hpaSpec.get("maxReplicas");
        autoscalingData.metrics = getAutoscalingMetrics(hpaSpec);
        return autoscalingData;
    }

    private List<AutoscalingMetric> getAutoscalingMetrics(Map hpaSpec) {
        if (!hpaSpec.containsKey("metrics")) {
            return null;
        } else {
            List<Map> metrics = (List<Map>) hpaSpec.get("metrics");
            return metrics.stream().map(metric -> {
                AutoscalingMetric autoscalingMetric = new AutoscalingMetric();
                String resourceName = Optional.ofNullable((Map) metric.get("resource"))
                        .map(resource -> (String) resource.get("name")).orElse("");
                String targetAverageValue = Optional.ofNullable((Map) metric.get("resource"))
                        .map(resource -> (String) resource.get("targetAverageValue")).orElse("");
                autoscalingMetric.name = getAutoscalingMetricName(resourceName);
                autoscalingMetric.targetAverageValue =
                        getAutoscalingMetricValue(autoscalingMetric.name, targetAverageValue);

                return autoscalingMetric;
            }).collect(Collectors.toList());
        }

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

    // Json path
    private Optional<Map<String, Object>> getPredictor() {
        return getJsonElementSpecPredictorByIdx(0);
    }

    private Map<String, Object> getHpaSpec(int compIdx) {
        return getJsonElementPredictorComponentSpecsByIdx(compIdx).map(compSpec -> (Map) compSpec.get("hpaSpec"))
                .orElse(null);
    }

    private Integer getReplicas() {
        Optional<Integer> inputReplicas = getPredictor().map(predictor -> (Integer) predictor.get("replicas"));
        return inputReplicas.isPresent() ? inputReplicas.get() : 1;
    }

    private Optional<Map<String, Object>> getJsonElementSpec() {
        return Optional.ofNullable((Map) this.getJsonObject().get("spec"));
    }

    private Optional<List> getJsonElementSpecPredictors() {
        return getJsonElementSpec().map(spec -> (List<Map>) spec.get("predictors"));
    }

    private Optional<Map<String, Object>> getJsonElementSpecPredictorByIdx(int idx) {
        return getJsonElementSpecPredictors().map(predictors -> (Map) predictors.get(idx));
    }

    private Optional<List<Map>> getJsonElementPredictorComponentSpecs() {
        return getPredictor().map(predictor -> (List<Map>) predictor.get("componentSpecs"));
    }

    private Optional<Map<String, Object>> getJsonElementPredictorComponentSpecsByIdx(int compIdx) {
        return getJsonElementPredictorComponentSpecs().map(componentSpecList -> (Map) componentSpecList.get(compIdx));
    }

    private Optional<Map<String, Object>> getJsonElementPodSpec(int compIdx) {
        Optional<Map<String, Object>> componentSpec = getJsonElementPredictorComponentSpecsByIdx(compIdx);
        return componentSpec.map(compSpec -> (Map) compSpec.get("spec"));
    }

    private Optional<List<Map>> getJsonElementPodContainers(int compIdx) {
        return getJsonElementPodSpec(compIdx).map(podSpec -> (List<Map>) podSpec.get("containers"));
    }

    private Optional<Map<String, Object>> getJsonElementPredictorGraph() {
        return getPredictor().map(predictor -> (Map) predictor.get("graph"));
    }

    private String getGraphImplementation() {
        return getJsonElementPredictorGraph().map(predictor -> (String) predictor.get("implementation")).orElseThrow();
    }

    private String getModelGraphEndpointType() {
        return getJsonElementPredictorGraph().map(predictor -> (Map) predictor.get("endpoint"))
                .map(endpoint -> (String) endpoint.get("type")).orElseThrow();
    }

    private List<String> getGraphChildrenEndpointTypes() {
        List<String> endpointTypes = new ArrayList<>();

        getJsonElementPredictorGraph().map(predictor -> (List<Map>) predictor.get("children"))
                .ifPresentOrElse(childrens -> {
                    childrens.forEach(children -> {
                        String epType = Optional.ofNullable((Map) children.get("endpoint"))
                                .map(endpoint -> (String) endpoint.get("type")).orElseThrow();// Error Invalid manifest
                                                                                              // - for static
                        endpointTypes.add(epType);
                    });
                }, () -> {
                    // TO_DO: Error Invalid manifest - for static
                });

        return endpointTypes;
    }

    private List<String> getImages() {
        List<String> imageList = new ArrayList<>();
        getJsonElementPredictorComponentSpecs().ifPresent(componentSpecs -> {
            componentSpecs.forEach(componentSpec -> {
                Optional.ofNullable((Map) componentSpec.get("spec"))
                        .map(podSpec -> (List<Map>) podSpec.get("containers")).ifPresent(containers -> {
                            containers.stream().filter(container -> {
                                String imgName = (String) container.get("name");
                                return !mxeInternalModelImage(imgName);
                            }).map(container -> (String) container.get("image")).forEach(image -> imageList.add(image));
                        });

            });
        });
        return imageList;
    }

    private boolean mxeInternalModelImage(String containerName) {
        return containerName.equals("input-transformer") || containerName.equals("output-transformer");
    }

    private Double getRatioAForABTest() {
        List<Double> rationA = new ArrayList<>();

        getJsonElementPredictorGraph().ifPresentOrElse(graph -> {
            if ("RANDOM_ABTEST".equals(graph.get("implementation"))) {
                Optional.ofNullable((List<Map>) graph.get("parameters")).ifPresent(parameters -> {
                    parameters.forEach(p -> {
                        if ("ratioA".equals(p.get("name"))) {
                            try {
                                rationA.add(Double.parseDouble((String) p.get("value")));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                });
            }
        }, () -> {
            logger.info("Unable to get ratioA for AB test:");

        });
        return !rationA.isEmpty() ? rationA.get(0) : null;

    }

    private List<MxeDeployerModelInfo> getModels() {
        List<MxeDeployerModelInfo> modelInfoList = new ArrayList<>();

        List<String> imageList = getImages();
        String graphImplementation = getGraphImplementation();

        if (imageList.size() == 1) {
            if ("RANDOM_ABTEST".equals(graphImplementation)) {
                throw new MxeBadRequestException("Invalid manifest: model type 'static' should have 2 model images");
            }
            String graphEndPoint = getModelGraphEndpointType();
            modelInfoList.add(new MxeDeployerModelInfo(imageList.get(0), graphEndPoint, null, null));
        } else if (imageList.size() == 2) {
            if (!"RANDOM_ABTEST".equals(graphImplementation)) {
                throw new MxeBadRequestException("Invalid manifest: single model should have only 1 model image");
            }
            List<String> graphChildrenEndpointTypes = getGraphChildrenEndpointTypes();

            Double weight0 = MxeParameterValidator.roundWeight(getRatioAForABTest());
            if (weight0 < 0 || weight0 > 1) {
                throw new MxeBadRequestException(
                        "Value of parameter 'weight' is not between 0 and 1: '%s'.".formatted(weight0));
            }
            Double weight1 = MxeParameterValidator.roundWeight(weight0 == null ? null : 1 - weight0);
            modelInfoList
                    .add(new MxeDeployerModelInfo(imageList.get(0), graphChildrenEndpointTypes.get(0), null, weight0));
            modelInfoList
                    .add(new MxeDeployerModelInfo(imageList.get(1), graphChildrenEndpointTypes.get(1), null, weight1));
        }

        return modelInfoList;

    }

    private void updatePredictorAnnotations(Map<String, Object> predictor) {
        Optional.of((Map<String, String>) predictor.get("annotations")).ifPresentOrElse(annotation -> {
            annotation.put("sidecar.istio.io/inject", "true");
            annotation.put("sidecar.istio.io/rewriteAppHTTPProbers", "true");
        }, () -> {
            Map<String, String> annotation = new HashMap<>();
            annotation.put("sidecar.istio.io/inject", "true");
            annotation.put("sidecar.istio.io/rewriteAppHTTPProbers", "true");
            predictor.put("annotations", annotation);
        });
    }


    private Consumer<Map> updatePredictorSpec(DockerProperties dockerProperties,
            ServiceMeshProperties serviceMeshProperties) {
        return predictor -> {
            updatePredictorMetadata(predictor, dockerProperties);
            if (serviceMeshProperties.isMtlsEnabled()) {
                updatePredictorAnnotations(predictor);
            }
        };
    }

    private void updatePredictorMetadata(Map<String, Object> predictor, DockerProperties dockerProperties) {
        Optional.of((List<Map>) predictor.get("componentSpecs")).ifPresent(componentSpec -> {
            componentSpec.forEach(updateComponentSpec(dockerProperties));
        });
    }

    private Consumer<Map> updateComponentSpec(DockerProperties dockerProperties) {
        return componentSpec -> {
            updateComponentSpecMetadata(componentSpec);
            componentSpec.put("spec", updateComponentSpecPodSpec(componentSpec, dockerProperties));
        };
    }

    private void updateComponentSpecMetadata(Map<String, Object> componentSpec) {
        Map<String, Object> metadata =
                Optional.ofNullable((Map<String, Object>) componentSpec.get("metadata")).orElse(new HashMap<>());
        Map<String, Object> labels =
                Optional.ofNullable((Map<String, Object>) metadata.get("labels")).orElse(new HashMap<>());
        labels.put("app.kubernetes.io/part-of", "mxe");
        labels.put("app.kubernetes.io/component", "model-service-instance");
        labels.put("app.kubernetes.io/instance", getDeploymentName());


        labels.put("mxe.ericsson.com/model-service-component", getContainerName(componentSpec));
        metadata.put("labels", labels);
        componentSpec.put("metadata", metadata);
    }

    private String getContainerName(Map<String, Object> componentSpec) {
        List<String> containerNameList = new ArrayList<>();
        Optional.ofNullable((Map<String, Object>) componentSpec.get("spec"))
                .map(spec -> (List<Map>) spec.get("containers")).ifPresent(containers -> {
                    containers.stream().map(container -> (String) container.get("name"))
                            .filter(modelName -> "model".equals(modelName)).findFirst().ifPresent(modelName -> {
                                containerNameList.add(modelName);
                            });

                });


        return !containerNameList.isEmpty() ? containerNameList.get(0) : "model";
    }

    private Map<String, Object> updateComponentSpecPodSpec(Map<String, Object> componentSpec,
            DockerProperties dockerProperties) {
        Map<String, Object> podSpec =
                Optional.ofNullable((Map<String, Object>) componentSpec.get("spec")).orElse(new HashMap<>());

        updatePodSpecTerminationPeriodSeconds(podSpec);
        updatePodSpecImagePullSecrets(podSpec, dockerProperties);
        updatePodSpecContainerProbes(podSpec);
        updatePodSpecAffinitySpec(podSpec);
        return podSpec;
    }

    private void updatePodSpecTerminationPeriodSeconds(Map<String, Object> podSpec) {
        podSpec.putIfAbsent("terminationGracePeriodSeconds", 1);
    }

    private void updatePodSpecImagePullSecrets(Map<String, Object> podSpec, DockerProperties dockerProperties) {
        List<Map> imagePullSecrets =
                Optional.ofNullable((List) podSpec.get("imagePullSecrets")).orElse(new ArrayList<>());
        if (!imagePullSecrets.isEmpty()) {
            logger.debug("ImagePullSecrets configuration exists in the manifest and will be maintained.");
        } else {
            logger.debug(
                    "ImagePullSecrets configuration not exists in the manifest. Default mxe configurations are added.");

            Map<String, String> msSecretMap = new HashMap<>();
            msSecretMap.put("name", dockerProperties.getRegistrySecretName());
            imagePullSecrets.add(msSecretMap);
            podSpec.put("imagePullSecrets", imagePullSecrets);
        }
    }

    private void updatePodSpecContainerProbes(Map<String, Object> podSpec) {
        Optional.of((List<Map>) podSpec.get("containers")).ifPresent(containerSpecs -> {
            containerSpecs.forEach(containerSpec -> {
                Optional.ofNullable(containerSpec.get("livenessProbe")).ifPresentOrElse((livenessProbe) -> {
                    logger.debug("LivenessProbe configuration exists in the manifest and will be maintained");
                }, () -> {
                    logger.debug(
                            "LivenessProbe configuration not exists in the manifest. Default mxe configurations are added.");
                    containerSpec.put("livenessProbe", getMxeDefaultLivenessReadinessSpec());
                });
                Optional.ofNullable(containerSpec.get("readinessProbe")).ifPresentOrElse((livenessProbe) -> {
                    logger.debug("ReadinessProbe configuration exists in the manifest and will be maintained");
                }, () -> {
                    logger.debug(
                            "ReadinessProbe configuration not exists in the manifest. Default mxe configurations are added.");
                    containerSpec.put("readinessProbe", getMxeDefaultLivenessReadinessSpec());
                });
            });
        });
    }

    private Map<String, Object> getMxeDefaultLivenessReadinessSpec() {
        Map<String, Object> liveReadySpec = new LinkedHashMap<>();
        liveReadySpec.put("failureThreshold", 6);
        liveReadySpec.put("initialDelaySeconds", 10);
        liveReadySpec.put("periodSeconds", 10);
        liveReadySpec.put("successThreshold", 1);
        liveReadySpec.put("timeoutSeconds", 1);
        liveReadySpec.put("tcpSocket", Map.of("port", "http"));

        return liveReadySpec;
    }

    private void updatePodSpecAffinitySpec(Map<String, Object> podSpec) {
        Optional.ofNullable((Map<String, Object>) podSpec.get("affinity")).ifPresentOrElse((affinity) -> {
            logger.debug("Pod Affinity configuration exists in the manifest and will be maintained");
        }, () -> {
            logger.debug(
                    "Pod Affinity configuration not exists in the manifest. Default mxe configurations are added.");
            podSpec.put("affinity", getMxeDefaultAffinitySpec());
        });

    }

    private Map<String, Object> getMxeDefaultAffinitySpec() {
        String topologyKey = "kubernetes.io/hostname";
        String appPartOfLabel = "app.kubernetes.io/part-of";
        String appComponentLabel = "app.kubernetes.io/component";
        String appInstanceLabel = "app.kubernetes.io/instance";
        String inOperator = "In";
        String notInOperator = "NotIn";

        Map<String, Object> affinity = new LinkedHashMap<>();
        Map<String, Object> antiAffinitySpec = new LinkedHashMap<>();
        List<Map<String, Object>> scheduleSpecList = new ArrayList<>();

        // 1. AntiAffinity: Wt 75, labels - partof(mxe), component(model-service-instance), instance(deployName)
        Map<String, Object> scheduleSpec1 = new LinkedHashMap<>();
        scheduleSpec1.put("weight", 75);

        Map<String, Object> podAffinityTerm1 = new LinkedHashMap<>();
        podAffinityTerm1.put("topologyKey", topologyKey);
        Map<String, Object> labelSelectorSpec1 = new LinkedHashMap<>();
        List<Map<String, Object>> matchSpecList1 = new LinkedList<>();
        matchSpecList1.add(labelMatchExpr(appPartOfLabel, inOperator, Arrays.asList("mxe")));
        matchSpecList1.add(labelMatchExpr(appComponentLabel, inOperator, Arrays.asList("model-service-instance")));
        matchSpecList1.add(labelMatchExpr(appInstanceLabel, inOperator, Arrays.asList(getDeploymentName())));

        labelSelectorSpec1.put("matchExpressions", matchSpecList1);
        podAffinityTerm1.put("labelSelector", labelSelectorSpec1);
        scheduleSpec1.put("podAffinityTerm", podAffinityTerm1);
        scheduleSpecList.add(scheduleSpec1);

        // 2. AntiAffinity: Wt 25, labels - partof(mxe), component(model-service-instance), instance(deployName -NotIN)
        Map<String, Object> scheduleSpec2 = new LinkedHashMap<>();
        scheduleSpec2.put("weight", 25);

        Map<String, Object> podAffinityTerm2 = new LinkedHashMap<>();
        podAffinityTerm2.put("topologyKey", topologyKey);
        Map<String, Object> labelSelectorSpec2 = new LinkedHashMap<>();
        List<Map<String, Object>> matchSpecList2 = new LinkedList<>();
        matchSpecList2.add(labelMatchExpr(appPartOfLabel, inOperator, Arrays.asList("mxe")));
        matchSpecList2.add(labelMatchExpr(appComponentLabel, inOperator, Arrays.asList("model-service-instance")));
        matchSpecList2.add(labelMatchExpr(appInstanceLabel, notInOperator, Arrays.asList(getDeploymentName())));

        labelSelectorSpec2.put("matchExpressions", matchSpecList2);
        podAffinityTerm2.put("labelSelector", labelSelectorSpec2);
        scheduleSpec2.put("podAffinityTerm", podAffinityTerm2);
        scheduleSpecList.add(scheduleSpec2);

        // 3. AntiAffinity: Wt 50, labels - partof(mxe), component(trainer)
        Map<String, Object> scheduleSpec3 = new LinkedHashMap<>();
        scheduleSpec3.put("weight", 50);

        Map<String, Object> podAffinityTerm3 = new LinkedHashMap<>();
        podAffinityTerm3.put("topologyKey", topologyKey);
        Map<String, Object> labelSelectorSpec3 = new LinkedHashMap<>();
        List<Map<String, Object>> matchSpecList3 = new LinkedList<>();
        matchSpecList3.add(labelMatchExpr(appPartOfLabel, inOperator, Arrays.asList("mxe")));
        matchSpecList3.add(labelMatchExpr(appComponentLabel, inOperator, Arrays.asList("trainer")));

        labelSelectorSpec3.put("matchExpressions", matchSpecList3);
        podAffinityTerm3.put("labelSelector", labelSelectorSpec3);
        scheduleSpec3.put("podAffinityTerm", podAffinityTerm3);
        scheduleSpecList.add(scheduleSpec3);

        // 4. AntiAffinity: Wt 50, labels - partof(mxe), component(packager)
        Map<String, Object> scheduleSpec4 = new LinkedHashMap<>();
        scheduleSpec4.put("weight", 50);

        Map<String, Object> podAffinityTerm4 = new LinkedHashMap<>();
        podAffinityTerm4.put("topologyKey", topologyKey);
        Map<String, Object> labelSelectorSpec4 = new LinkedHashMap<>();
        List<Map<String, Object>> matchSpecList4 = new LinkedList<>();
        matchSpecList4.add(labelMatchExpr(appPartOfLabel, inOperator, Arrays.asList("mxe")));
        matchSpecList4.add(labelMatchExpr(appComponentLabel, inOperator, Arrays.asList("packager")));

        labelSelectorSpec4.put("matchExpressions", matchSpecList4);
        podAffinityTerm4.put("labelSelector", labelSelectorSpec4);
        scheduleSpec4.put("podAffinityTerm", podAffinityTerm4);
        scheduleSpecList.add(scheduleSpec4);

        // 5. AntiAffinity: Wt 10, labels - partof(mxe), component(gatekeeper)
        Map<String, Object> scheduleSpec5 = new LinkedHashMap<>();
        scheduleSpec5.put("weight", 10);

        Map<String, Object> podAffinityTerm5 = new LinkedHashMap<>();
        podAffinityTerm5.put("topologyKey", topologyKey);
        Map<String, Object> labelSelectorSpec5 = new LinkedHashMap<>();
        List<Map<String, Object>> matchSpecList5 = new LinkedList<>();
        matchSpecList5.add(labelMatchExpr(appPartOfLabel, inOperator, Arrays.asList("mxe")));
        matchSpecList5.add(labelMatchExpr(appComponentLabel, inOperator, Arrays.asList("gatekeeper")));

        labelSelectorSpec5.put("matchExpressions", matchSpecList5);
        podAffinityTerm5.put("labelSelector", labelSelectorSpec5);
        scheduleSpec5.put("podAffinityTerm", podAffinityTerm5);
        scheduleSpecList.add(scheduleSpec5);

        // 6. AntiAffinity: Wt 10, labels - partof(mxe), component(ingress-controller)
        Map<String, Object> scheduleSpec6 = new LinkedHashMap<>();
        scheduleSpec6.put("weight", 10);

        Map<String, Object> podAffinityTerm6 = new LinkedHashMap<>();
        podAffinityTerm6.put("topologyKey", topologyKey);
        Map<String, Object> labelSelectorSpec6 = new LinkedHashMap<>();
        List<Map<String, Object>> matchSpecList6 = new LinkedList<>();
        matchSpecList6.add(labelMatchExpr(appPartOfLabel, inOperator, Arrays.asList("mxe")));
        matchSpecList6.add(labelMatchExpr(appComponentLabel, inOperator, Arrays.asList("ingress-controller")));

        labelSelectorSpec6.put("matchExpressions", matchSpecList6);
        podAffinityTerm6.put("labelSelector", labelSelectorSpec6);
        scheduleSpec6.put("podAffinityTerm", podAffinityTerm6);
        scheduleSpecList.add(scheduleSpec6);

        // 7. AntiAffinity: Wt 5, labels - app(nginx-ingress),
        Map<String, Object> scheduleSpec7 = new LinkedHashMap<>();
        scheduleSpec7.put("weight", 5);

        Map<String, Object> podAffinityTerm7 = new LinkedHashMap<>();
        podAffinityTerm7.put("topologyKey", topologyKey);
        Map<String, Object> labelSelectorSpec7 = new LinkedHashMap<>();
        List<Map<String, Object>> matchSpecList7 = new LinkedList<>();
        matchSpecList7.add(labelMatchExpr("app", inOperator, Arrays.asList("nginx-ingress")));

        labelSelectorSpec7.put("matchExpressions", matchSpecList7);
        podAffinityTerm7.put("labelSelector", labelSelectorSpec7);
        scheduleSpec7.put("podAffinityTerm", podAffinityTerm7);
        scheduleSpecList.add(scheduleSpec7);

        // Set the pod AntiAffinity specs
        antiAffinitySpec.put("preferredDuringSchedulingIgnoredDuringExecution", scheduleSpecList);
        affinity.put("podAntiAffinity", antiAffinitySpec);
        return affinity;
    }

    private Map<String, Object> labelMatchExpr(String key, String operator, List<String> values) {
        Map<String, Object> matchExprSpec = new LinkedHashMap<>();
        matchExprSpec.put("key", key);
        matchExprSpec.put("operator", operator);
        matchExprSpec.put("values", values);
        return matchExprSpec;
    }
}
