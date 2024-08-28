package com.ericsson.mxe.jcat.json.prometheus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"__name__", "app", "instance", "job", "kubernetes_namespace", "kubernetes_pod_name",
        "pod_template_hash", "seldon_app", "seldon_app_mymodel2_0_0_1", "seldon_deployment_id",
        "seldon_app_seldon_python_reference_0_0_1", "seldon_app_seldon_example_0_0_1", "seldon_app_sklearn_iris_0_0_1"})
public class Metric implements Serializable {

    @JsonProperty("__name__")
    private String name;
    @JsonProperty("app")
    private String app;
    @JsonProperty("instance")
    private String instance;
    @JsonProperty("job")
    private String job;
    @JsonProperty("kubernetes_namespace")
    private String kubernetesNamespace;
    @JsonProperty("kubernetes_pod_name")
    private String kubernetesPodName;
    @JsonProperty("pod_template_hash")
    private String podTemplateHash;
    @JsonProperty("seldon_app")
    private String seldonApp;
    @JsonProperty("seldon_app_mymodel2_0_0_1")
    private String seldonAppMymodel2001;
    @JsonProperty("seldon_deployment_id")
    private String seldonDeploymentId;
    @JsonProperty("seldon_app_seldon_python_reference_0_0_1")
    private String seldonAppSeldonPythonReference001;
    @JsonProperty("seldon_app_seldon_example_0_0_1")
    private String seldonAppSeldonExample001;
    @JsonProperty("seldon_app_sklearn_iris_0_0_1")
    private String seldonAppSklearnIris001;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 5828920610555749890L;

    /**
     * No args constructor for use in serialization
     */
    public Metric() {}

    /**
     * @param app
     * @param podTemplateHash
     * @param kubernetesNamespace
     * @param seldonAppSeldonPythonReference001
     * @param job
     * @param seldonAppSeldonExample001
     * @param kubernetesPodName
     * @param seldonAppSklearnIris001
     * @param name
     * @param seldonAppMymodel2001
     * @param seldonDeploymentId
     * @param instance
     * @param seldonApp
     */
    public Metric(String name, String app, String instance, String job, String kubernetesNamespace,
            String kubernetesPodName, String podTemplateHash, String seldonApp, String seldonAppMymodel2001,
            String seldonDeploymentId, String seldonAppSeldonPythonReference001, String seldonAppSeldonExample001,
            String seldonAppSklearnIris001) {
        super();
        this.name = name;
        this.app = app;
        this.instance = instance;
        this.job = job;
        this.kubernetesNamespace = kubernetesNamespace;
        this.kubernetesPodName = kubernetesPodName;
        this.podTemplateHash = podTemplateHash;
        this.seldonApp = seldonApp;
        this.seldonAppMymodel2001 = seldonAppMymodel2001;
        this.seldonDeploymentId = seldonDeploymentId;
        this.seldonAppSeldonPythonReference001 = seldonAppSeldonPythonReference001;
        this.seldonAppSeldonExample001 = seldonAppSeldonExample001;
        this.seldonAppSklearnIris001 = seldonAppSklearnIris001;
    }

    @JsonProperty("__name__")
    public String getName() {
        return name;
    }

    @JsonProperty("__name__")
    public void setName(String name) {
        this.name = name;
    }

