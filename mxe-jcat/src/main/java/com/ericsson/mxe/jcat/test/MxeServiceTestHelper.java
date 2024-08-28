package com.ericsson.mxe.jcat.test;

import com.ericsson.mxe.jcat.command.Commands;
import com.ericsson.mxe.jcat.command.CustomCommand;
import com.ericsson.mxe.jcat.command.MxeServiceCommand;
import com.ericsson.mxe.jcat.config.TestExecutionHost;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import com.ericsson.mxe.jcat.dto.MxeModel;
import com.ericsson.mxe.jcat.dto.autoscaling.AutoscalingData;
import com.ericsson.mxe.jcat.dto.autoscaling.AutoscalingMetric;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.*;
import static se.ericsson.jcat.fw.assertion.JcatAssertApi.saveAssertTrue;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.*;

public class MxeServiceTestHelper {

    static final String MODEL_UNKNOWN = UNKNOWN + ":" + MODEL_VERSION;
    static final String SERVICE_UNKNOWN = "unknownservice";
    static final String SUCCESS_MODEL_SERVICE_CREATE =
            "Success: Model service \"%s\" has been created with model%s \"%s\"%s, with %s instance%s%s%s";
    static final String SUCCESS_MODEL_SERVICE_MODIFY =
            "Success: Model service \"%s\" has been updated to use model%s \"%s\"%s, with %s instance%s%s";
    static final String SUCCESS_MODEL_SERVICE_MODIFY_NOT_NEEDED =
            "Success: Nothing to do, model service \"%s\" already contains model%s \"%s\"%s, and has %s instance%s%s";
    public static final String IN_DOMAIN = " in domain %s";
    private static final List<Integer> sleepSecBetweenRetries = Lists.newArrayList(2, 5, 10);

    private MxeServiceTestHelper() {}

    static Map<String, String> getModelFromList(List<Map<String, String>> modelList, String filterKey,
            String filterValue) {
        for (Map<String, String> model : modelList) {
            if (model.get(filterKey) != null && model.get(filterKey).equals(filterValue)) {
                return model;
            }
        }
        return null;
    }

    static List<Map<String, String>> getServiceList(final MxeCliDriver mxeCliDriver) {
        return getList(mxeCliDriver, Commands.mxeService(mxeCliDriver.getTestExecutionHost()).list());
    }

    static void serviceListInStep(final MxeCliDriver mxeCliDriver) {
        setTestStepBegin("List model services");
        getServiceList(mxeCliDriver);
        setTestStepEnd();
    }

    static void serviceCreate(final MxeCliDriver mxeCliDriver, String serviceName, List<MxeModel> mxeModels,
            Integer instance) {
        final MxeServiceCommand command =
                getMxeServiceCreateCommand(mxeCliDriver, serviceName, mxeModels, null, instance);
        String message = getServiceCreateResponseMessage(serviceName, mxeModels, null, instance, null);
        expectCommandToSucceed(mxeCliDriver, command, message);
    }

    static void serviceCreate(final MxeCliDriver mxeCliDriver, String serviceName, List<MxeModel> mxeModels,
            String domain, Integer instance) {
        final MxeServiceCommand command =
                getMxeServiceCreateCommand(mxeCliDriver, serviceName, mxeModels, domain, instance);
        String message = getServiceCreateResponseMessage(serviceName, mxeModels, domain, instance, null);
        expectCommandToSucceed(mxeCliDriver, command, message);
    }

    static void serviceCreate(final MxeCliDriver mxeCliDriver, String serviceName, List<MxeModel> mxeModels,
            AutoscalingData autoscalingData) {
        final MxeServiceCommand command =
                getMxeServiceCreateCommand(mxeCliDriver, serviceName, mxeModels, null, autoscalingData);
        String message = getServiceCreateResponseMessage(serviceName, mxeModels, null, null, autoscalingData);
        expectCommandToSucceed(mxeCliDriver, command, message);
    }

