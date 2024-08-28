package com.ericsson.mxe.jcat.driver.rest;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import java.util.Map;

public class MxeRequestFactory {

    private static final String V_1 = "v1";
    private static final String MODELS = "models";
    private static final String TRAINING_PACKAGES = "training-packages";
    private static final String TRAINING_JOBS = "training-jobs";
    private static final String MODEL_SERVICES = "model-services";
    private static final String FLOWS = "flows";
    private static final String FLOW_DEPLOYMENTS = "flow-deployments";
    private static final String RESULT = "result";
    private static final String LIST_TRAINING_JOBS_WITH_PACKAGE_ID = "training-jobs?packageId=%s";
    private static final String LIST_TRAINING_JOBS_WITH_PACKAGE_ID_AND_PACKAGE_VERSION =
            "training-jobs?packageId=%s&packageVersion=%s";
    private static final String TYPE_APPLICATION_JSON = ";type=application/json";
    private static final String START_TRAINING_JOBS_WITH_PACKAGE_ID_AND_PACKAGE_VERSION =
            "{\"packageId\":\"%s\",\"packageVersion\":\"%s\"}" + TYPE_APPLICATION_JSON;
    private static final String START_MODEL_SERVICE_WITH_PACKAGE_ID_AND_PACKAGE_VERSION =
            "{\"type\":\"model\",\"replicas\":3,\"models\":[{\"id\":\"%s\",\"version\":\"%s\"}]}"
                    + TYPE_APPLICATION_JSON;
    private static final String SCALE_MODEL_SERVICE = "{\"replicas\":%s}" + TYPE_APPLICATION_JSON;
    private static final String UPGRADE_MODEL_SERVICE = "{\"models\":[{\"version\":\"%s\"}]}" + TYPE_APPLICATION_JSON;
    private static final String ONBOARD_MODEL_FROM_EXTERNAL_DOCKER_REGISTRY =
            "{\"id\":\"%s\",\"author\":\"%s\",\"title\":\"%s\",\"description\":\"%s\",\"version\":\"%s\",\"image\":\"%s\",\"icon\":\"%s\"}"
                    + TYPE_APPLICATION_JSON;
    private static final String ONBOARD_NIFI_FLOW = "{\"name\":%s}" + TYPE_APPLICATION_JSON;
    private static final String SOURCEFILE = "sourcefile";
    private static final String ARCHIVEFILE = "archivefile";
    private static final String TRAININGPACKAGE = "trainingpackage";
    private static final String FILE = "file";
    private static final String MODEL_ENDPOINTS = "model-endpoints";
    private static final String MODEL_FEED = "{\"data\":{\"ndarray\": [%s]}}" + TYPE_APPLICATION_JSON;

    public static Request<String> onboardModelFromExternalDockerRegistry(String id, String author, String title,
            String description, String version, String image, String icon) {
        return Request.create(RestHelper.link(V_1, MODELS), HttpMethod.POST, String.class, String.format(
                ONBOARD_MODEL_FROM_EXTERNAL_DOCKER_REGISTRY, id, author, title, description, version, image, icon));
    }

    public static Request<String> onboardModelFromSourceFiles(String filePath) {
        return Request.create(RestHelper.link(V_1, MODELS), HttpMethod.POST, String.class, SOURCEFILE,
                new FileSystemResource(filePath));
    }

    public static Request<String> onboardModelFromDockerImageArchive(String filePath) {
        return Request.create(RestHelper.link(V_1, MODELS), HttpMethod.POST, String.class, ARCHIVEFILE,
                new FileSystemResource(filePath));
    }

    public static Request<String> listOnboardedModels() {
        return Request.create(RestHelper.link(V_1, MODELS), HttpMethod.GET, String.class);
    }

    public static Request<String> deleteModel(String id, String version) {
        return Request.create(RestHelper.link(V_1, MODELS, id, version), HttpMethod.DELETE, String.class);
    }

    /// ###///###///###///###///###///###///###///###///###///###///###///###///###///###///
    public static Request<String> onboardTrainingPackageFromSourceFiles(String filePath) {
        return Request.create(RestHelper.link(V_1, TRAINING_PACKAGES), HttpMethod.POST, String.class, TRAININGPACKAGE,
                new FileSystemResource(filePath));
    }

    public static Request<String> listOnboardedTrainingPackages() {
        return Request.create(RestHelper.link(V_1, TRAINING_PACKAGES), HttpMethod.GET, String.class);
    }

    public static Request<String> listOnboardedTrainingPackage(String id, String version) {
        return Request.create(RestHelper.link(V_1, TRAINING_PACKAGES, id, version), HttpMethod.GET, String.class);
    }

    public static Request<String> deleteTrainingPackage(String id, String version) {
        return Request.create(RestHelper.link(V_1, TRAINING_PACKAGES, id, version), HttpMethod.DELETE, String.class);
    }

