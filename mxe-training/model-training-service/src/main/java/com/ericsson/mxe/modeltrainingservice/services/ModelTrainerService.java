package com.ericsson.mxe.modeltrainingservice.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.modeltrainingservice.config.properties.DockerProperties;
import com.ericsson.mxe.modeltrainingservice.config.properties.ModelTrainerProperties;
import com.ericsson.mxe.modeltrainingservice.config.properties.ModelTrainingServiceProperties;
import com.ericsson.mxe.modeltrainingservice.config.properties.PypiServiceConfig;
import com.ericsson.mxe.modeltrainingservice.config.properties.ServiceMeshProperties;
import com.ericsson.mxe.modeltrainingservice.services.minio.TrainingJobResultRepositoryService;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Affinity;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1EnvVarSource;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobSpec;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1LabelSelectorRequirement;
import io.kubernetes.client.openapi.models.V1LocalObjectReference;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodAffinityTerm;
import io.kubernetes.client.openapi.models.V1PodAntiAffinity;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1SecretKeySelector;
import io.kubernetes.client.openapi.models.V1SecretVolumeSource;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import io.kubernetes.client.openapi.models.V1WeightedPodAffinityTerm;

@Service
public class ModelTrainerService {
    private static final Logger logger = LogManager.getLogger(ModelTrainerService.class);
    private static final String MXE_TRAINER_NAME_PREFIX = "mxe-trainer-";
    private static final String PULL_SECRET_VOLUME_NAME = "trainer-pull-secret";
    private static final String MINIO_URL_ENV_VAR = "MINIO_URL";
    private static final String MINIO_ACCESS_KEY_ENV_VAR = "MINIO_ACCESS_KEY";
    private static final String MINIO_SECRET_KEY_ENV_VAR = "MINIO_SECRET_KEY";
    private static final String MINIO_BUCKET_ENV_VAR = "MINIO_BUCKET";
    private static final String MINIO_ACCESS_KEY_SECRET_KEY = "accesskey";
    private static final String MINIO_SECRET_KEY_SECRET_KEY = "secretkey";
    private static final String TRAINING_RESULT_FILENAME = "training_result.zip";
    private static final String AFFINITY_TOPOLOGY_KEY = "kubernetes.io/hostname";
    private static final int TRAINER_AFFINITY_WEIGHT = 75;
    private static final int PACKAGER_AFFINITY_WEIGHT = 75;
    private static final String GATEKEEPER_COMPONENT_NAME = "gatekeeper";
    private static final int GATEKEEPER_AFFINITY_WEIGHT = 40;
    private static final String INTERNAL_INGRESS_CONTROLLER_COMPONENT_NAME = "ingress-controller";
    private static final int INTERNAL_INGRESS_CONTROLLER_AFFINITY_WEIGHT = 30;
    private static final String EXTERNAL_INGRESS_CONTROLLER_LABEL_KEY = "app";
    private static final String EXTERNAL_INGRESS_CONTROLLER_LABEL_VALUE = "nginx-ingress";
    private static final int EXTERNAL_INGRESS_CONTROLLER_AFFINITY_WEIGHT = 10;
    private static final String AMBASSADOR_NAME_LABEL_VALUE = "ambassador";
    private static final int AMBASSADOR_AFFINITY_WEIGHT = 30;
    private static final String MODEL_COMPONENT_NAME = "model-service-instance";
    private static final int MODEL_AFFINITY_WEIGHT = 20;

    private final KubernetesService kubernetesService;
    private final ModelTrainerProperties modelTrainerProperties;
    private final ModelTrainingServiceProperties modelCatalogueServiceProperties;
    private final TrainingJobResultRepositoryService trainingJobResultRepositoryService;
    private final DockerProperties dockerProperties;
    private final PypiServiceConfig pypiServiceConfig;
    private final ServiceMeshProperties serviceMeshProperties;
    private final String namespace;