    private static String getServiceCreateResponseMessage(String serviceName, List<MxeModel> mxeModels, String domain,
            Integer instance, AutoscalingData autoscalingData) {
        List<String> modelIdsWithVersions = getModelIdsWithVersions(mxeModels);
        List<Double> weights = getWeights(mxeModels);
        String newIdsVersionsPattern = String.join(",", modelIdsWithVersions);
        String weightsPattern = !weights.isEmpty() ? Joiner.on(",").join(weights) : "0.5,0.5";
        String replicas = autoscalingData != null ? autoscalingData.minReplicas + "-" + autoscalingData.maxReplicas
                : instance.toString();
        String autoscaling = autoscalingData != null ? ", with autoscaling metrics " + autoscalingData.metrics : "";
        boolean hasMoreThanOneReplicas = autoscalingData != null || instance > 1;
        boolean moreThanOneModel = mxeModels.size() > 1;
        return String.format(SUCCESS_MODEL_SERVICE_CREATE, serviceName, moreThanOneModel ? "s" : "",
                newIdsVersionsPattern, moreThanOneModel ? " with weights " + weightsPattern : "", replicas,
                hasMoreThanOneReplicas ? "s" : "", autoscaling, getInDomainMessage(domain));
    }

    private static MxeServiceCommand getMxeServiceCreateCommand(MxeCliDriver mxeCliDriver, String serviceName,
            List<MxeModel> mxeModels, String domain, Integer instance) {
        List<String> modelIdsWithVersions = getModelIdsWithVersions(mxeModels);
        List<Double> weights = getWeights(mxeModels);
        return Commands.mxeService(mxeCliDriver.getTestExecutionHost()).create(serviceName, modelIdsWithVersions,
                domain, instance, weights);
    }

    private static MxeServiceCommand getMxeServiceCreateCommand(MxeCliDriver mxeCliDriver, String serviceName,
            List<MxeModel> mxeModels, String domain, AutoscalingData autoscalingData) {
        List<String> modelIdsWithVersions = getModelIdsWithVersions(mxeModels);
        List<Double> weights = getWeights(mxeModels);
        return Commands.mxeService(mxeCliDriver.getTestExecutionHost()).create(serviceName, modelIdsWithVersions,
                domain, autoscalingData, weights);
    }

    private static MxeServiceCommand getMxeServiceModifyCommand(MxeCliDriver mxeCliDriver, String serviceName,
            List<MxeModel> mxeModels, int instance) {
        List<String> modelIdsWithVersions = getModelIdsWithVersions(mxeModels);
        List<Double> weights = getWeights(mxeModels);
        return Commands.mxeService(mxeCliDriver.getTestExecutionHost()).modify(serviceName, modelIdsWithVersions,
                instance, weights);
    }

    private static List<String> getModelIdsWithVersions(List<MxeModel> mxeModels) {
        if (CollectionUtils.isEmpty(mxeModels)) {
            return Collections.emptyList();
        }
        return mxeModels.stream().filter(m -> m.getId() != null).map(m -> m.getId() + ":" + m.getVersion())
                .collect(Collectors.toList());
    }

    private static List<Double> getWeights(List<MxeModel> mxeModels) {
        if (CollectionUtils.isEmpty(mxeModels)) {
            return Collections.emptyList();
        }
        return mxeModels.stream().filter(m -> m.getWeight() != null).map(m -> m.getWeight())
                .collect(Collectors.toList());
    }

    static void serviceCreateAlreadyCreatedExpectFail(final MxeCliDriver mxeCliDriver, String serviceName,
            String modelId, String modelVersion, int instance) {
        serviceCreateAlreadyCreatedExpectFail(mxeCliDriver, serviceName,
                Lists.newArrayList(new MxeModel(modelId, modelVersion)), instance);
    }

    static void serviceCreateAlreadyCreatedExpectFail(final MxeCliDriver mxeCliDriver, String serviceName,
            List<MxeModel> mxeModels, int instance) {
        setTestStepBegin(getCreateStepName(serviceName, mxeModels, instance) + " which is already running");
        final MxeServiceCommand command =
                getMxeServiceCreateCommand(mxeCliDriver, serviceName, mxeModels, null, instance);
        expectCommandToFail(mxeCliDriver, command,
                "Error: Model service \"" + serviceName + "\" is already running on cluster \".*\".");
        setTestStepEnd();
    }

    static String serviceCreateInStep(final MxeCliDriver mxeCliDriver, String modelId, String modelVersion,
            int instance) {
        String serviceName = generateServiceName(modelId);
        serviceCreateInStep(mxeCliDriver, serviceName, modelId, modelVersion, instance);
        return serviceName;
    }

    static void serviceCreateInStep(final MxeCliDriver mxeCliDriver, String serviceName, String modelId,
            String modelVersion, int instance) {
        serviceCreateInStep(mxeCliDriver, serviceName, modelId, modelVersion, null, instance);
    }