    /// ###///###///###///###///###///###///###///###///###///###///###///###///###///###///
    public static Request<String> startingTrainingJobFromAnOnboardedTrainingPackage(String packageId,
            String packageVersion) {
        return Request.create(RestHelper.link(V_1, TRAINING_JOBS), HttpMethod.POST, String.class,
                String.format(START_TRAINING_JOBS_WITH_PACKAGE_ID_AND_PACKAGE_VERSION, packageId, packageVersion));
    }

    public static Request<String> listTrainingJobs() {
        return Request.create(RestHelper.link(V_1, TRAINING_JOBS), HttpMethod.GET, String.class);
    }

    public static Request<String> listTrainingJob(String id) {
        return Request.create(RestHelper.link(V_1, TRAINING_JOBS, id), HttpMethod.GET, String.class);
    }

    public static Request<String> listTrainingJobs(String packageId) {
        return Request.create(RestHelper.link(V_1, String.format(LIST_TRAINING_JOBS_WITH_PACKAGE_ID, packageId)),
                HttpMethod.GET, String.class);
    }

    public static Request<String> listTrainingJobs(String packageId, String packageVersion) {
        return Request.create(RestHelper.link(V_1,
                String.format(LIST_TRAINING_JOBS_WITH_PACKAGE_ID_AND_PACKAGE_VERSION, packageId, packageVersion)),
                HttpMethod.GET, String.class);
    }

    public static Request<String> downloadTrainingJobResult(String id) {
        return Request.create(RestHelper.link(V_1, TRAINING_JOBS, id, RESULT), HttpMethod.GET, String.class);
    }

    public static Request<String> deleteTrainingJob(String id) {
        return Request.create(RestHelper.link(V_1, TRAINING_JOBS, id), HttpMethod.DELETE, String.class);
    }

    public static Request<String> deleteTrainingJobs(String packageId, String packageVersion) {
        return Request.create(RestHelper.link(V_1,
                String.format(LIST_TRAINING_JOBS_WITH_PACKAGE_ID_AND_PACKAGE_VERSION, packageId, packageVersion)),
                HttpMethod.DELETE, String.class);
    }

    /// ###///###///###///###///###///###///###///###///###///###///###///###///###///###///
    public static Request<String> startModelService(String id, String version) {
        return Request.create(RestHelper.link(V_1, MODEL_SERVICES), HttpMethod.POST, String.class,
                String.format(START_MODEL_SERVICE_WITH_PACKAGE_ID_AND_PACKAGE_VERSION, id, version));
    }

    public static Request<String> changeRunningModelServiceScale(String modelName, String scale) {
        return Request.create(RestHelper.link(V_1, MODEL_SERVICES, modelName), HttpMethod.PATCH, String.class,
                String.format(SCALE_MODEL_SERVICE, scale));
    }

    public static Request<String> changeRunningModelServiceUpgrade(String modelName, String version) {
        return Request.create(RestHelper.link(V_1, MODEL_SERVICES, modelName), HttpMethod.PATCH, String.class,
                String.format(UPGRADE_MODEL_SERVICE, version));
    }

    public static Request<String> listRunningModelServices() {
        return Request.create(RestHelper.link(V_1, MODEL_SERVICES), HttpMethod.GET, String.class);
    }

    public static Request<String> stopModelService(String name) {
        return Request.create(RestHelper.link(V_1, MODEL_SERVICES, name), HttpMethod.DELETE, String.class);
    }

    /// ###///###///###///###///###///###///###///###///###///###///###///###///###///###///
    public static Request<String> onboardFlows(String filePath, String flowName) {
        return Request.create(RestHelper.link(V_1, FLOWS), HttpMethod.POST, String.class, FILE,
                new FileSystemResource(filePath), String.format(ONBOARD_NIFI_FLOW, flowName));
    }

    public static Request<String> listFlows() {
        return Request.create(RestHelper.link(V_1, FLOWS), HttpMethod.GET, String.class);
    }

    public static Request<String> deleteFlow(String flowName) {
        return Request.create(RestHelper.link(V_1, FLOWS, flowName), HttpMethod.DELETE, String.class);
    }

    /// ###///###///###///###///###///###///###///###///###///###///###///###///###///###///
    public static Request<String> deployFlowDeployment(Map<String, String> parameters) {
        return Request.create(RestHelper.link(V_1, FLOW_DEPLOYMENTS), HttpMethod.POST, String.class,
                ToStringBuilder.reflectionToString(parameters, ToStringStyle.JSON_STYLE) + TYPE_APPLICATION_JSON);
    }

    public static Request<String> listFlowDeployments() {
        return Request.create(RestHelper.link(V_1, FLOW_DEPLOYMENTS), HttpMethod.GET, String.class);
    }

    public static Request<String> deleteFlowDeployment(String flowDeploymentName) {
        return Request.create(RestHelper.link(V_1, FLOW_DEPLOYMENTS, flowDeploymentName), HttpMethod.DELETE,
                String.class);
    }

    public static Request<String> feedModel(String modelName, String feed) {
        return Request.create(RestHelper.link(MODEL_ENDPOINTS, modelName), HttpMethod.POST, String.class,
                String.format(MODEL_FEED, feed));
    }
}
