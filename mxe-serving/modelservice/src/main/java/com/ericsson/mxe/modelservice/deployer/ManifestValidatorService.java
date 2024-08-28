package com.ericsson.mxe.modelservice.deployer;

import java.util.Objects;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.ericsson.mxe.backendservicescommon.exception.MxeBadRequestException;
import com.ericsson.mxe.backendservicescommon.exception.MxeConflictException;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.modelservice.aop.annotation.LogExecutionTime;
import com.ericsson.mxe.modelservice.config.properties.SeldonProperties;
import com.ericsson.mxe.modelservice.deployer.utils.MxeDeployerConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Status;

@Service
public class ManifestValidatorService {
    private static final Logger logger = LogManager.getLogger(ManifestValidatorService.class);

    private final KubernetesService kubernetesService;
    private final SeldonProperties seldonProperties;

    public ManifestValidatorService(final KubernetesService kubernetesService,
            final SeldonProperties seldonProperties) {
        this.kubernetesService = kubernetesService;
        this.seldonProperties = seldonProperties;
    }

    @LogExecutionTime
    public void validateNamespacedCustomSeldonObject(final String serviceName, final String apiVersion,
            final String kind, final Object m, boolean dryRun, String method) {

        String supportedApiVersion =
                "%s/%s".formatted(MxeDeployerConstants.SELDON_GROUP, MxeDeployerConstants.SELDON_VERSION);
        if (!supportedApiVersion.equals(apiVersion)) {
            throw new MxeBadRequestException(
                    "Invalid manifest: Supports SeldonDeployment group %s only".formatted(supportedApiVersion));
        }

        if (!seldonProperties.getCrd().getName().equals(kind)) {
            throw new MxeBadRequestException("Invalid manifest: Supports SeldonDeployment resource only");
        }

        if (dryRun) {
            if (HttpMethod.POST.name().equals(method)) {
                validateNamespacedCustomSeldonObjectDryRun(serviceName, m);
            }
            if (HttpMethod.PATCH.name().equals(method)) {
                validateNamespacedCustomSeldonObjectPatchDryRun(serviceName, m);
            }
        }
    }

    public void validateNamespacedCustomSeldonObjectDryRun(final String serviceName, final Object m) {
        try {
            kubernetesService.createNamespacedCustomObjectDryRun(MxeDeployerConstants.SELDON_GROUP,
                    MxeDeployerConstants.SELDON_VERSION, kubernetesService.getNamespace(),
                    MxeDeployerConstants.SELDON_PLURAL, m);
        } catch (ApiException e) {
            if (e.getCode() == HttpStatus.CONFLICT.value()) {
                throw new MxeConflictException("Model service \"" + serviceName + "\" is already running on cluster.");
            }
            String failureMessage = apiFailureMessage(e);
            throw new MxeInternalException(failureMessage, e);
        }
    }

    public void validateNamespacedCustomSeldonObjectPatchDryRun(final String serviceName, final Object m) {
        try {
            kubernetesService.replaceNamespacedCustomObjectDryRun(MxeDeployerConstants.SELDON_GROUP,
                    MxeDeployerConstants.SELDON_VERSION, kubernetesService.getNamespace(),
                    MxeDeployerConstants.SELDON_PLURAL, serviceName, m);
        } catch (ApiException e) {
            String failureMessage = apiFailureMessage(e);
            throw new MxeInternalException(failureMessage, e);
        }
    }

    /*
     * Private Methods - Incoming request read and Manifest Validators
     */
    private String apiFailureMessage(ApiException apiEx) {
        String failureMessage = apiEx.getMessage();
        try {
            V1Status v1Status = new ObjectMapper().readValue(apiEx.getResponseBody(), V1Status.class);
            failureMessage = Optional.ofNullable(v1Status).map(Objects::nonNull).map(status -> v1Status.getMessage())
                    .orElse(failureMessage);
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return failureMessage;
    }

}