    static void serviceCreateInStep(final MxeCliDriver mxeCliDriver, String serviceName, String modelId,
            String modelVersion, String domain, int instance) {
        setTestStepBegin("Create model service " + serviceName + " from model " + modelId + " with version "
                + modelVersion + " with instance number " + instance
                + (StringUtils.isEmpty(domain) ? "" : " in domain " + domain));
        List<MxeModel> mxeModels = Lists.newArrayList(new MxeModel(modelId, modelVersion));
        serviceCreate(mxeCliDriver, serviceName, mxeModels, domain, instance);
        setTestStepEnd();
    }

    static void serviceCreateInStep(final MxeCliDriver mxeCliDriver, String serviceName, String modelId,
            String modelVersion, int minReplicas, int maxReplicas, AutoscalingMetric autoscalingMetric) {
        setTestStepBegin("Create model service " + serviceName + " from model " + modelId + " with version "
                + modelVersion + " with autoscaling minReplicas " + minReplicas + " maxReplicas " + maxReplicas
                + " metric " + autoscalingMetric);
        List<MxeModel> mxeModels = Lists.newArrayList(new MxeModel(modelId, modelVersion));
        AutoscalingData autoscalingData = new AutoscalingData();
        autoscalingData.minReplicas = minReplicas;
        autoscalingData.maxReplicas = maxReplicas;
        autoscalingData.metrics = autoscalingMetric;
        serviceCreate(mxeCliDriver, serviceName, mxeModels, autoscalingData);
        setTestStepEnd();
    }


    static void serviceCreateInStep(final MxeCliDriver mxeCliDriver, String serviceName, List<MxeModel> mxeModels,
            int instance) {
        setTestStepBegin(getCreateStepName(serviceName, mxeModels, instance));
        serviceCreate(mxeCliDriver, serviceName, mxeModels, instance);
        setTestStepEnd();
    }

    private static String getCreateStepName(String serviceName, List<MxeModel> mxeModels, Integer instance) {
        StringBuilder sb = new StringBuilder();
        sb.append("Create model service").append(serviceName);
        for (int i = 0; i < mxeModels.size(); i++) {
            MxeModel mxeModel = mxeModels.get(i);
            if (i > 0) {
                sb.append(" and");
            }
            sb.append(" from model ").append(mxeModel.getId()).append(" with version ").append(mxeModel.getVersion());
            if (mxeModel.getWeight() != null) {
                sb.append(" with weight ").append(mxeModel.getWeight());
            }
        }
        sb.append(" with instance number ").append(instance);
        return sb.toString();
    }

    static void serviceCreateSingleServiceNonExistingModelExpectFail(final MxeCliDriver mxeCliDriver) {
        final String serviceName = "nonexistingname";
        final String modelId = "nonexistingmodel";
        final String modelVersion = "0.0.1";
        final int instance = 1;
        serviceCreateSingleServiceExpectFail(mxeCliDriver, serviceName, modelId, modelVersion, instance,
                "Error: Model with ID \"" + modelId + "\" and version \"" + modelVersion + "\" does not exist.*");
    }

    static void serviceCreateSingleServiceExpectFail(final MxeCliDriver mxeCliDriver, String serviceName,
            String modelId, String modelVersion, int instance, String expectedErrorMessage) {
        serviceCreateSingleServiceExpectFail(mxeCliDriver, serviceName, modelId, modelVersion, null, instance,
                expectedErrorMessage);
    }

    static void serviceCreateSingleServiceExpectFail(final MxeCliDriver mxeCliDriver, String serviceName,
            String modelId, String modelVersion, String domain, int instance, String expectedErrorMessage) {
        setTestStepBegin("Create mxe model service " + serviceName + " from model " + modelId + " with version "
                + modelVersion + " with instance number " + instance
                + (StringUtils.isEmpty(domain) ? "" : " in domain " + domain) + " expect fail");
        final CustomCommand customCommand;
        customCommand = Commands.mxeService(mxeCliDriver.getTestExecutionHost()).create(serviceName,
                getModelList(modelId + ":" + modelVersion), domain, instance, null);
        expectCommandToFail(mxeCliDriver, customCommand, expectedErrorMessage);
        setTestStepEnd();
    }