    public ModelTrainerService(final KubernetesService kubernetesService,
            final ModelTrainerProperties modelTrainerProperties,
            final ModelTrainingServiceProperties modelCatalogueServiceProperties,
            final TrainingJobResultRepositoryService trainingJobResultRepositoryService,
            final DockerProperties dockerProperties, PypiServiceConfig pypiServiceConfig,
            final ServiceMeshProperties serviceMeshProperties) {
        this.kubernetesService = kubernetesService;
        this.modelTrainerProperties = modelTrainerProperties;
        this.modelCatalogueServiceProperties = modelCatalogueServiceProperties;
        this.trainingJobResultRepositoryService = trainingJobResultRepositoryService;
        this.namespace = kubernetesService.getNamespace();
        this.dockerProperties = dockerProperties;
        this.pypiServiceConfig = pypiServiceConfig;
        this.serviceMeshProperties = serviceMeshProperties;
    }

    public void startJob(final String trainingImage, final String trainingJobId) throws ApiException {
        final V1Container container = this.getContainer(
                dockerProperties.getRegistry().getExternalHostname() + "/" + trainingImage, trainingJobId);

        final V1PodSpec podSpec = new V1PodSpec();
        podSpec.addContainersItem(container);
        podSpec.setRestartPolicy("OnFailure");
        podSpec.setAffinity(getAffinity());

        if (!ObjectUtils.isEmpty(dockerProperties.getRegistry().getExternalSecretName())) {
            final V1LocalObjectReference pullSecretName = new V1LocalObjectReference();
            pullSecretName.setName(dockerProperties.getRegistry().getExternalSecretName());

            podSpec.addImagePullSecretsItem(pullSecretName);

            final V1Volume pullSecretVolume = new V1Volume();
            pullSecretVolume.setName(PULL_SECRET_VOLUME_NAME);

            final V1SecretVolumeSource pullSecretVolumeSource = new V1SecretVolumeSource();
            pullSecretVolumeSource.setSecretName(dockerProperties.getRegistry().getExternalSecretName());
            pullSecretVolume.setSecret(pullSecretVolumeSource);

            podSpec.addVolumesItem(pullSecretVolume);

            final V1VolumeMount pullSecretVolumeMount = new V1VolumeMount();
            pullSecretVolumeMount.setName(PULL_SECRET_VOLUME_NAME);
            pullSecretVolumeMount.setMountPath("/mnt/docker");
            pullSecretVolumeMount.setReadOnly(true);

            container.addVolumeMountsItem(pullSecretVolumeMount);
        }

        final Map<String, String> jobLabels = this.getJobLabels(trainingJobId);

        final V1PodTemplateSpec template = new V1PodTemplateSpec();
        template.setSpec(podSpec);


        final V1ObjectMeta templateMetaData = new V1ObjectMeta();
        templateMetaData.setLabels(new HashMap<>(this.getPackagerPodLabels()));

        if (serviceMeshProperties.isMtlsEnabled()) {
            // To add sidecar annotations only if mtls is enabled in the mesh
            templateMetaData.setAnnotations(this.getTempateIstioAnnotations());
        }
        template.setMetadata(templateMetaData);

        final V1JobSpec jobSpec = new V1JobSpec();
        jobSpec.setCompletions(1);
        jobSpec.setParallelism(1);
        jobSpec.setTemplate(template);
        jobSpec.setActiveDeadlineSeconds(10800L);
        jobSpec.setBackoffLimit(5);

        final V1ObjectMeta deploymentMeta = new V1ObjectMeta();
        deploymentMeta.setName(this.getName(trainingJobId));
        deploymentMeta.setLabels(new HashMap<>(jobLabels));

        final V1Job job = new V1Job();
        job.setSpec(jobSpec);
        job.setMetadata(deploymentMeta);

        this.kubernetesService.createNamespacedJob(this.namespace, job);
    }

