package com.ericsson.mxe.backendservicescommon.kubernetes;

import static io.kubernetes.client.util.Config.SERVICEACCOUNT_ROOT;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import com.ericsson.mxe.backendservicescommon.config.properties.KubernetesServiceProperties;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonSyntaxException;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinition;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1Ingress;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobList;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimList;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.openapi.models.V1StatefulSetList;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.PatchUtils;
import okhttp3.logging.HttpLoggingInterceptor;

@Service
public class KubernetesService {

    private static final Logger logger = LogManager.getLogger(KubernetesService.class);

    private final KubernetesServiceProperties kubernetesServiceProperties;
    private final String namespace;
    private final BatchV1Api batchApi;
    private final CoreV1Api coreApi;
    private final AppsV1Api appsApi;
    private final NetworkingV1Api networkingApi;

    private final CustomObjectsApi customObjectsApi;
    private final SharedInformerFactory factory;

    public KubernetesService(final KubernetesServiceProperties kubernetesServiceProperties) {
        this.kubernetesServiceProperties = kubernetesServiceProperties;
        logger.info("KubernetesServiceConfig:\r\n{}", kubernetesServiceProperties);
        namespace = loadNameSpace();
        logger.info("Namespace detected: {}", namespace);

        final ApiClient customObjectsApiClient = initApi();
        final ApiClient apiClient = initApi(V1Patch.PATCH_FORMAT_JSON_PATCH);

        this.batchApi = new BatchV1Api(apiClient);
        this.coreApi = new CoreV1Api(apiClient);
        this.customObjectsApi = new CustomObjectsApi(customObjectsApiClient);
        this.appsApi = new AppsV1Api(apiClient);
        this.networkingApi = new NetworkingV1Api(apiClient);
        this.factory = new SharedInformerFactory(apiClient);
    }

    public String getNamespace() {
        return namespace;
    }

