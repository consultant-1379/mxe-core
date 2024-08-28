package com.ericsson.mxe.modelservice.deployer.utils;

import java.util.List;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import com.ericsson.mxe.backendservicescommon.dto.status.PackageStatus;
import com.ericsson.mxe.backendservicescommon.exception.MxeConflictException;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.modelservice.controller.input.RequestModelData;
import com.ericsson.mxe.modelservice.controller.input.SeldonManifestRequest;
import com.ericsson.mxe.modelservice.deployer.domain.MxeDeployerModelInfo;
import com.ericsson.mxe.modelservice.deployer.domain.MxeDeployerRequestData;
import com.ericsson.mxe.modelservice.modelcatalog.ModelCatalogResolver;
import com.ericsson.mxe.modelservice.modelcatalog.dto.ModelPackageData;

public class MxeDeployerUtils {

    public static String getTypeFromRequest(RequestModelData modelData) {
        return StringUtils.defaultString(Optional.ofNullable(modelData).map(m -> m.modelType).orElse(null));
    }

    /*
     * CREATE DEPLOYMENT
     */
    public static void validateCreateRequest(MxeDeployerRequestData mxeDeployerRequestData,
            final ModelCatalogResolver modelCatalogResolver) {

        List<MxeDeployerModelInfo> models = mxeDeployerRequestData.getModels();
        MxeDeployerParameterValidator.validateReplicas(mxeDeployerRequestData);
        MxeDeployerParameterValidator.validateType(mxeDeployerRequestData.getType(), models.size());

        checkModelPackageStatus(mxeDeployerRequestData, modelCatalogResolver);

    }

    public static void finalCheckCreateDeploymentManifest(SeldonManifestRequest manifestRequest) {
        // 4. Optional - Check final manifest data
    }

    /*
     * PATCH DEPLOYMENT
     */

    public static void validatePatchRequest(MxeDeployerRequestData mxeDeployerRequestData) {

        List<MxeDeployerModelInfo> models = mxeDeployerRequestData.getModels();
        MxeDeployerParameterValidator.validateReplicas(mxeDeployerRequestData);
        MxeDeployerParameterValidator.validateType(mxeDeployerRequestData.getType(), models.size());

    }

    public static void isPatchRequired(MxeDeployerRequestData deployerRequestData, String userId, String userName) {
        // # TO_DO: Validate the request
        // 1. Get the model service data from k8s cluster - api server (with access control) - A CURRENT
        // 2. Get the model service data from incoming request - incoming manifest request - B NEW
        // 3. Convert both (A) and (B) to comparable format (domain object
        // 4. Check for modifications ->
        // 5. Trigger request if modifications found

    }

    public static void populatePatchMxeDataIntoManifest(SeldonManifestRequest manifestRequest,
            MxeDeployerRequestData deployerRequestData) {
        // # REVISIT - whether required for patch
        // Enhance the input manifest i. Prepopulate the additional data - metadata, additional fields

    }

    public static void finalCheckPatchDeploymentManifest(SeldonManifestRequest manifestRequest) {
        // 4. Optional - Check final manifest data
    }

    // Private methods
    private static void checkModelPackageStatus(final MxeDeployerRequestData mxeDeployerRequestData,
            final ModelCatalogResolver modelCatalogResolver) {
        final List<String> issues = Lists.newArrayList();

        for (MxeDeployerModelInfo mxeModelInfo : mxeDeployerRequestData.getModels()) {
            final ModelPackageData modelPackageData =
                    modelCatalogResolver.getPackageForImage(mxeModelInfo.image).orElseThrow(() -> {
                        throw new MxeInternalException(
                                "Could not perform operation for model service, Model with Image \"%s\" is not onboarded"
                                        .formatted(mxeModelInfo.image));
                    });

            if (modelPackageData.status != PackageStatus.Available) {
                issues.add(StringUtils.join("[", modelPackageData.id, ":", modelPackageData.status, "]"));
            }
        }

        if (CollectionUtils.isNotEmpty(issues)) {
            throw new MxeConflictException(
                    "Could not perform operation for model service " + mxeDeployerRequestData.getName()
                            + ", invalid model state (" + StringUtils.join(issues, ", ") + ")");
        }
    }
}
