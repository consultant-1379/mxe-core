/*******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.mxe.modelcatalogueservice.services;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ericsson.mxe.backendservicescommon.dto.status.PackageStatus;
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.modelcatalogueservice.dto.request.UpdateModelRequest;
import com.ericsson.mxe.modelcatalogueservice.dto.response.ModelCatalogueServiceResponse;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1JobCondition;

@Service
public class PackagerJobEventHandlerService implements ResourceEventHandler<V1Job> {

    private static final Logger LOG = LoggerFactory.getLogger(PackagerJobEventHandlerService.class);
    private final KubernetesService kubernetesService;
    private final ModelService modelService;

    public static final String UNKNOWN = "unknown";
    UpdateModelRequest updateErrorStatus = new UpdateModelRequest(PackageStatus.Error, "Packaging Failed", "");

    public PackagerJobEventHandlerService(final KubernetesService kubernetesService, final ModelService modelService) {
        this.kubernetesService = kubernetesService;
        this.modelService = modelService;
    }

    @Override
    public void onAdd(final V1Job obj) {
        LOG.debug("onAdd Event Handler for Packager Job {}", getJobName(obj));
    }

    @Override
    public void onUpdate(final V1Job oldObj, final V1Job newJob) {
        // LOG.debug("onUpdate Event Handler for Packager Job {}", getJobName(newJob));
        String status = null;
        Date jobEndTime = null;
        var tmpModelName = getTempModelName(newJob);
        var packageId = getPackageName(newJob);
        if (newJob.getStatus().getSucceeded() != null && newJob.getStatus().getSucceeded() > 0) {
            status = "SUCCESSFUL";
            jobEndTime = Date.from(newJob.getStatus().getCompletionTime().toInstant());
        } else if (newJob.getStatus().getFailed() != null && newJob.getStatus().getFailed() > 0) {
            status = "ERROR";

            final List<V1JobCondition> jobConditions = newJob.getStatus().getConditions();
            for (final V1JobCondition condition : jobConditions) {
                jobEndTime = Date.from(condition.getLastTransitionTime().toInstant());
                if (jobEndTime != null) {
                    break;
                }
            }
        }
        /*
         * Condition: Job Completed and Status is error Action: Clean up Model Info/Source
         */
        if (jobEndTime != null) {
            LOG.info("Packager Job is {}. Job Name {} Temp Model Name '{}' Package Name '{}' Job Completion Time {}",
                    status, getJobName(newJob), tmpModelName, packageId, jobEndTime);
            if (status.equals("ERROR") && !StringUtils.isEmpty(tmpModelName)) {
                // Update Model status to 'error'
                final ModelCatalogueServiceResponse resp =
                        modelService.update(tmpModelName, UNKNOWN, updateErrorStatus);
                LOG.info(resp.message);
            }
        }
    }

    @Override
    public void onDelete(final V1Job obj, final boolean deletedFinalStateUnknown) {
        LOG.debug("onDelete Event Handler for Packager Job {}", getJobName(obj));
    }

    private String getJobName(final V1Job job) {
        return Objects.requireNonNull(job.getMetadata()).getName();
    }

    private String getPackageName(final V1Job job) {
        Map<String, String> labels = Objects.requireNonNull(job.getMetadata()).getLabels();
        return labels.getOrDefault(this.kubernetesService.getPackageNameLabelKey(), "");
    }

    private String getTempModelName(final V1Job job) {
        var wrapper = new Object() {
            String value = "";
        };
        job.getSpec().getTemplate().getSpec().getContainers().forEach(c -> {
            c.getEnv().forEach(e -> {
                if ("DUMMY_MODEL_ENTRY_NAME".equals(e.getName())) {
                    wrapper.value = e.getValue();
                }
            });
        });
        return wrapper.value;
    }
}
