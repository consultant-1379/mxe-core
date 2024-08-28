package com.ericsson.mxe.modelcatalogueservice.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.ericsson.mxe.backendservicescommon.dto.status.Status;
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.backendservicescommon.kubernetes.ServicePortResolverService;
import com.ericsson.mxe.modelcatalogueservice.config.properties.AuthorServiceProperties;
import com.ericsson.mxe.modelcatalogueservice.config.properties.DockerProperties;
import com.ericsson.mxe.modelcatalogueservice.config.properties.InstallerDockerRegistryProperties;
import com.ericsson.mxe.modelcatalogueservice.config.properties.ModelCatalogueServiceProperties;
import com.ericsson.mxe.modelcatalogueservice.config.properties.ModelPackagerProperties;
import com.ericsson.mxe.modelcatalogueservice.config.properties.PypiServiceConfig;
import com.ericsson.mxe.modelcatalogueservice.config.properties.ServiceMeshProperties;
import com.ericsson.mxe.modelcatalogueservice.dto.JobType;
import com.ericsson.mxe.modelcatalogueservice.dto.request.CreateRequest;
import com.ericsson.mxe.modelcatalogueservice.dto.response.ModelCatalogueServiceResponse;
import com.ericsson.mxe.modelcatalogueservice.services.minio.MinioRepositoryUploader;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Affinity;
import io.kubernetes.client.openapi.models.V1ConfigMapVolumeSource;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1EmptyDirVolumeSource;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1EnvVarSource;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobSpec;
import io.kubernetes.client.openapi.models.V1KeyToPath;
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
public class PackagerService {
    private static final String PULL_SECRET_VOLUME_NAME = "packager-pull-secret";
    private static final String MINIO_URL_ENV_VAR = "MINIO_URL";
    private static final String MINIO_ACCESS_KEY_ENV_VAR = "MINIO_ACCESS_KEY";
    private static final String MINIO_SECRET_KEY_ENV_VAR = "MINIO_SECRET_KEY";
    private static final String MINIO_BUCKET_ENV_VAR = "MINIO_BUCKET";
    private static final String MINIO_ACCESS_KEY_SECRET_KEY = "accesskey";
    private static final String MINIO_SECRET_KEY_SECRET_KEY = "secretkey";
    private static final String AFFINITY_TOPOLOGY_KEY = "kubernetes.io/hostname";
    private static final int PACKAGER_AFFINITY_WEIGHT = 75;
    private static final int TRAINER_AFFINITY_WEIGHT = 75;
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
    private static final String DOCKER_CONFIG_SECRET = "docker-config-secret";
    private static final String DOCKERFILE_NAME = "model-dockerfile";

    private static final String VOLUME_CACERTS = "cacerts";
    private static final String VOLUME_MNT_PATH_CACERTS = "/cacerts";
    private static final String VOLUME_INSTALLER_DOCKER_CACERT = "installer-docker-ca-cert";
    private static final String VOLUME_MNT_PATH_INSTALLER_DOCKER_CACERTS = "/usr/share/pki/trust/anchors/extca.crt";
    private static final String VOLUME_MNT_SUBPATH_INSTALLER_DOCKER_CACERTS = "ca.crt";
    private static final String EXTRA_CERT_DIR = "/var/lib/ca-certificates";
    private static final String EXTRA_CERT_FILE = EXTRA_CERT_DIR + "/ca-bundle.pem";

    private String initCaUpdateCmd;

    private final KubernetesService kubernetesService;
    private final ServicePortResolverService servicePortResolver;
    private final ModelPackagerProperties modelPackagerProperties;
    private final ModelCatalogueServiceProperties modelCatalogueServiceProperties;
    private final DockerProperties dockerProperties;
    private final InstallerDockerRegistryProperties installerDockerRegistryProperties;
    private final AuthorServiceProperties authorConfig;
    private final PypiServiceConfig pypiServiceConfig;
    private final ServiceMeshProperties serviceMeshProperties;
    private final PackagerJobEventHandlerService resourceEventHandler;
    private SharedIndexInformer<V1Job> informer;

    private final String namespace;