    public Map<String, String> getLogsForDeployments(Set<String> deploymentNames, Integer limit, Integer lastSeconds,
            Integer tailLines) {
        Map<String, String> result = new HashMap<>();
        try {
            coreApi.listNamespacedPod(getNamespace(), null, null, null, null, null, null, null, null, null, null)
                    .getItems().stream().filter(v1Pod -> deploymentNames.contains(getPodDeploymentName(v1Pod)))
                    .forEach(v1Pod -> {
                        String podName = v1Pod.getMetadata().getName();
                        getPodContainers(v1Pod).forEach(containerName -> result.put(podName + "/" + containerName,
                                getContainerLog(podName, containerName, limit, lastSeconds, tailLines)));
                    });
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getPodDeploymentName(V1Pod v1Pod) {
        try {
            return v1Pod.getMetadata().getLabels().getOrDefault("app", "-");
        } catch (Exception e) {
            e.printStackTrace();
            return "-";
        }

    }

    private List<String> getPodContainers(V1Pod v1Pod) {
        return v1Pod.getSpec().getContainers().stream().map(v1Container -> v1Container.getName())
                .collect(Collectors.toList());
    }

    private String getContainerLog(String podName, String containerName, final Integer limit, Integer lastSeconds,
            final Integer tailLines) {
        try {
            String log = coreApi.readNamespacedPodLog(podName, getNamespace(), containerName, false, null, limit,
                    "true", null, lastSeconds, tailLines, null);
            if (log == null)
                return "<no logs within the specified range>";
            else
                return log;
        } catch (ApiException e) {
            e.printStackTrace();
            return "<logs unavailable>";
        }
    }

    public V1Job createNamespacedJob(String namespace, V1Job body) throws ApiException {
        return this.batchApi.createNamespacedJob(namespace, body, null, null, null, null);
    }

    public V1Status deleteNamespacedJob(String namespace, String jobName) throws ApiException {
        try {
            final V1DeleteOptions deleteOptions = new V1DeleteOptions();
            deleteOptions.propagationPolicy("Background");

            return this.batchApi.deleteNamespacedJob(jobName, namespace, null, null, null, null, null, deleteOptions);
        } catch (JsonSyntaxException e) {
            logger.warn("JsonSyntaxException during job removal is a known api bug", e);
            return new V1Status();
        }
    }

    public V1JobList listNamespacedJobs(String namespace) throws ApiException {
        return this.batchApi.listNamespacedJob(namespace, null, null, null, null, null, null, null, null, null, null);
    }

    public V1JobList listNamespacedJobs(String namespace, String labelSelector, Integer limit, Integer timeoutSeconds)
            throws ApiException {
        return this.batchApi.listNamespacedJob(namespace, null, null, null, null, labelSelector, limit, null, null,
                timeoutSeconds, null);
    }

    public V1StatefulSet createNamespacedStatefulSet(String namespace, V1StatefulSet body) throws ApiException {
        return appsApi.createNamespacedStatefulSet(namespace, body, null, null, null, null);
    }

    public V1Service createNamespacedService(String namespace, V1Service body) throws ApiException {
        return coreApi.createNamespacedService(namespace, body, null, null, null, null);
    }

    public V1Ingress createNamespacedIngress(String namespace, V1Ingress body) throws ApiException {
        return networkingApi.createNamespacedIngress(namespace, body, null, null, null, null);
    }

    public V1Secret createNamespacedSecret(String namespace, V1Secret body) throws ApiException {
        return coreApi.createNamespacedSecret(namespace, body, null, null, null, null);
    }

    public V1ConfigMap createNamespacedConfigMap(String namespace, V1ConfigMap body) throws ApiException {
        return coreApi.createNamespacedConfigMap(namespace, body, null, null, null, null);
    }

    public V1Service deleteNamespacedService(String name, String namespace, V1DeleteOptions options)
            throws ApiException {
        return coreApi.deleteNamespacedService(name, namespace, null, null, null, null, null, options);
    }

    public V1Status deleteCollectionNamespacedDeployment(String namespace, String labelSelector) throws ApiException {
        return appsApi.deleteCollectionNamespacedDeployment(namespace, null, null, null, null, null, labelSelector,
                null, null, null, null, null, null, null);
    }

    public V1Status deleteCollectionNamespacedIngress(String namespace, String labelSelector) throws ApiException {
        return networkingApi.deleteCollectionNamespacedIngress(namespace, null, null, null, null, null, labelSelector,
                null, null, null, null, null, null, null);
    }

    public V1Status deleteCollectionNamespacedPersistentVolumeClaim(String namespace, String labelSelector)
            throws ApiException {
        return coreApi.deleteCollectionNamespacedPersistentVolumeClaim(namespace, null, null, null, null, null,
                labelSelector, null, null, null, null, null, null, null);
    }

    public V1StatefulSetList listNamespacedStatefulSet(String namespace, String labelSelector) throws ApiException {
        return appsApi.listNamespacedStatefulSet(namespace, null, null, null, null, labelSelector, null, null, null,
                null, null);
    }

    public V1DeploymentList listNamespacedDeployment(String namespace, String labelSelector) throws ApiException {
        return appsApi.listNamespacedDeployment(namespace, null, null, null, null, labelSelector, null, null, null,
                null, null);
    }

    public V1PodList listNamespacedPod(String namespace, String labelSelector) throws ApiException {
        return this.coreApi.listNamespacedPod(namespace, null, null, null, null, labelSelector, null, null, null, null,
                null);
    }

    public V1ConfigMapList listNamespacedConfigMap(String namespace, String labelSelector) throws ApiException {
        return this.coreApi.listNamespacedConfigMap(namespace, null, null, null, null, labelSelector, null, null, null,
                null, null);
    }

    public V1PersistentVolumeClaimList listNamespacedPersistentVolumeClaim(String namespace, String labelSelector)
            throws ApiException {
        return this.coreApi.listNamespacedPersistentVolumeClaim(namespace, null, null, null, null, labelSelector, null,
                null, null, null, null);
    }

    public void patchNamespacedStatefulSet(String name, String namespace, String body) throws ApiException {
        this.appsApi.patchNamespacedStatefulSet(name, namespace, new V1Patch(body), null, null, null, null, false);
    }

    public void patchNamespacedConfigMap(String name, String namespace, String body) throws ApiException {
        this.coreApi.patchNamespacedConfigMap(name, namespace, new V1Patch(body), null, null, null, null, false);
    }

    public Object getNamespacedCustomObject(String group, String version, String namespace, String plural, String name)
            throws ApiException {
        return this.customObjectsApi.getNamespacedCustomObject(group, version, namespace, plural, name);
    }

    public Object listNamespacedCustomObject(String group, String version, String namespace, String plural)
            throws ApiException {
        return this.customObjectsApi.listNamespacedCustomObject(group, version, namespace, plural, null, null, null,
                null, null, null, null, null, null, null);
    }

    public Object createNamespacedCustomObject(String group, String version, String namespace, String plural,
            Object body) throws ApiException {
        return this.customObjectsApi.createNamespacedCustomObject(group, version, namespace, plural, body, null, null,
                null);
    }

    public Object createNamespacedCustomObjectDryRun(String group, String version, String namespace, String plural,
            Object body) throws ApiException {
        String dryRun = "All";
        return this.customObjectsApi.createNamespacedCustomObject(group, version, namespace, plural, body, null, dryRun,
                null);
    }

    public Object deleteNamespacedCustomObject(String group, String version, String namespace, String plural,
            String name, V1DeleteOptions options) throws ApiException {
        return this.customObjectsApi.deleteNamespacedCustomObject(group, version, namespace, plural, name, null, null,
                null, null, options);
    }

    public Object patchNamespacedCustomObject(String group, String version, String namespace, String plural,
            String name, Object body) throws ApiException {

        return PatchUtils.patch(V1CustomResourceDefinition.class,
                () -> customObjectsApi.patchNamespacedCustomObjectCall(group, version, namespace, plural, name, body,
                        null, null, null, null),
                V1Patch.PATCH_FORMAT_JSON_MERGE_PATCH, customObjectsApi.getApiClient());
    }

    public Object replaceNamespacedCustomObject(String group, String version, String namespace, String plural,
            String name, Object body) throws ApiException {
        return this.customObjectsApi.replaceNamespacedCustomObject(group, version, namespace, plural, name, body, null,
                null);
    }

    public Object replaceNamespacedCustomObjectDryRun(String group, String version, String namespace, String plural,
            String name, Object body) throws ApiException {
        String dryRun = "All";
        return this.customObjectsApi.replaceNamespacedCustomObject(group, version, namespace, plural, name, body,
                dryRun, null);
    }

    public V1Secret readNamespacedSecret(String name, String namespace) throws ApiException {
        return this.coreApi.readNamespacedSecret(name, namespace, null);
    }

    public void replaceNamespacedSecret(String name, String namespace, V1Secret body) throws ApiException {
        this.coreApi.replaceNamespacedSecret(name, namespace, body, null, null, null, null);
    }

    private ApiClient initApi() {
        return this.initApi(null);
    }

    @VisibleForTesting
    protected ApiClient initApi(String patchFormat) {
        final ClientBuilder clientBuilder;
        try {
            if (kubernetesServiceProperties.isLocalTestEnabled()) {
                final String kubeConfigFile =
                        System.getProperty("user.home") + File.separator + ".kube" + File.separator + "config";
                logger.info("Local test is enabled, using kubernetes configuration for API init: {}", kubeConfigFile);
                final KubeConfig config = KubeConfig.loadKubeConfig(new FileReader(kubeConfigFile));
                config.setFile(new File(kubeConfigFile));
                clientBuilder = ClientBuilder.kubeconfig(config);
            } else {
                clientBuilder = ClientBuilder.cluster();
            }
        } catch (IOException e) {
            logger.error("Unable to init Kubernetes configuration", e);
            throw new MxeInternalException("Unable to init Kubernetes configuration");
        }

        if (patchFormat != null) {
            /* Removed as part of https://github.com/kubernetes-client/java/pull/959 */
            // clientBuilder.setOverridePatchFormat(patchFormat);
        }

        final ApiClient apiClient = clientBuilder.build();
        apiClient.setDebugging(false); // Watch is incompatible with debugging mode active
        apiClient.getHttpClient().interceptors().forEach(interceptor -> {
            if(interceptor instanceof HttpLoggingInterceptor httpLoggingInterceptor) {
                httpLoggingInterceptor.redactHeader("Authorization");
                httpLoggingInterceptor.redactHeader("Cookie");
                }
        });

        return apiClient;
    }

    @VisibleForTesting
    protected String loadNameSpace() {
        if (kubernetesServiceProperties.isLocalTestEnabled()) {
            return kubernetesServiceProperties.getLocalTestNamespace();
        }

        final String namespacePath = SERVICEACCOUNT_ROOT + "/namespace";

        try {
            return Files.readString(Paths.get(namespacePath), Charset.defaultCharset());
        } catch (IOException e) {
            logger.error("Unable to get current namespace", e);
            throw new MxeInternalException("Unable to get current namespace");
        }
    }

    public String getPackageNameLabelKey() {
        return "mxe/package-name";
    }

    public String getComponentLabelKey() {
        return "app.kubernetes.io/component";
    }

    public String getInstanceLabelKey() {
        return "app.kubernetes.io/instance";
    }

    public String getPackagerComponentName() {
        return "packager";
    }

    public String getTrainerComponentName() {
        return "trainer";
    }

    public String getVersionLabelKey() {
        return "app.kubernetes.io/version";
    }

    public String getNameLabelKey() {
        return "app.kubernetes.io/name";
    }

    public String getManagedByLabelKey() {
        return "app.kubernetes.io/managed-by";
    }

    public String getPartOfLabelKey() {
        return "app.kubernetes.io/part-of";
    }

    public String getModelPodIPLabelKey() {
        return "mxe/model-pod-ip";
    }

    public String getModelPodIPEnvName() {
        return "POD_IP";
    }

    public String getPartOfLabelValue() {
        return "mxe";
    }

    public Map<String, String> getMandatoryCreationMatchLabels(final String componentName, final String instanceName) {
        final Map<String, String> labels = this.getMandatoryListLabels(componentName);
        this.addLabel(labels, this.getInstanceLabelKey(), instanceName);
        return labels;
    }

    public Map<String, String> getMandatoryListLabels(final String componentName) {
        final Map<String, String> labels = new HashMap<>();
        this.addLabel(labels, this.getPartOfLabelKey(), this.getPartOfLabelValue());
        this.addLabel(labels, this.getComponentLabelKey(), componentName);
        return labels;
    }

    public String getLabelSelector(Map<String, String> labels) {
        final StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, String> label : labels.entrySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(",");
            }

            stringBuilder.append(label.getKey());
            stringBuilder.append("=");
            stringBuilder.append(label.getValue());
        }

        return stringBuilder.toString();
    }