    private Map<String, String> getTempateIstioAnnotations() {
        final Map<String, String> annotations = new HashMap<String, String>();
        annotations.put("sidecar.istio.io/inject", "true");
        annotations.put("sidecar.istio.io/rewriteAppHTTPProbers", "true");
        annotations.put("sidecar.istio.io/userVolume", serviceMeshProperties.getUserVolume());
        annotations.put("sidecar.istio.io/userVolumeMount", serviceMeshProperties.getUserVolumeMounts());
        return annotations;
    }

    private Map<String, String> getJobLabels(final String trainingJobId) {
        final Map<String, String> labels = getSelectorLabels();
        this.kubernetesService.addLabel(labels, this.kubernetesService.getPackageNameLabelKey(), trainingJobId);
        this.kubernetesService.addLabel(labels, this.kubernetesService.getVersionLabelKey(),
                this.modelCatalogueServiceProperties.getVersion());
        this.kubernetesService.addLabel(labels, this.kubernetesService.getNameLabelKey(), this.getName(trainingJobId));
        this.kubernetesService.addLabel(labels, this.kubernetesService.getManagedByLabelKey(),
                this.modelCatalogueServiceProperties.getName());
        return labels;
    }

    private Map<String, String> getPackagerPodLabels() {
        final Map<String, String> labels = getSelectorLabels();
        this.kubernetesService.addLabel(labels, this.kubernetesService.getVersionLabelKey(),
                this.modelCatalogueServiceProperties.getVersion());
        this.kubernetesService.addLabel(labels, this.kubernetesService.getManagedByLabelKey(),
                this.modelCatalogueServiceProperties.getName());
        return labels;
    }

    private Map<String, String> getSelectorLabels() {
        return this.kubernetesService.getMandatoryJobLabels(this.kubernetesService.getTrainerComponentName(),
                this.modelCatalogueServiceProperties.getInstanceName());
    }

    private V1Container getContainer(final String trainingImage, final String trainingJobId) {
        final V1Container container = new V1Container();

        addEnv(container, getMinioUrlEnv());
        addEnv(container, getMinioBucketEnv());
        addEnv(container, getMinioAccessKeyEnv());
        addEnv(container, getMinioSecretKeyEnv());

        addEnv(container, "TRAINING_JOB_ID", trainingJobId);

        addEnv(container, "DOCKER_REGISTRY_HOSTNAME", dockerProperties.getRegistry().getHostname());
        addEnv(container, "MODEL_TRAINING_HOST", modelCatalogueServiceProperties.getName());

        addEnv(container, "PYPISERVICE_HOST", pypiServiceConfig.getHost());
        addEnv(container, "SERVICE_MESH_MTLS_ENABLED", String.valueOf(serviceMeshProperties.isMtlsEnabled()));

        container.setImage(trainingImage);
        container.setImagePullPolicy(modelTrainerProperties.getPullPolicy());
        container.setName(this.getName(trainingJobId));

        return container;
    }

    private void addEnv(final V1Container container, final String envName, final String envValue) {
        addEnv(container, getEnv(envName, envValue));
    }

    private void addEnv(final V1Container container, final V1EnvVar env) {
        container.addEnvItem(env);
    }

    private V1EnvVar getMinioUrlEnv() {
        return getEnv(MINIO_URL_ENV_VAR, trainingJobResultRepositoryService.getUrl().toString());
    }

    private V1EnvVar getMinioBucketEnv() {
        return getEnv(MINIO_BUCKET_ENV_VAR, trainingJobResultRepositoryService.getBucket());
    }

    private V1EnvVar getMinioAccessKeyEnv() {
        return getMinioSecretEnv(MINIO_ACCESS_KEY_ENV_VAR, MINIO_ACCESS_KEY_SECRET_KEY);
    }

    private V1EnvVar getMinioSecretKeyEnv() {
        return getMinioSecretEnv(MINIO_SECRET_KEY_ENV_VAR, MINIO_SECRET_KEY_SECRET_KEY);
    }

