package com.ericsson.mxe.jcat.command;

import com.ericsson.mxe.jcat.dto.autoscaling.AutoscalingData;
import com.google.common.base.Joiner;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

public abstract class MxeServiceCommand extends MxeCommand {
    private static final String PARAMETER_TEMPLATE_DELETE = " delete --name %s";
    private static final String PARAMETER_TEMPLATE_CREATE = " create --name %s%s%s%s%s";
    private static final String PARAMETER_TEMPLATE_MODIFY = " modify --name %s%s%s%s%s";
    private static final String PARAMETER_TEMPLATE_MODELS = " --models %s";
    private static final String PARAMETER_TEMPLATE_DOMAIN = " --domain %s";
    private static final String PARAMETER_TEMPLATE_INSTANCES = " --instances %d";
    private static final String PARAMETER_TEMPLATE_WEIGHTS = " --weights %s";
    private static final String PARAMETER_TEMPLATE_AUTOSCALE =
            " --instances auto --minReplicas %d --maxReplicas %d --metric %s --targetAverageValue %d";
    private static final String PARAMETER_TEMPLATE_VERSION = " version";

    public MxeServiceCommand(String command) {
        super(command);
    }

    public MxeServiceCommand list() {
        setParameter(PARAMETER_TEMPLATE_LIST);
        return this;
    }

    public MxeServiceCommand delete(final String name) {
        setParameter(String.format(PARAMETER_TEMPLATE_DELETE, name));
        return this;
    }

    public MxeServiceCommand create(final String name, final List<String> models, String domain,
            final Integer instances, final List<Double> weights) {
        return getMxeServiceCommand(PARAMETER_TEMPLATE_CREATE, name, models, domain, instances, null, weights);
    }

    public MxeServiceCommand create(final String name, final List<String> models, String domain,
            AutoscalingData autoscalingData, final List<Double> weights) {
        return getMxeServiceCommand(PARAMETER_TEMPLATE_CREATE, name, models, domain, null, autoscalingData, weights);
    }

    public MxeServiceCommand create(final String name, final List<String> models, final Integer instances,
            final List<Double> weights) {
        return getMxeServiceCommand(PARAMETER_TEMPLATE_CREATE, name, models, null, instances, null, weights);
    }

    public MxeServiceCommand create(final String name, final List<String> models, AutoscalingData autoscalingData,
            final List<Double> weights) {
        return getMxeServiceCommand(PARAMETER_TEMPLATE_CREATE, name, models, null, null, autoscalingData, weights);
    }

    public MxeServiceCommand modify(final String name, final List<String> models, String domain,
            final Integer instances, final List<Double> weights) {
        return getMxeServiceCommand(PARAMETER_TEMPLATE_MODIFY, name, models, domain, instances, null, weights);
    }

    public MxeServiceCommand modify(final String name, final List<String> models, final Integer instances,
            final List<Double> weights) {
        return getMxeServiceCommand(PARAMETER_TEMPLATE_MODIFY, name, models, null, instances, null, weights);
    }

    public MxeServiceCommand modify(final String name, final List<String> models) {
        return getMxeServiceCommand(PARAMETER_TEMPLATE_MODIFY, name, models, null, null, null, null);
    }

    public MxeServiceCommand modify(final String name, final List<String> models, AutoscalingData autoscalingData,
            final List<Double> weights) {
        return getMxeServiceCommand(PARAMETER_TEMPLATE_MODIFY, name, models, null, null, autoscalingData, weights);
    }

    private MxeServiceCommand getMxeServiceCommand(String template, String name, List<String> models, String domain,
            Integer instances, AutoscalingData autoscalingData, List<Double> weights) {
        String modelsValue = CollectionUtils.isEmpty(models) ? ""
                : String.format(PARAMETER_TEMPLATE_MODELS, String.join(",", models));
        String domainValue = StringUtils.isEmpty(domain) ? "" : String.format(PARAMETER_TEMPLATE_DOMAIN, domain);
        String instancesOrAutoscalingValue = instances != null ? String.format(PARAMETER_TEMPLATE_INSTANCES, instances)
                : (autoscalingData != null ? String.format(PARAMETER_TEMPLATE_AUTOSCALE, autoscalingData.minReplicas,
                        autoscalingData.maxReplicas, autoscalingData.metrics.name.getResourceName(),
                        autoscalingData.metrics.targetAverageValue) : "");
        String weightsValue = CollectionUtils.isEmpty(weights) ? ""
                : String.format(PARAMETER_TEMPLATE_WEIGHTS, Joiner.on(',').join(weights));
        setParameter(
                String.format(template, name, modelsValue, domainValue, weightsValue, instancesOrAutoscalingValue));
        return this;
    }

    public MxeServiceCommand version() {
        setParameter(PARAMETER_TEMPLATE_VERSION);
        return this;
    }
}