    static void serviceCreateABTestNonExistingModelExpectFail(final MxeCliDriver mxeCliDriver) {
        final String serviceName = "nonexistingname";
        final String modelId1 = "nonexistingmodel1";
        final String modelVersion1 = "0.0.1";
        final String modelId2 = "nonexistingmodel2";
        final String modelVersion2 = "0.0.1";
        List<MxeModel> mxeModels = new ArrayList<>();
        mxeModels.add(new MxeModel(modelId1, modelVersion1));
        mxeModels.add(new MxeModel(modelId2, modelVersion2));
        final int instance = 1;
        setTestStepBegin(getCreateStepName(serviceName, mxeModels, instance) + " which models do not exist");
        final CustomCommand customCommand;
        customCommand = getMxeServiceCreateCommand(mxeCliDriver, serviceName, mxeModels, null, instance);
        expectCommandToFail(mxeCliDriver, customCommand,
                "Error: Model with ID \"" + modelId1 + "\" and version \"" + modelVersion1 + "\" does not exist.*");
        setTestStepEnd();
    }

    static void serviceModifyABTestNonExistingModelExpectFail(final MxeCliDriver mxeCliDriver, String serviceName,
            String modelId, String modelVersion) {
        final String nonExistingModelId = "nonexistingmodel";
        List<MxeModel> mxeModels = new ArrayList<>();
        mxeModels.add(new MxeModel(modelId, modelVersion));
        mxeModels.add(new MxeModel(nonExistingModelId, modelVersion));
        final int instance = 1;
        setTestStepBegin(
                getModifyStepName(serviceName, mxeModels, instance) + " where the second model does not exist");
        final CustomCommand customCommand;
        customCommand = getMxeServiceModifyCommand(mxeCliDriver, serviceName, mxeModels, instance);
        expectCommandToFail(mxeCliDriver, customCommand, "Error: Model with ID \"" + nonExistingModelId
                + "\" and version \"" + modelVersion + "\" does not exist.*");
        setTestStepEnd();
    }

    static void serviceModifyABTestNonExistingServiceExpectFail(final MxeCliDriver mxeCliDriver,
            List<MxeModel> mxeModels) {
        final String serviceName = "nonexistingservice";
        final int instance = 1;
        setTestStepBegin(getModifyStepName(serviceName, mxeModels, instance) + " which does not exist");
        final CustomCommand customCommand;
        customCommand = getMxeServiceModifyCommand(mxeCliDriver, serviceName, mxeModels, instance);
        expectCommandToFail(mxeCliDriver, customCommand,
                "Error: Model service \"" + serviceName + "\" does not exist.*");
        setTestStepEnd();
    }

    static void serviceModifyABTestWithOneModelExpectFail(final MxeCliDriver mxeCliDriver, String serviceName,
            String modelId, String modelVersion) {
        List<MxeModel> mxeModels = new ArrayList<>();
        mxeModels.add(new MxeModel(modelId, modelVersion));
        final int instance = 1;
        setTestStepBegin(getModifyStepName(serviceName, mxeModels, instance) + " with one model");
        final CustomCommand customCommand;
        customCommand = getMxeServiceModifyCommand(mxeCliDriver, serviceName, mxeModels, instance);
        expectCommandToFail(mxeCliDriver, customCommand,
                "Error: Invalid service modify request: to modify model service \"" + serviceName
                        + "\", you must specify exactly 2 model\\(s\\).*");
        setTestStepEnd();
    }

    static void serviceDelete(final MxeCliDriver mxeCliDriver, String serviceName) {
        setTestStepBegin("Delete model service '" + serviceName + "'");
        CustomCommand customCommand = Commands.mxeService(mxeCliDriver.getTestExecutionHost()).delete(serviceName);
        expectCommandToSucceed(mxeCliDriver, customCommand,
                "Success: Model service \"" + serviceName + "\" has been deleted on cluster \".*\"");
        setTestStepEnd();
    }

    static void serviceDeleteNonExistingModelExpectFail(final MxeCliDriver mxeCliDriver) {
        setTestStepBegin("Delete non-existing model service");
        CustomCommand customCommand =
                Commands.mxeService(mxeCliDriver.getTestExecutionHost()).delete("nonexistingname");
        expectCommandToFail(mxeCliDriver, customCommand, "Error: Model service \"nonexistingname\" does not exist");
        setTestStepEnd();
    }