    public Metric withName(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty("app")
    public String getApp() {
        return app;
    }

    @JsonProperty("app")
    public void setApp(String app) {
        this.app = app;
    }

    public Metric withApp(String app) {
        this.app = app;
        return this;
    }

    @JsonProperty("instance")
    public String getInstance() {
        return instance;
    }

    @JsonProperty("instance")
    public void setInstance(String instance) {
        this.instance = instance;
    }

    public Metric withInstance(String instance) {
        this.instance = instance;
        return this;
    }

    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    public Metric withJob(String job) {
        this.job = job;
        return this;
    }

    @JsonProperty("kubernetes_namespace")
    public String getKubernetesNamespace() {
        return kubernetesNamespace;
    }

    @JsonProperty("kubernetes_namespace")
    public void setKubernetesNamespace(String kubernetesNamespace) {
        this.kubernetesNamespace = kubernetesNamespace;
    }

    public Metric withKubernetesNamespace(String kubernetesNamespace) {
        this.kubernetesNamespace = kubernetesNamespace;
        return this;
    }

    @JsonProperty("kubernetes_pod_name")
    public String getKubernetesPodName() {
        return kubernetesPodName;
    }

    @JsonProperty("kubernetes_pod_name")
    public void setKubernetesPodName(String kubernetesPodName) {
        this.kubernetesPodName = kubernetesPodName;
    }

    public Metric withKubernetesPodName(String kubernetesPodName) {
        this.kubernetesPodName = kubernetesPodName;
        return this;
    }

    @JsonProperty("pod_template_hash")
    public String getPodTemplateHash() {
        return podTemplateHash;
    }

    @JsonProperty("pod_template_hash")
    public void setPodTemplateHash(String podTemplateHash) {
        this.podTemplateHash = podTemplateHash;
    }

    public Metric withPodTemplateHash(String podTemplateHash) {
        this.podTemplateHash = podTemplateHash;
        return this;
    }

    @JsonProperty("seldon_app")
    public String getSeldonApp() {
        return seldonApp;
    }

    @JsonProperty("seldon_app")
    public void setSeldonApp(String seldonApp) {
        this.seldonApp = seldonApp;
    }

    public Metric withSeldonApp(String seldonApp) {
        this.seldonApp = seldonApp;
        return this;
    }

    @JsonProperty("seldon_app_mymodel2_0_0_1")
    public String getSeldonAppMymodel2001() {
        return seldonAppMymodel2001;
    }

    @JsonProperty("seldon_app_mymodel2_0_0_1")
    public void setSeldonAppMymodel2001(String seldonAppMymodel2001) {
        this.seldonAppMymodel2001 = seldonAppMymodel2001;
    }

    public Metric withSeldonAppMymodel2001(String seldonAppMymodel2001) {
        this.seldonAppMymodel2001 = seldonAppMymodel2001;
        return this;
    }

    @JsonProperty("seldon_deployment_id")
    public String getSeldonDeploymentId() {
        return seldonDeploymentId;
    }

    @JsonProperty("seldon_deployment_id")
    public void setSeldonDeploymentId(String seldonDeploymentId) {
        this.seldonDeploymentId = seldonDeploymentId;
    }

    public Metric withSeldonDeploymentId(String seldonDeploymentId) {
        this.seldonDeploymentId = seldonDeploymentId;
        return this;
    }

    @JsonProperty("seldon_app_seldon_python_reference_0_0_1")
    public String getSeldonAppSeldonPythonReference001() {
        return seldonAppSeldonPythonReference001;
    }

    @JsonProperty("seldon_app_seldon_python_reference_0_0_1")
    public void setSeldonAppSeldonPythonReference001(String seldonAppSeldonPythonReference001) {
        this.seldonAppSeldonPythonReference001 = seldonAppSeldonPythonReference001;
    }

    public Metric withSeldonAppSeldonPythonReference001(String seldonAppSeldonPythonReference001) {
        this.seldonAppSeldonPythonReference001 = seldonAppSeldonPythonReference001;
        return this;
    }

    @JsonProperty("seldon_app_seldon_example_0_0_1")
    public String getSeldonAppSeldonExample001() {
        return seldonAppSeldonExample001;
    }

    @JsonProperty("seldon_app_seldon_example_0_0_1")
    public void setSeldonAppSeldonExample001(String seldonAppSeldonExample001) {
        this.seldonAppSeldonExample001 = seldonAppSeldonExample001;
    }

    public Metric withSeldonAppSeldonExample001(String seldonAppSeldonExample001) {
        this.seldonAppSeldonExample001 = seldonAppSeldonExample001;
        return this;
    }

    @JsonProperty("seldon_app_sklearn_iris_0_0_1")
    public String getSeldonAppSklearnIris001() {
        return seldonAppSklearnIris001;
    }

    @JsonProperty("seldon_app_sklearn_iris_0_0_1")
    public void setSeldonAppSklearnIris001(String seldonAppSklearnIris001) {
        this.seldonAppSklearnIris001 = seldonAppSklearnIris001;
    }

    public Metric withSeldonAppSklearnIris001(String seldonAppSklearnIris001) {
        this.seldonAppSklearnIris001 = seldonAppSklearnIris001;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Metric withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE).append("name", name).append("app", app)
                .append("instance", instance).append("job", job).append("kubernetesNamespace", kubernetesNamespace)
                .append("kubernetesPodName", kubernetesPodName).append("podTemplateHash", podTemplateHash)
                .append("seldonApp", seldonApp).append("seldonAppMymodel2001", seldonAppMymodel2001)
                .append("seldonDeploymentId", seldonDeploymentId)
                .append("seldonAppSeldonPythonReference001", seldonAppSeldonPythonReference001)
                .append("seldonAppSeldonExample001", seldonAppSeldonExample001)
                .append("seldonAppSklearnIris001", seldonAppSklearnIris001)
                .append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(app).append(podTemplateHash).append(kubernetesNamespace)
                .append(seldonAppSeldonPythonReference001).append(job).append(seldonAppSeldonExample001)
                .append(kubernetesPodName).append(seldonAppSklearnIris001).append(additionalProperties).append(name)
                .append(seldonDeploymentId).append(seldonAppMymodel2001).append(instance).append(seldonApp)
                .toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Metric) == false) {
            return false;
        }
        Metric rhs = ((Metric) other);
        return new EqualsBuilder().append(app, rhs.app).append(podTemplateHash, rhs.podTemplateHash)
                .append(kubernetesNamespace, rhs.kubernetesNamespace)
                .append(seldonAppSeldonPythonReference001, rhs.seldonAppSeldonPythonReference001).append(job, rhs.job)
                .append(seldonAppSeldonExample001, rhs.seldonAppSeldonExample001)
                .append(kubernetesPodName, rhs.kubernetesPodName)
                .append(seldonAppSklearnIris001, rhs.seldonAppSklearnIris001)
                .append(additionalProperties, rhs.additionalProperties).append(name, rhs.name)
                .append(seldonDeploymentId, rhs.seldonDeploymentId)
                .append(seldonAppMymodel2001, rhs.seldonAppMymodel2001).append(instance, rhs.instance)
                .append(seldonApp, rhs.seldonApp).isEquals();
    }

}
