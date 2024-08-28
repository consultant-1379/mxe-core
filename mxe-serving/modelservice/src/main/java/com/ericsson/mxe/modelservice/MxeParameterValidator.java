package com.ericsson.mxe.modelservice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import com.ericsson.mxe.backendservicescommon.exception.MxeBadRequestException;
import com.ericsson.mxe.backendservicescommon.exception.MxeConflictException;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.backendservicescommon.exception.MxePackageNotFoundException;
import com.ericsson.mxe.modelservice.dto.AutoscalingData;
import com.ericsson.mxe.modelservice.dto.AutoscalingMetric;
import com.ericsson.mxe.modelservice.dto.MxeModelData;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentType;
import com.ericsson.mxe.modelservice.modelcatalog.ModelCatalogResolver;
import com.ericsson.mxe.modelservice.seldondeployment.MxeModelInfo;
import com.ericsson.mxe.modelservice.modelcatalog.DockerImage;

public class MxeParameterValidator {
    // later someday this should come from the package data
    private static final String REST = "REST";
    private static final String INVALID_SERVICE_MODIFY_REQUEST = "Invalid service modify request: %s";
    private static final int DEFAULT_REPLICA = 1;
    private static final Double DEFAULT_WEIGHT = 0.5;
    private static final int NUMBER_OF_DIGITS_IN_WEIGHT = 5;
    private static final String WEIGHT = "weight";
    static final String WEIGHT_IS_NOT_BETWEEN_0_AND_1_ERROR_MESSAGE =
            "Value of parameter '" + WEIGHT + "' is not between 0 and 1: '%s'.";
    static final String SUM_OF_WEIGHTS_SHOULD_BE_ONE = "Sum of defined weights is %s, it should be 1.";
    static final String MORE_THAN_ONE_WEIGHT_IS_MISSING =
            "Weight parameter has not been defined for %s models. You can only leave one empty.";
    static final String SUM_OF_DEFINED_WEIGHTS_IS_MORE_THAN_ONE = "Sum of defined weights is %s, which is more than 1.";
    static final String INVALID_NUMBER_OF = "Invalid number of %s: %s. Number of %s can not be less than %s.";

    static MxeModelDeploymentType validateType(MxeModelDeploymentType type, int numberOfModels) {
        switch (type) {
            case MODEL:
                validateModelsCreate(1, numberOfModels);
                return type;
            case STATIC:
                validateModelsCreate(2, numberOfModels);
                return type;
            default:
                throw new IllegalArgumentException("Request type is not supported: " + type);
        }
    }

    private static void validateModelsCreate(int requiredNumberOfModels, int numberOfModelsInRequest) {
        if (requiredNumberOfModels != numberOfModelsInRequest) {
            throw new MxeInternalException("Invalid number of models provided: " + numberOfModelsInRequest
                    + ". Expected: " + requiredNumberOfModels);
        }
    }

    static List<MxeModelInfo> validateModelsPatch(String serviceName, List<MxeModelData> models,
            int numberOfModelsInService, ModelCatalogResolver resolver) {
        if (models.stream().allMatch(model -> StringUtils.isEmpty(model.id) && StringUtils.isEmpty(model.version))) {
            return Collections.emptyList();
        }
        if (models.size() != numberOfModelsInService) {
            throw new MxeInternalException(INVALID_SERVICE_MODIFY_REQUEST
                    .formatted("to modify model service \"%s\", you must specify exactly %d model(s)"
                            .formatted(serviceName, numberOfModelsInService)));
        }
        return models.stream().map(mxeModelData -> mxePackage2Model(resolver, mxeModelData))
                .collect(Collectors.toList());
    }

    public static MxeModelInfo mxePackage2Model(ModelCatalogResolver resolver, MxeModelData mxeModel) {
        DockerImage dockerImage = resolver.getImageForPackage(mxeModel.id, mxeModel.version).orElseThrow(
                () -> new MxePackageNotFoundException("Model with ID \"" + mxeModel.id + "\" and version \""
                        + mxeModel.version + "\" does not exist, or there is no permission to use it"));
        return new MxeModelInfo(dockerImage.getTag(), REST, dockerImage.getPullSecretName());
    }

    static Integer validateReplicas(Integer replicas, boolean useDefaultReplicaIfEmpty) {
        if (Objects.isNull(replicas)) {
            if (useDefaultReplicaIfEmpty) {
                return DEFAULT_REPLICA;
            }
            return replicas;
        }
        if (replicas < 1) {
            throw new MxeInternalException(INVALID_NUMBER_OF.formatted("replicas", replicas, "replicas", 1));
        }
        return replicas;
    }