    static void serviceModifyModel(final MxeCliDriver mxeCliDriver, String serviceName, String targetModelId,
            String targetVersion, boolean willChange) {
        setTestStepBegin("Modify mxe model to " + targetModelId + " version " + targetVersion + " in model service "
                + serviceName);
        CustomCommand customCommand = Commands.mxeService(mxeCliDriver.getTestExecutionHost()).modify(serviceName,
                getModelList(targetModelId + ":" + targetVersion));
        String successMessage = String.format(SUCCESS_MODEL_SERVICE_MODIFY, serviceName, "",
                targetModelId + ":" + targetVersion, "", ".*", ".*", ".*");
        String nothingToDoMessage = String.format(SUCCESS_MODEL_SERVICE_MODIFY_NOT_NEEDED, serviceName, "",
                targetModelId + ":" + targetVersion, "", ".*", ".*", ".*");
        String successPattern = willChange ? successMessage : nothingToDoMessage;
        expectCommandToSucceedWithRetries(mxeCliDriver, customCommand, successPattern, sleepSecBetweenRetries);
        setTestStepEnd();
    }

    static void serviceModifyModel(final MxeCliDriver mxeCliDriver, String serviceName, Integer instance,
            boolean willChange) {
        serviceModifyModel(mxeCliDriver, serviceName, null, instance, willChange);
    }

    static void serviceModifyModel(final MxeCliDriver mxeCliDriver, String serviceName, List<MxeModel> mxeModels,
            boolean willChange) {
        serviceModifyModel(mxeCliDriver, serviceName, mxeModels, null, willChange);
    }

    static void serviceModifyModel(final MxeCliDriver mxeCliDriver, String serviceName, List<MxeModel> mxeModels,
            Integer instance, boolean willChange) {
        setTestStepBegin(getModifyStepName(serviceName, mxeModels, instance));

        List<String> modelIdsWithVersions = getModelIdsWithVersions(mxeModels);
        List<Double> weights = getWeights(mxeModels);

        CustomCommand customCommand = Commands.mxeService(mxeCliDriver.getTestExecutionHost()).modify(serviceName,
                modelIdsWithVersions, instance, weights);

        String newIdsVersionsPattern = !modelIdsWithVersions.isEmpty() ? String.join(",", modelIdsWithVersions) : ".*";
        String weightsPattern = !weights.isEmpty() ? Joiner.on(",").join(weights) : ".*";
        String successMessage = String.format(SUCCESS_MODEL_SERVICE_MODIFY, serviceName, ".*", newIdsVersionsPattern,
                " with weights " + weightsPattern, instance == null ? ".*" : instance.toString(),
                instance == null ? ".*" : instance > 1 ? "s" : "", ".*");
        String nothingToDoMessage = String.format(SUCCESS_MODEL_SERVICE_MODIFY_NOT_NEEDED, serviceName, ".*",
                newIdsVersionsPattern, " with weights " + weightsPattern, instance == null ? ".*" : instance.toString(),
                instance == null ? ".*" : instance > 1 ? "s" : "", ".*");;
        String successPattern = willChange ? successMessage : nothingToDoMessage;
        expectCommandToSucceed(mxeCliDriver, customCommand, successPattern);
        setTestStepEnd();
    }