    public PackagerService(final KubernetesService kubernetesService,
            final ServicePortResolverService servicePortResolver, final ModelPackagerProperties modelPackagerProperties,
            final ModelCatalogueServiceProperties modelCatalogueServiceProperties,
            final DockerProperties dockerProperties,
            final InstallerDockerRegistryProperties installerDockerRegistryProperties,
            final AuthorServiceProperties authorConfig, PypiServiceConfig pypiServiceConfig,
            final ServiceMeshProperties serviceMeshProperties,
            final PackagerJobEventHandlerService resourceEventHandler) {
        this.kubernetesService = kubernetesService;
        this.servicePortResolver = servicePortResolver;
        this.modelPackagerProperties = modelPackagerProperties;
        this.modelCatalogueServiceProperties = modelCatalogueServiceProperties;
        this.namespace = kubernetesService.getNamespace();
        this.dockerProperties = dockerProperties;
        this.installerDockerRegistryProperties = installerDockerRegistryProperties;
        this.authorConfig = authorConfig;
        this.pypiServiceConfig = pypiServiceConfig;
        this.serviceMeshProperties = serviceMeshProperties;
        this.resourceEventHandler = resourceEventHandler;
        loadinitCaUpdateCmd();
    }

    private void getJobInformer() {
        informer = kubernetesService.getJobInformer();
        informer.addEventHandler(this.resourceEventHandler);
        kubernetesService.startInformers();
    }

    public <REQUEST extends CreateRequest, STATUS extends Status> ModelCatalogueServiceResponse handleDataAndJob(
            String userId, String userName, final CatalogueService<REQUEST, STATUS> service, final String fileName,
            final InputStream inputStream, final MinioRepositoryUploader minioRepositoryService, final JobType type)
            throws ApiException {
        final String repositoryFileName = minioRepositoryService.putObject(inputStream);

        final String dummyEntityName = service.createDummyEntity(userId, userName, fileName);

        startJob(repositoryFileName, dummyEntityName, minioRepositoryService, type);

        return new ModelCatalogueServiceResponse("Packaging has been started");
    }