    static AutoscalingData validateAutoScaling(AutoscalingData autoScaling) {
        if (Objects.isNull(autoScaling)) {
            return autoScaling;
        }
        if (autoScaling.minReplicas < 1) {
            throw new MxeInternalException(
                    INVALID_NUMBER_OF.formatted("minReplicas", autoScaling.minReplicas, "minReplicas", 1));
        }
        if (autoScaling.maxReplicas < autoScaling.minReplicas) {
            throw new MxeInternalException(INVALID_NUMBER_OF.formatted("maxReplicas", autoScaling.maxReplicas,
                    "maxReplicas", "number of minReplicas"));
        }
        for (AutoscalingMetric metric : autoScaling.metrics) {
            if (metric.targetAverageValue < 1) {
                throw new MxeInternalException(
                        INVALID_NUMBER_OF.formatted(metric.name.getResourceName() + " targetAverageValue",
                                metric.targetAverageValue, metric.name.getResourceName() + " targetAverageValue", "1"));
            }
        }
        return autoScaling;
    }

    static List<Double> validateWeights(List<MxeModelData> models, boolean useDefaultWeightIfEmpty,
            int numberOfModelsInService) {
        if (numberOfModelsInService == 1) {
            return Lists.newArrayList();
        }
        List<Double> weights = models.stream().map(mxeModelData -> mxeModelData.weight).collect(Collectors.toList());
        if (weights.size() == 1) {
            weights.add(null);
        }
        weights = fillDefaultValuesIfNeeded(weights, useDefaultWeightIfEmpty);
        weights.forEach(weight -> validateWeightIsBetweenZeroAndOne(weight));
        weights = checkWeightsAndCalculateMissingWeightIfNeededAndPossible(weights);
        return weights;
    }

    private static List<Double> fillDefaultValuesIfNeeded(List<Double> weights, boolean useDefaultWeightIfEmpty) {
        if (hasNoWeightDefined(weights) && useDefaultWeightIfEmpty) {
            return weights.stream().map(weight -> DEFAULT_WEIGHT).collect(Collectors.toList());
        }
        return weights;
    }

    static void validateWeightIsBetweenZeroAndOne(Double weight) {
        if (Objects.nonNull(weight) && (weight < 0 || weight > 1)) {
            throw new MxeInternalException(WEIGHT_IS_NOT_BETWEEN_0_AND_1_ERROR_MESSAGE.formatted(weight));
        }
    }

    static List<Double> checkWeightsAndCalculateMissingWeightIfNeededAndPossible(List<Double> weights) {
        if (weights.size() < 2) {
            return weights;
        }
        if (hasNoWeightDefined(weights)) {
            return weights;
        }
        weights = roundWeights(weights);
        if (hasAllWeightDefined(weights)) {
            checkSumOfWeights(weights);
            return weights;
        }
        checkIfOnlyOneWeightIsNotDefined(weights);
        return fillMissingWeight(weights);
    }

    private static boolean hasNoWeightDefined(List<Double> weights) {
        return weights.stream().allMatch(Objects::isNull);
    }

    private static boolean hasAllWeightDefined(List<Double> weights) {
        return weights.stream().allMatch(Objects::nonNull);
    }

    private static void checkSumOfWeights(List<Double> weights) {
        Double sumOfDefinedWeights = getSumOfWeights(weights);
        if (sumOfDefinedWeights != 1) {
            throw new MxeInternalException(SUM_OF_WEIGHTS_SHOULD_BE_ONE.formatted(sumOfDefinedWeights));
        }
    }

    private static void checkIfOnlyOneWeightIsNotDefined(List<Double> weights) {
        int numberOfNullWeights = weights.stream().filter(Objects::isNull).collect(Collectors.toList()).size();
        if (numberOfNullWeights > 1) {
            throw new MxeInternalException(MORE_THAN_ONE_WEIGHT_IS_MISSING.formatted(numberOfNullWeights));
        }
    }

    private static List<Double> fillMissingWeight(List<Double> weights) {
        Double sumOfDefinedWeights =
                getSumOfWeights(weights.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        if (sumOfDefinedWeights > 1) {
            throw new MxeInternalException(SUM_OF_DEFINED_WEIGHTS_IS_MORE_THAN_ONE.formatted(sumOfDefinedWeights));
        }
        return weights.stream().map(weight -> {
            if (Objects.isNull(weight)) {
                return roundWeight(1 - sumOfDefinedWeights);
            } else {
                return weight;
            }
        }).collect(Collectors.toList());
    }

    private static List<Double> roundWeights(List<Double> weights) {
        return weights.stream().map(weight -> roundWeight(weight)).collect(Collectors.toList());
    }

    public static Double roundWeight(Double weight) {
        if (Objects.isNull(weight)) {
            return weight;
        }
        return BigDecimal.valueOf(weight).setScale(NUMBER_OF_DIGITS_IN_WEIGHT, RoundingMode.HALF_UP).doubleValue();
    }

    private static Double getSumOfWeights(List<Double> weights) {
        return weights.stream().mapToDouble(Double::doubleValue).sum();
    }

    public static String createDeploymentName(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9\\-]", "-").replaceAll("-*$", "");
    }

    public static String getDomainOrDefault(String domain) {
        return StringUtils.defaultString(domain);
    }
}