    private static String getModifyStepName(String serviceName, List<MxeModel> mxeModels, Integer instance) {
        if (CollectionUtils.isEmpty(mxeModels) && instance == null) {
            setTestStepBegin("Modify mxe model: nothing to modify because every parameter is empty");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Modify mxe model to ");
        if (!CollectionUtils.isEmpty(mxeModels)) {
            for (int i = 0; i < mxeModels.size(); i++) {
                MxeModel mxeModel = mxeModels.get(i);
                if (mxeModel.getId() != null) {
                    if (i > 0) {
                        sb.append("and ");
                    }
                    sb.append(mxeModel.getId()).append(" version ").append(mxeModel.getVersion()).append(" ");
                }
                if (mxeModel.getWeight() != null) {
                    sb.append("weight ").append(mxeModel.getWeight()).append(" ");
                }
            }
        }
        if (!CollectionUtils.isEmpty(mxeModels) && instance != null) {
            sb.append("with ");
        }
        if (instance != null) {
            sb.append("instance number ").append(instance).append(" ");
        }
        sb.append("in model service ").append(serviceName);
        return sb.toString();
    }

    static void serviceModifyModelFailed(final MxeCliDriver mxeCliDriver, String serviceName) {
        setTestStepBegin("Mxe service modify model failure test");
        serviceModifyModelFailedSubStep(mxeCliDriver, "Modify service with unknown service name", SERVICE_UNKNOWN,
                getModelList(MODEL_UNKNOWN), "Error: Model service \"" + SERVICE_UNKNOWN + "\" does not exist");
        serviceModifyModelFailedSubStep(mxeCliDriver, "Modify service with unknown model id", serviceName,
                getModelList(MODEL_UNKNOWN),
                "Error: Model with ID \"" + UNKNOWN + "\" and version \"" + MODEL_VERSION + "\" does not exist.*");
        setTestStepEnd();
    }

    static void serviceModifyModelFailedInStep(final MxeCliDriver mxeCliDriver, String testStepName, String serviceName,
            List<String> targetModel, String errorPattern) {
        setTestStepBegin(testStepName);
        CustomCommand customCommand =
                Commands.mxeService(mxeCliDriver.getTestExecutionHost()).modify(serviceName, targetModel);
        expectCommandToFail(mxeCliDriver, customCommand, errorPattern);
        setTestStepEnd();
    }


    static void serviceModifyModelFailedSubStep(final MxeCliDriver mxeCliDriver, String testStepName,
            String serviceName, List<String> targetModel, String errorPattern) {
        setSubTestStep(testStepName);
        CustomCommand customCommand =
                Commands.mxeService(mxeCliDriver.getTestExecutionHost()).modify(serviceName, targetModel);
        expectCommandToFail(mxeCliDriver, customCommand, errorPattern);
    }

    static void serviceModifyInstances(final MxeCliDriver mxeCliDriver, String serviceName, int oldInstance,
            int newInstance) {
        setTestStepBegin(
                "Scaling mxe service " + serviceName + " from instance number " + oldInstance + " to " + newInstance);
        CustomCommand customCommand =
                Commands.mxeService(mxeCliDriver.getTestExecutionHost()).modify(serviceName, null, newInstance, null);
        String successMessage = String.format(SUCCESS_MODEL_SERVICE_MODIFY, serviceName, "", ".*:.*", "", newInstance,
                newInstance > 1 ? "s" : "", ".*");
        String nothingToDoMessage = String.format(SUCCESS_MODEL_SERVICE_MODIFY_NOT_NEEDED, serviceName, "", ".*:.*", "",
                newInstance, newInstance > 1 ? "s" : "", ".*");
        String successPattern = oldInstance != newInstance ? successMessage : nothingToDoMessage;
        expectCommandToSucceed(mxeCliDriver, customCommand, successPattern);
        setTestStepEnd();
    }

    static void serviceModifyAutoscaling(final MxeCliDriver mxeCliDriver, String serviceName, int minReplicas,
            int maxReplicas, AutoscalingMetric autoscalingMetric, boolean noChange) {
        setTestStepBegin("Modify mxe service " + serviceName + " to use minReplicas " + minReplicas + " maxReplicas "
                + maxReplicas + " with metric " + autoscalingMetric);
        AutoscalingData autoscalingData = new AutoscalingData();
        autoscalingData.minReplicas = minReplicas;
        autoscalingData.maxReplicas = maxReplicas;
        autoscalingData.metrics = autoscalingMetric;

        CustomCommand customCommand = Commands.mxeService(mxeCliDriver.getTestExecutionHost()).modify(serviceName, null,
                autoscalingData, null);
        String autoscaling = ", with autoscaling metrics " + autoscalingData.metrics;
        String successMessage = String.format(SUCCESS_MODEL_SERVICE_MODIFY, serviceName, "", ".*:.*", "",
                minReplicas + "-" + maxReplicas, "s", autoscaling);
        String nothingToDoMessage = String.format(SUCCESS_MODEL_SERVICE_MODIFY_NOT_NEEDED, serviceName, "", ".*:.*", "",
                minReplicas + "-" + maxReplicas, "s", autoscaling);
        String successPattern = noChange ? nothingToDoMessage : successMessage;
        expectCommandToSucceed(mxeCliDriver, customCommand, successPattern);
        setTestStepEnd();
    }

    static void serviceModifyInstancesFailed(final MxeCliDriver mxeCliDriver) {
        setTestStepBegin("Scaling test of an unknown model deployment");
        String notExistingServiceName = SERVICE_UNKNOWN;
        CustomCommand customCommand =
                Commands.mxeService(mxeCliDriver.getTestExecutionHost()).modify(notExistingServiceName, null, 1, null);
        expectCommandToFail(mxeCliDriver, customCommand,
                "Error: Model service \"" + notExistingServiceName + "\" does not exist");
        setTestStepEnd();
    }

    static void serviceModifyInstancesModel(final MxeCliDriver mxeCliDriver, String serviceName, String targetModelId,
            String targetVersion, int newInstance, boolean willChange) {
        setTestStepBegin("Modify mxe model to " + targetModelId + " version " + targetVersion + " and instances to "
                + newInstance + " in model service " + serviceName);
        CustomCommand customCommand = Commands.mxeService(mxeCliDriver.getTestExecutionHost()).modify(serviceName,
                getModelList(targetModelId + ":" + targetVersion), newInstance, null);
        String successMessage = String.format(SUCCESS_MODEL_SERVICE_MODIFY, serviceName, "",
                targetModelId + ":" + targetVersion, "", newInstance, newInstance > 1 ? "s" : "", ".*");
        String nothingToDoMessage = String.format(SUCCESS_MODEL_SERVICE_MODIFY_NOT_NEEDED, serviceName, "",
                targetModelId + ":" + targetVersion, "", newInstance, newInstance > 1 ? "s" : "", ".*");
        String successPattern = willChange ? successMessage : nothingToDoMessage;
        expectCommandToSucceed(mxeCliDriver, customCommand, successPattern);
        setTestStepEnd();
    }

    static void deleteAllServicesInSubstep(TestExecutionHost testExecutionHost) {
        setSubTestStep("Delete services");
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            List<Map<String, String>> serviceList = getServiceList(mxeCliDriver);
            setTestInfoExpandable("Services:", String.valueOf(serviceList));
            serviceList.forEach(map -> {
                mxeCliDriver.execute(
                        Commands.mxeService(testExecutionHost).delete(map.get(MxeTestHelper.HEADER_ELEMENT_NAME)));
            });

        } catch (Exception e) {
            setTestError(MxeTestHelper.ERROR_RESOURCE_RELEASE, e);
        }
        setSubTestStepEnd();
    }