    private V1EnvVar getMinioSecretEnv(final String envName, final String secretKey) {
        final V1SecretKeySelector envSecretKeySelector = new V1SecretKeySelector();
        envSecretKeySelector.setName(trainingJobResultRepositoryService.getInstanceSecretName());
        envSecretKeySelector.setKey(secretKey);

        final V1EnvVarSource envVarSource = new V1EnvVarSource();
        envVarSource.setSecretKeyRef(envSecretKeySelector);

        final V1EnvVar hostEnv = new V1EnvVar();
        hostEnv.setName(envName);
        hostEnv.setValueFrom(envVarSource);

        return hostEnv;
    }

    private V1EnvVar getEnv(final String name, final String value) {
        final V1EnvVar hostEnv = new V1EnvVar();
        hostEnv.setName(name);
        hostEnv.setValue(value);

        return hostEnv;
    }

    private String getName(final String packageName) {
        final String escapedPackageName =
                packageName.toLowerCase().replaceAll("[^a-z0-9\\-]", "-").replaceAll("-*$", "");
        return MXE_TRAINER_NAME_PREFIX + escapedPackageName;
    }

    public void deleteJob(final String trainingJobId) {
        List<String> errorMessages = new ArrayList<>();

        try {
            trainingJobResultRepositoryService.removeObject(trainingJobId);
        } catch (Exception e) {
            e.printStackTrace();

            errorMessages.add("Failed to delete stored training results of job \"" + trainingJobId + "\"");
        }
        for (V1Job job : listNamespacedJobs(errorMessages)) {
            if (job.getMetadata().getName().contains(MXE_TRAINER_NAME_PREFIX + trainingJobId)) {
                try {
                    this.kubernetesService.deleteNamespacedJob(this.namespace, job.getMetadata().getName());
                } catch (ApiException e) {
                    errorMessages.add("Failed to delete job \"" + job.getMetadata().getName() + "\" for training job:"
                            + trainingJobId);
                }
            }
        }
        if (!errorMessages.isEmpty()) {
            throw new MxeInternalException(errorMessages.stream().collect(Collectors.joining(System.lineSeparator())));
        }
    }

    public List<V1Job> listNamespacedJobs(List<String> errorMessages) {
        try {
            return this.kubernetesService.listNamespacedJobs(this.namespace).getItems();
        } catch (ApiException e) {
            logger.error("Exception in ModelTrainerService during training job deletion", e);
            errorMessages.add("Failed to get jobs for namespace:" + this.namespace);
        }
        return new ArrayList<>();
    }

    private V1Affinity getAffinity() {
        final V1WeightedPodAffinityTerm trainerWeightedAffinity =
                getWeightedAffinity(getListLabels(), TRAINER_AFFINITY_WEIGHT);
        final V1WeightedPodAffinityTerm packagerWeightedAffinity =
                getWeightedAffinity(getPackagerLabels(), PACKAGER_AFFINITY_WEIGHT);
        final V1WeightedPodAffinityTerm gatekeeperWeightedAffinity =
                getWeightedAffinity(getGatekeeperLabels(), GATEKEEPER_AFFINITY_WEIGHT);
        final V1WeightedPodAffinityTerm internalIngressControllerWeightedAffinity =
                getWeightedAffinity(getInternalIngressControllerLabels(), INTERNAL_INGRESS_CONTROLLER_AFFINITY_WEIGHT);
        final V1WeightedPodAffinityTerm externalIngressControllerWeightedAffinity =
                getWeightedAffinity(getExternalIngressControllerLabels(), EXTERNAL_INGRESS_CONTROLLER_AFFINITY_WEIGHT);
        final V1WeightedPodAffinityTerm ambassadorWeightedAffinity =
                getWeightedAffinity(getAmbassadorLabels(), AMBASSADOR_AFFINITY_WEIGHT);
        final V1WeightedPodAffinityTerm modelWeightedAffinity =
                getWeightedAffinity(getModelLabels(), MODEL_AFFINITY_WEIGHT);

        final V1PodAntiAffinity podAntiAffinity = new V1PodAntiAffinity();
        podAntiAffinity.addPreferredDuringSchedulingIgnoredDuringExecutionItem(trainerWeightedAffinity);
        podAntiAffinity.addPreferredDuringSchedulingIgnoredDuringExecutionItem(packagerWeightedAffinity);
        podAntiAffinity.addPreferredDuringSchedulingIgnoredDuringExecutionItem(gatekeeperWeightedAffinity);
        podAntiAffinity
                .addPreferredDuringSchedulingIgnoredDuringExecutionItem(internalIngressControllerWeightedAffinity);
        podAntiAffinity
                .addPreferredDuringSchedulingIgnoredDuringExecutionItem(externalIngressControllerWeightedAffinity);
        podAntiAffinity.addPreferredDuringSchedulingIgnoredDuringExecutionItem(ambassadorWeightedAffinity);
        podAntiAffinity.addPreferredDuringSchedulingIgnoredDuringExecutionItem(modelWeightedAffinity);

        final V1Affinity affinity = new V1Affinity();
        affinity.setPodAntiAffinity(podAntiAffinity);

        return affinity;
    }