    public Map<String, String> getMandatoryJobLabels(final String componentName, final String instanceName) {
        final Map<String, String> labels = new HashMap<>();
        this.addLabel(labels, this.getPartOfLabelKey(), this.getPartOfLabelValue());
        this.addLabel(labels, this.getComponentLabelKey(), componentName);
        this.addLabel(labels, this.getInstanceLabelKey(), instanceName);
        return labels;
    }

    public void addLabel(Map<String, String> labels, final String key, final String value) {
        String trimmedValue = value.substring(0, Math.min(value.length(), 63));
        labels.put(key, trimmedValue);
    }

    public SharedIndexInformer<V1Job> getJobInformer() {
        String var10000 = this.getComponentLabelKey();
        String componentLabel = var10000 + "=" + this.getPackagerComponentName();
        var10000 = this.getModelPodIPLabelKey();
        String ipLabel = var10000 + "=" + this.getenv(this.getModelPodIPEnvName());
        String labelSelectors = componentLabel + "," + ipLabel;
        return this.factory.sharedIndexInformerFor((params) -> {
            return this.batchApi.listNamespacedJobCall(this.getNamespace(), (String) null, (Boolean) null,
                    (String) null, (String) null, labelSelectors, (Integer) null, params.resourceVersion, (String) null,
                    params.timeoutSeconds, params.watch, (ApiCallback) null);
        }, V1Job.class, V1JobList.class);
    }

    public void startInformers() {
        this.factory.startAllRegisteredInformers();
    }

    public String getenv(final String env) {
        return System.getenv(env);
    }
}