    static void checkServiceNotFound(final MxeCliDriver mxeCliDriver, String serviceName) {
        checkServiceNotFound(mxeCliDriver, "Check service not found", serviceName);
    }

    static void checkServiceNotFound(final MxeCliDriver mxeCliDriver, String stepName, String serviceName) {
        setTestStepBegin(stepName);
        Map<String, String> service =
                getModelFromList(getServiceList(mxeCliDriver), MxeTestHelper.HEADER_ELEMENT_NAME, serviceName);
        saveAssertTrue("Service found which should not be exist", service == null || service.isEmpty());
        setTestStepEnd();
    }

    static void verifyServiceModel(final MxeCliDriver mxeCliDriver, String serviceName, String modelId,
            String modelVersion) {
        setTestStepBegin("Check service model");
        Map<String, String> model =
                getModelFromList(getServiceList(mxeCliDriver), MxeTestHelper.HEADER_ELEMENT_NAME, serviceName);
        saveAssertTrue("Model of service " + serviceName + " is not " + modelId + ":" + modelVersion,
                model != null && model.get(HEADER_ELEMENT_MODEL) != null
                        && model.get(HEADER_ELEMENT_MODEL).endsWith(modelId + ":" + modelVersion));
        setTestStepEnd();
    }

    static void verifyServiceModel(final MxeCliDriver mxeCliDriver, String serviceName, List<MxeModel> mxeModels) {
        setTestStepBegin("Check service model");
        saveAssertTrue("At least two models are required as parameter", mxeModels.size() >= 2);
        List<String> modelIdsAndVersions = getModelIdsWithVersions(mxeModels);
        Map<String, String> model =
                getModelFromList(getServiceList(mxeCliDriver), MxeTestHelper.HEADER_ELEMENT_NAME, serviceName);
        saveAssertTrue("Model of service " + serviceName + " is not " + modelIdsAndVersions,
                checkModel(model, HEADER_ELEMENT_MODEL_A, mxeModels.get(0)));
        saveAssertTrue("Model of service " + serviceName + " is not " + modelIdsAndVersions,
                checkModel(model, HEADER_ELEMENT_MODEL_B, mxeModels.get(1)));
        setTestStepEnd();
    }

    static void verifyServiceDomain(final MxeCliDriver mxeCliDriver, String serviceName, String serviceDomain) {
        setTestStepBegin("Check service domain of service " + serviceName);
        Map<String, String> service =
                getModelFromList(getServiceList(mxeCliDriver), MxeTestHelper.HEADER_ELEMENT_NAME, serviceName);
        if (serviceDomain.isEmpty()) {
            saveAssertTrue("Domain of service " + serviceName + " is not empty",
                    service != null && service.get(HEADER_ELEMENT_DOMAIN) == null);
        } else {
            saveAssertTrue("Domain of service " + serviceName + " is not " + serviceDomain,
                    service != null && service.get(HEADER_ELEMENT_DOMAIN) != null
                            && service.get(HEADER_ELEMENT_DOMAIN).equals(serviceDomain));
        }
        setTestStepEnd();
    }