    private Map<String, String> getListLabels() {
        return this.kubernetesService.getMandatoryListLabels(this.kubernetesService.getTrainerComponentName());
    }

    private Map<String, String> getPackagerLabels() {
        return this.kubernetesService.getMandatoryListLabels(this.kubernetesService.getTrainerComponentName());
    }

    private Map<String, String> getGatekeeperLabels() {
        return this.kubernetesService.getMandatoryListLabels(GATEKEEPER_COMPONENT_NAME);
    }

    private Map<String, String> getInternalIngressControllerLabels() {
        return this.kubernetesService.getMandatoryListLabels(INTERNAL_INGRESS_CONTROLLER_COMPONENT_NAME);
    }

    private Map<String, String> getExternalIngressControllerLabels() {
        final Map<String, String> labels = new HashMap<>();

        labels.put(EXTERNAL_INGRESS_CONTROLLER_LABEL_KEY, EXTERNAL_INGRESS_CONTROLLER_LABEL_VALUE);

        return labels;
    }

    private Map<String, String> getAmbassadorLabels() {
        final Map<String, String> labels = new HashMap<>();

        labels.put(this.kubernetesService.getNameLabelKey(), AMBASSADOR_NAME_LABEL_VALUE);

        return labels;
    }

    private Map<String, String> getModelLabels() {
        return this.kubernetesService.getMandatoryListLabels(MODEL_COMPONENT_NAME);
    }

    private V1WeightedPodAffinityTerm getWeightedAffinity(Map<String, String> labels, int weight) {
        final V1LabelSelector ambassadorAffinitySelector = getLabelSelectorFromLabels(labels);

        final V1PodAffinityTerm ambassadorAffinityTerm = new V1PodAffinityTerm();
        ambassadorAffinityTerm.setTopologyKey(AFFINITY_TOPOLOGY_KEY);
        ambassadorAffinityTerm.setLabelSelector(ambassadorAffinitySelector);

        final V1WeightedPodAffinityTerm ambassadorWeightedAffinity = new V1WeightedPodAffinityTerm();
        ambassadorWeightedAffinity.setWeight(weight);
        ambassadorWeightedAffinity.setPodAffinityTerm(ambassadorAffinityTerm);

        return ambassadorWeightedAffinity;
    }

    private V1LabelSelector getLabelSelectorFromLabels(Map<String, String> labels) {
        final List<V1LabelSelectorRequirement> matchExpressions = labels.entrySet().stream().map(label -> {
            final V1LabelSelectorRequirement selectorRequirement = new V1LabelSelectorRequirement();
            selectorRequirement.setKey(label.getKey());
            selectorRequirement.setOperator("In");
            selectorRequirement.addValuesItem(label.getValue());

            return selectorRequirement;
        }).collect(Collectors.toList());

        final V1LabelSelector labelSelector = new V1LabelSelector();
        labelSelector.setMatchExpressions(matchExpressions);

        return labelSelector;
    }
}