    private void startJob(final String packageName, final String dummyEntityName,
            final MinioRepositoryUploader minioRepositoryService, final JobType type) throws ApiException {
        if (informer == null) {
            getJobInformer();
        }
        final V1Container container = this.getContainer(packageName, dummyEntityName, minioRepositoryService, type);

        final V1PodSpec podSpec = new V1PodSpec();
        podSpec.addContainersItem(container);

        if (StringUtils.isNotEmpty(this.installerDockerRegistryProperties.getCaSecretName())) {
            // Init CA Container: Required to load INSTALLER_DOCKER CA Cert
            final V1Container initCaContainer = this.getInitCaContainer();
            podSpec.addInitContainersItem(initCaContainer);

            // Add CA certs related volumes
            final V1Volume caCertsVolume = new V1Volume();
            caCertsVolume.setName(VOLUME_CACERTS);
            caCertsVolume.emptyDir(new V1EmptyDirVolumeSource());
            podSpec.addVolumesItem(caCertsVolume);

            final V1Volume installerDockerCaCertVolume = new V1Volume();
            installerDockerCaCertVolume.setName(VOLUME_INSTALLER_DOCKER_CACERT);
            final V1SecretVolumeSource installerDockerCaSecretVolumeSource = new V1SecretVolumeSource();
            installerDockerCaSecretVolumeSource.setSecretName(this.installerDockerRegistryProperties.getCaSecretName());
            installerDockerCaCertVolume.setSecret(installerDockerCaSecretVolumeSource);
            podSpec.addVolumesItem(installerDockerCaCertVolume);
        }

        podSpec.setRestartPolicy("OnFailure");
        podSpec.setAffinity(getAffinity());

        if (StringUtils.isNotEmpty(modelPackagerProperties.getPullSecret())) {
            final V1LocalObjectReference pullSecretName = new V1LocalObjectReference();
            pullSecretName.setName(modelPackagerProperties.getPullSecret());

            podSpec.addImagePullSecretsItem(pullSecretName);

            final V1Volume pullSecretVolume = new V1Volume();
            pullSecretVolume.setName(PULL_SECRET_VOLUME_NAME);

            final V1SecretVolumeSource pullSecretVolumeSource = new V1SecretVolumeSource();
            pullSecretVolumeSource.setSecretName(modelPackagerProperties.getPullSecret());
            pullSecretVolume.setSecret(pullSecretVolumeSource);

            podSpec.addVolumesItem(pullSecretVolume);

            final V1VolumeMount pullSecretVolumeMount = new V1VolumeMount();
            pullSecretVolumeMount.setName(PULL_SECRET_VOLUME_NAME);
            pullSecretVolumeMount.setMountPath("/mnt/docker");
            pullSecretVolumeMount.setReadOnly(true);

            container.addVolumeMountsItem(pullSecretVolumeMount);
        }

        if (StringUtils.isNotEmpty(this.dockerProperties.getRegistry().getSecretName())) {
            V1KeyToPath renameDockerConfig = new V1KeyToPath();
            renameDockerConfig.setKey(".dockerconfigjson");
            renameDockerConfig.setPath("config.json");

            List<V1KeyToPath> items = new ArrayList<V1KeyToPath>();
            items.add(renameDockerConfig);

            final V1SecretVolumeSource dockerConfigSecretVolumeSource = new V1SecretVolumeSource();
            dockerConfigSecretVolumeSource.setSecretName(this.dockerProperties.getRegistry().getSecretName());
            dockerConfigSecretVolumeSource.setItems(items);

            final V1Volume dockerConfigSecretVolume = new V1Volume();
            dockerConfigSecretVolume.setName(DOCKER_CONFIG_SECRET);
            dockerConfigSecretVolume.setSecret(dockerConfigSecretVolumeSource);

            podSpec.addVolumesItem(dockerConfigSecretVolume);

            final V1VolumeMount dockerConfigSecretVolumeMount = new V1VolumeMount();
            dockerConfigSecretVolumeMount.setName(DOCKER_CONFIG_SECRET);
            dockerConfigSecretVolumeMount.setMountPath("/mnt/.docker");
            dockerConfigSecretVolumeMount.setReadOnly(true);
            container.addVolumeMountsItem(dockerConfigSecretVolumeMount);

        } else {
            // TODO Failure handling
        }

        if (StringUtils.isNotEmpty(this.modelCatalogueServiceProperties.getDockerfile())) {
            final V1ConfigMapVolumeSource dockerfileConfigMapVolumeSource = new V1ConfigMapVolumeSource();
            dockerfileConfigMapVolumeSource.setName(this.modelCatalogueServiceProperties.getDockerfile());

            final V1Volume dockerfileVolume = new V1Volume();
            dockerfileVolume.setName(DOCKERFILE_NAME);
            dockerfileVolume.setConfigMap(dockerfileConfigMapVolumeSource);

            podSpec.addVolumesItem(dockerfileVolume);

            final V1VolumeMount dockerfileSecretVolumeMount = new V1VolumeMount();
            dockerfileSecretVolumeMount.setName(DOCKERFILE_NAME);
            dockerfileSecretVolumeMount.setMountPath("/mnt/dockerfile");
            dockerfileSecretVolumeMount.setReadOnly(true);
            container.addVolumeMountsItem(dockerfileSecretVolumeMount);
        }

        final Map<String, String> jobLabels = this.getJobLabels(packageName);

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
        jobSpec.setBackoffLimit(0);

        final V1ObjectMeta deploymentMeta = new V1ObjectMeta();
        deploymentMeta.setName(this.getName(packageName));
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

    private Map<String, String> getJobLabels(final String packageName) {
        final Map<String, String> labels = getSelectorLabels();
        labels.put(this.kubernetesService.getPackageNameLabelKey(), packageName);
        labels.put(this.kubernetesService.getVersionLabelKey(), this.modelCatalogueServiceProperties.getVersion());
        labels.put(this.kubernetesService.getNameLabelKey(), this.getName(packageName));
        labels.put(this.kubernetesService.getManagedByLabelKey(), this.modelCatalogueServiceProperties.getName());
        labels.put(this.kubernetesService.getModelPodIPLabelKey(),
                this.kubernetesService.getenv(this.kubernetesService.getModelPodIPEnvName()));
        return labels;
    }

    private Map<String, String> getPackagerPodLabels() {
        final Map<String, String> labels = getSelectorLabels();
        labels.put(this.kubernetesService.getVersionLabelKey(), this.modelCatalogueServiceProperties.getVersion());
        labels.put(this.kubernetesService.getManagedByLabelKey(), this.modelCatalogueServiceProperties.getName());
        return labels;
    }

    private Map<String, String> getSelectorLabels() {
        return this.kubernetesService.getMandatoryJobLabels(this.kubernetesService.getPackagerComponentName(),
                this.modelCatalogueServiceProperties.getInstanceName());
    }

    private V1Container getContainer(final String packageName, final String dummyModelEntryName,
            final MinioRepositoryUploader minioRepositoryService, final JobType jobType) {
        final V1Container container = new V1Container();

        addEnv(container, getMinioUrlEnv(minioRepositoryService));
        addEnv(container, getMinioBucketEnv(minioRepositoryService));
        addEnv(container, getMinioAccessKeyEnv(minioRepositoryService));
        addEnv(container, getMinioSecretKeyEnv(minioRepositoryService));

        addEnv(container, "PACKAGE_NAME", packageName);
        addEnv(container, "DUMMY_MODEL_ENTRY_NAME", dummyModelEntryName);

        addEnv(container, "DOCKER_REGISTRY_HOSTNAME", dockerProperties.getRegistry().getHostname());
        addEnv(container, "MODEL_CATALOG_HOST", modelCatalogueServiceProperties.getName());

        addEnv(container, "PACKAGE_TYPE", jobType.getType());

        addEnv(container, "AUTHOR_SERVICE_HOST", authorConfig.getHostName());
        addEnv(container, "AUTHOR_SERVICE_PORT", getAuthorServicePort());

        addEnv(container, "PYPISERVICE_INTERNAL_SERVER", pypiServiceConfig.getInternalServer());
        addEnv(container, "PYPISERVICE_EXTERNAL_SERVER", pypiServiceConfig.getExternalServer());

        addEnv(container, "SERVICE_MESH_MTLS_ENABLED", String.valueOf(serviceMeshProperties.isMtlsEnabled()));

        if (StringUtils.isNotEmpty(this.installerDockerRegistryProperties.getCaSecretName())) {
            addEnv(container, "SSL_CERT_DIR", EXTRA_CERT_DIR + "/pem");
            addEnv(container, "SSL_CERT_FILE", EXTRA_CERT_FILE);

            final V1VolumeMount cacertsPath = new V1VolumeMount();
            cacertsPath.setName(VOLUME_CACERTS);
            cacertsPath.setMountPath(EXTRA_CERT_DIR);
            container.addVolumeMountsItem(cacertsPath);
        }

        container.setImage(this.modelPackagerProperties.getImage());
        container.setName(this.getName(packageName));
        container.setImagePullPolicy(this.modelPackagerProperties.getPullPolicy());

        return container;
    }

    private V1Container getInitCaContainer() {
        final V1Container container = new V1Container();
        container.setImage(this.modelPackagerProperties.getImage());
        container.setName("init-ca-container");
        container.setImagePullPolicy(this.modelPackagerProperties.getPullPolicy());

        container.addCommandItem("bash");
        container.addCommandItem("-c");
        container.addCommandItem(initCaUpdateCmd);

        final V1VolumeMount cacertsPath = new V1VolumeMount();
        cacertsPath.setName(VOLUME_CACERTS);
        cacertsPath.setMountPath(VOLUME_MNT_PATH_CACERTS);
        container.addVolumeMountsItem(cacertsPath);

        final V1VolumeMount installerDockerCaCertPath = new V1VolumeMount();
        installerDockerCaCertPath.setName(VOLUME_INSTALLER_DOCKER_CACERT);
        installerDockerCaCertPath.setMountPath(VOLUME_MNT_PATH_INSTALLER_DOCKER_CACERTS);
        installerDockerCaCertPath.setSubPath(VOLUME_MNT_SUBPATH_INSTALLER_DOCKER_CACERTS);
        container.addVolumeMountsItem(installerDockerCaCertPath);

        return container;
    }

    private void loadinitCaUpdateCmd() {
        final StringBuilder sb = new StringBuilder("");
        sb.append("mkdir -p /cacerts/pem;");
        sb.append("cp -r /etc/ssl/certs/*.* /cacerts/pem/;");
        sb.append("touch /cacerts/ca-bundle.pem;");
        sb.append("for pem in `ls /cacerts/pem/*.pem`; do cat $pem >> /cacerts/ca-bundle.pem; done;");
        sb.append("cp " + VOLUME_MNT_PATH_INSTALLER_DOCKER_CACERTS + " /cacerts/pem/rootca.pem;");
        sb.append("cat /usr/share/pki/trust/anchors/extca.crt >>  /cacerts/ca-bundle.pem;");
        sb.append("cd /cacerts/pem; hashkey=`openssl x509 -in rootca.pem -noout -hash`;");
        sb.append("if [ ! -L $hashkey.0 ]; then ln -s rootca.pem $hashkey.0; fi;");
        sb.append("chmod 555 /cacerts/pem;");
        sb.append("chmod 444 /cacerts/ca-bundle.pem;");
        initCaUpdateCmd = sb.toString();
    }

    private String getAuthorServicePort() {
        final Optional<Integer> port = this.servicePortResolver.resolve(this.authorConfig.getHostName(),
                this.authorConfig.getPortName(), "tcp");

        return port.orElse(8080).toString();
    }

    private void addEnv(final V1Container container, final String envName, final String envValue) {
        addEnv(container, getEnv(envName, envValue));
    }

    private void addEnv(final V1Container container, final V1EnvVar env) {
        container.addEnvItem(env);
    }

    private V1EnvVar getMinioUrlEnv(final MinioRepositoryUploader minioRepositoryService) {
        return getEnv(MINIO_URL_ENV_VAR, minioRepositoryService.getUrl().toString());
    }

    private V1EnvVar getMinioBucketEnv(final MinioRepositoryUploader minioRepositoryService) {
        return getEnv(MINIO_BUCKET_ENV_VAR, minioRepositoryService.getBucket());
    }

    private V1EnvVar getMinioAccessKeyEnv(final MinioRepositoryUploader minioRepositoryService) {
        return getMinioSecretEnv(minioRepositoryService, MINIO_ACCESS_KEY_ENV_VAR, MINIO_ACCESS_KEY_SECRET_KEY);
    }

    private V1EnvVar getMinioSecretKeyEnv(final MinioRepositoryUploader minioRepositoryService) {
        return getMinioSecretEnv(minioRepositoryService, MINIO_SECRET_KEY_ENV_VAR, MINIO_SECRET_KEY_SECRET_KEY);
    }

    private V1EnvVar getMinioSecretEnv(final MinioRepositoryUploader minioRepositoryService, final String envName,
            final String secretKey) {
        final V1SecretKeySelector envSecretKeySelector = new V1SecretKeySelector();
        envSecretKeySelector.setName(minioRepositoryService.getInstanceSecretName());
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
        return "mxe-packager-" + escapedPackageName;
    }

    private V1Affinity getAffinity() {
        final V1WeightedPodAffinityTerm packagerWeightedAffinity =
                getWeightedAffinity(getListLabels(), PACKAGER_AFFINITY_WEIGHT);
        final V1WeightedPodAffinityTerm trainerWeightedAffinity =
                getWeightedAffinity(getTrainerLabels(), TRAINER_AFFINITY_WEIGHT);
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
        podAntiAffinity.addPreferredDuringSchedulingIgnoredDuringExecutionItem(packagerWeightedAffinity);
        podAntiAffinity.addPreferredDuringSchedulingIgnoredDuringExecutionItem(trainerWeightedAffinity);
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
        return this.kubernetesService.getMandatoryListLabels(this.kubernetesService.getPackagerComponentName());
    }

    private Map<String, String> getTrainerLabels() {
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
        }).toList();

        final V1LabelSelector labelSelector = new V1LabelSelector();
        labelSelector.setMatchExpressions(matchExpressions);

        return labelSelector;
    }
}