    private static boolean checkModel(Map<String, String> model, String header, MxeModel mxeModel) {
        return model != null && model.get(header) != null
                && model.get(header).endsWith(mxeModel.getId() + ":" + mxeModel.getVersion());
    }

    static void verifyServiceEndpoint(final MxeCliDriver mxeCliDriver, String serviceName) {
        setTestStepBegin("Check endpoint");
        String expectedEndpoint = "<mxe-host>/model-endpoints/" + serviceName;
        Map<String, String> model =
                getModelFromList(getServiceList(mxeCliDriver), MxeTestHelper.HEADER_ELEMENT_NAME, serviceName);
        saveAssertTrue("Endpoint of service " + serviceName + " is not " + expectedEndpoint,
                model != null && model.get(HEADER_ELEMENT_ENDPOINT) != null
                        && model.get(HEADER_ELEMENT_ENDPOINT).equals(expectedEndpoint));
        setTestStepEnd();
    }

    static void verifyServiceInstanceNumber(final MxeCliDriver mxeCliDriver, String serviceName, Integer instance) {
        verifyServiceInstanceNumber(mxeCliDriver, serviceName, instance.toString());
    }

    static void verifyServiceInstanceNumber(final MxeCliDriver mxeCliDriver, String serviceName, String instance) {
        setTestStepBegin("Check instance number");
        Map<String, String> model =
                getModelFromList(getServiceList(mxeCliDriver), MxeTestHelper.HEADER_ELEMENT_NAME, serviceName);
        saveAssertTrue("Instance number of service " + serviceName + " is not " + instance, model != null
                && model.get(HEADER_ELEMENT_INSTANCES) != null && model.get(HEADER_ELEMENT_INSTANCES).equals(instance));
        setTestStepEnd();
    }

    static void verifyServiceModelWeights(final MxeCliDriver mxeCliDriver, String serviceName,
            List<MxeModel> mxeModels) {
        setTestStepBegin("Check weights");
        List<Double> weights = getWeights(mxeModels);
        if (weights.size() == 1) {
            weights.add(1 - weights.get(0));
        }
        String weightsString = Joiner.on(",").join(weights);
        Map<String, String> model =
                getModelFromList(getServiceList(mxeCliDriver), MxeTestHelper.HEADER_ELEMENT_NAME, serviceName);
        saveAssertTrue("Weights of models in service " + serviceName + " is not " + weightsString,
                model != null && model.get(HEADER_ELEMENT_WEIGHTS) != null
                        && weightsString.equals(model.get(HEADER_ELEMENT_WEIGHTS)));
        setTestStepEnd();
    }

    static void verifyServiceAutoScalingMetric(final MxeCliDriver mxeCliDriver, String serviceName,
            AutoscalingMetric autoscalingMetric) {
        setTestStepBegin("Check autoscaling configuration");
        Map<String, String> model =
                getModelFromList(getServiceList(mxeCliDriver), MxeTestHelper.HEADER_ELEMENT_NAME, serviceName);
        saveAssertTrue("Autoscaling metric of " + serviceName + " is not " + autoscalingMetric,
                model != null && model.get(HEADER_ELEMENT_AUTOSCALING) != null
                        && model.get(HEADER_ELEMENT_AUTOSCALING).equals(autoscalingMetric.toString()));
        setTestStepEnd();
    }

    static void waitUntilServiceStatusIs(MxeCliDriver mxeCliDriver, String serviceName, String expectedStatus,
            Duration timeOut) {
        MxeServiceCommand command = Commands.mxeService(mxeCliDriver.getTestExecutionHost()).list();
        waitUntilRunningDeploymentStatusIs(mxeCliDriver, serviceName, expectedStatus, timeOut,
                MxeTestHelper.HEADER_ELEMENT_NAME, command);
    }

    public static List<String> getModelList(String... models) {
        return Stream.of(models).collect(Collectors.toList());
    }

    public static String generateServiceName(String modelId) {
        return modelId.replaceAll("[^\\.a-zA-Z0-9]", "").replaceAll("\\.", "-").toLowerCase();
    }

    static String getInDomainMessage(String domain) {
        return StringUtils.isEmpty(domain) ? "" : String.format(IN_DOMAIN, domain);
    }

}
