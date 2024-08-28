package com.ericsson.mxe.modelservice.deployer.utils;

import java.util.Objects;
import com.ericsson.mxe.backendservicescommon.exception.MxeBadRequestException;
import com.ericsson.mxe.backendservicescommon.exception.MxeConflictException;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.modelservice.deployer.domain.MxeDeployerRequestData;
import com.ericsson.mxe.modelservice.dto.AutoscalingData;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentType;

public class MxeDeployerParameterValidator {

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

    static void validateReplicas(MxeDeployerRequestData mxeDeployerRequestData) {
        AutoscalingData autoScaling = mxeDeployerRequestData.getAutoScaling();
        if (!Objects.isNull(autoScaling)) {
            if (autoScaling.minReplicas < 1) {
                throw new MxeBadRequestException(
                        INVALID_NUMBER_OF.formatted("minReplicas", autoScaling.minReplicas, "minReplicas", 1));
            }
            if (autoScaling.maxReplicas < autoScaling.minReplicas) {
                throw new MxeBadRequestException(INVALID_NUMBER_OF.formatted("maxReplicas", autoScaling.maxReplicas,
                        "maxReplicas", "number of minReplicas"));
            }
        } else {
            int replicas = mxeDeployerRequestData.getReplicas();
            if (replicas < 1) {
                throw new MxeBadRequestException(INVALID_NUMBER_OF.formatted("replicas", replicas, "replicas", 1));
            }
        }

    }

    private static void validateModelsCreate(int requiredNumberOfModels, int numberOfModelsInRequest) {
        if (requiredNumberOfModels != numberOfModelsInRequest) {
            throw new MxeInternalException("Invalid number of models provided: " + numberOfModelsInRequest
                    + ". Expected: " + requiredNumberOfModels);
        }
    }
}
