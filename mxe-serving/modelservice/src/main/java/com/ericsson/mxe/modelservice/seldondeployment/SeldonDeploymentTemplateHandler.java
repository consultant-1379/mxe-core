package com.ericsson.mxe.modelservice.seldondeployment;

import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.modelservice.config.properties.SeldonProperties;
import com.ericsson.mxe.modelservice.dto.AutoscalingData;
import com.ericsson.mxe.modelservice.dto.AutoscalingMetric;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeldonDeploymentTemplateHandler {
    private static final String SELDON_DEPLOYMENT_KIND = "<SELDON_DEPLOYMENT_KIND>";
    private static final String MXE_MODELDEPLOYMENT_NAME = "<MXE_MODELDEPLOYMENT_NAME>";
    private static final String MXE_MODEL_IMAGE = "<MXE_MODEL_IMAGE>";
    private static final String MXE_REPLICAS = "<MXE_REPLICAS>";
    private static final String MXE_IMAGEPULLSECRET = "<MXE_IMAGEPULLSECRET>";
    private static final String MXE_MODELDEPLOYMENT_NAMESPACE = "<MXE_MODELDEPLOYMENT_NAMESPACE>";
    private static final String MXE_CREATEDBY_USERID = "<MXE_CREATEDBY_USERID>";
    private static final String MXE_CREATEDBY_USERNAME = "<MXE_CREATEDBY_USERNAME>";
    private static final String MXE_MODELDEPLOYMENT_STATEFUL = "<MXE_MODELDEPLOYMENT_STATEFUL>";
    private static final String MXE_MODEL_CONTAINER_NAME = "<MXE_MODEL_CONTAINER_NAME>";
    private static final String MXE_MODEL_CONTAINER_ENVS = "<MXE_MODEL_CONTAINER_ENVS>";
    private static final String MXE_MODELDEPLOYMENT_CONTAINERS = "<MXE_MODELDEPLOYMENT_CONTAINERS>";
    private static final String MXE_MODEL_SERVICE_COMPONENT_NAME = "<MXE_MODEL_SERVICE_COMPONENT_NAME>";
    private static final String MXE_DEPLOYMENT_TYPE = "<MXE_DEPLOYMENT_TYPE>";
    private static final String MXE_DEPLOYMENT_COMPONENT_SPECS = "<MXE_DEPLOYMENT_COMPONENT_SPECS>";
    private static final String MXE_DEPLOYMENT_GRAPH = "<MXE_DEPLOYMENT_GRAPH>";
    private static final String MXE_MODELDEPLOYMENT_RATIOA = "<MXE_MODELDEPLOYMENT_RATIOA>";
    private static final String MXE_HPA_SPECS = "<MXE_HPA_SPECS>";
    private static final String MXE_AUTOSCALING_MIN_REPLICAS = "<AUTOSCALING_MIN_REPLICAS>";
    private static final String MXE_AUTOSCALING_MAX_REPLICAS = "<AUTOSCALING_MAX_REPLICAS>";
    private static final String MXE_AUTOSCALING_RESOURCE_NAME = "<AUTOSCALING_RESOURCE_NAME>";
    private static final String MXE_AUTOSCALING_TARGET_AVERAGE_VALUE = "<AUTOSCALING_TARGET_AVERAGE_VALUE>";
    private static final String MXE_AUTOSCALING_METRICS = "<AUTOSCALING_METRICS>";
    private static final String MODEL_NAME = "model";
    private static final String MODEL_NAME_1 = "model-1";
    private static final String MODEL_NAME_2 = "model-2";

    private final SeldonProperties seldonProperties;
    private final KubernetesService kubernetesService;

    public SeldonDeploymentTemplateHandler(final SeldonProperties seldonProperties,
            final KubernetesService kubernetesService) {
        this.seldonProperties = seldonProperties;
        this.kubernetesService = kubernetesService;
    }

    String fillTemplate(MxeSeldonDeploymentData req) {
        String componentSpecsJson;
        String graphJson;
        switch (req.type) {
            case MODEL:
                componentSpecsJson = fillModelSpecTemplate(req);
                graphJson = fillModelGraphTemplate();
                break;
            case STATIC:
                List<String> componentSpecsJsonList = fillStaticSpecTemplate(req);
                componentSpecsJson = Joiner.on(",\n").join(componentSpecsJsonList);
                graphJson = fillStaticGraphTemplate(req.weights.get(0));
                break;
            default:
                throw new MxeInternalException("Unknown deployment type");
        }
        return fillCommonTemplate(req, componentSpecsJson, graphJson);
    }

    String fillModelSpecTemplate(MxeSeldonDeploymentData req) {
        String container = fillContainerTemplate(MODEL_NAME, req.models.get(0).image);
        return fillSpecsTemplate(req.name, MODEL_NAME, req.models.get(0), req.autoScaling, container);
    }

    List<String> fillStaticSpecTemplate(MxeSeldonDeploymentData req) {
        String containers1 = fillContainerTemplate(MODEL_NAME_1, req.models.get(0).image);
        String containers2 = fillContainerTemplate(MODEL_NAME_2, req.models.get(1).image);

        String specsTemplate1 =
                fillSpecsTemplate(req.name, MODEL_NAME_1, req.models.get(0), req.autoScaling, containers1);
        String specsTemplate2 =
                fillSpecsTemplate(req.name, MODEL_NAME_2, req.models.get(1), req.autoScaling, containers2);

        return Lists.newArrayList(specsTemplate1, specsTemplate2);
    }

    private String fillSpecsTemplate(String modelDeploymentName, String serviceComponentName, MxeModelInfo model,
            AutoscalingData autoscalingData, String containers) {
        String specsTemplate = getResourceFileAsString("SeldonTemplates/specs_template.json");
        return specsTemplate.replaceAll(MXE_MODELDEPLOYMENT_CONTAINERS, containers)
                .replaceAll(MXE_IMAGEPULLSECRET, getPullSecretName(model))
                .replaceAll(MXE_HPA_SPECS, fillHpaSpecTemplate(autoscalingData))
                .replaceAll(MXE_MODELDEPLOYMENT_NAME, modelDeploymentName)
                .replaceAll(MXE_MODEL_SERVICE_COMPONENT_NAME, serviceComponentName);
    }

    private String fillContainerTemplate(String name, String image) {
        return fillContainerTemplate(name, image, "");
    }

    private String fillContainerTemplate(String name, String image, String envs) {
        String containerTemplate = getResourceFileAsString("SeldonTemplates/container_template.json");
        return containerTemplate.replaceAll(MXE_MODEL_IMAGE, image).replaceAll(MXE_MODEL_CONTAINER_NAME, name)
                .replaceAll(MXE_MODEL_CONTAINER_ENVS, envs);
    }

    private String fillHpaSpecTemplate(AutoscalingData autoscalingData) {
        if (autoscalingData != null) {
            String hpaSpecTemplate = getResourceFileAsString("SeldonTemplates/hpa_spec_template.json");
            hpaSpecTemplate =
                    hpaSpecTemplate.replaceAll(MXE_AUTOSCALING_MIN_REPLICAS, autoscalingData.minReplicas.toString())
                            .replaceAll(MXE_AUTOSCALING_MAX_REPLICAS, autoscalingData.maxReplicas.toString())
                            .replaceAll(MXE_AUTOSCALING_METRICS,
                                    Joiner.on(",\n").join(fillHpaSpecMetricsTemplate(autoscalingData.metrics)));
            return ",\n" + hpaSpecTemplate;
        } else {
            return StringUtils.EMPTY;
        }
    }

    private List<String> fillHpaSpecMetricsTemplate(List<AutoscalingMetric> autoscalingMetrics) {
        List<String> hpaSpecMetrics = Lists.newArrayList();
        for (AutoscalingMetric autoscalingMetric : autoscalingMetrics) {
            String hpaSpecMetricTemplate = getResourceFileAsString("SeldonTemplates/hpa_spec_metric_template.json");
            hpaSpecMetricTemplate = hpaSpecMetricTemplate
                    .replaceAll(MXE_AUTOSCALING_RESOURCE_NAME, autoscalingMetric.name.getResourceName().name())
                    .replaceAll(MXE_AUTOSCALING_TARGET_AVERAGE_VALUE, autoscalingMetric.targetAverageValue.toString()
                            + autoscalingMetric.name.getResourceUnit().name());
            hpaSpecMetrics.add(hpaSpecMetricTemplate);
        }
        return hpaSpecMetrics;
    }

    private String getPullSecretName(MxeModelInfo mxeModelInfo) {
        return mxeModelInfo.pullSecretName != null ? "{\"name\":\"" + mxeModelInfo.pullSecretName + "\"}" : "";
    }

    String fillModelGraphTemplate() {
        return getResourceFileAsString("SeldonTemplates/single_graph_template.json");
    }

    String fillStatefulModelGraphTemplate() {
        return getResourceFileAsString("SeldonTemplates/single_model_stateful_graph_template.json");
    }

    String fillStaticGraphTemplate(double ratioA) {
        String graphTemplate = getResourceFileAsString("SeldonTemplates/abtest_graph_template.json");
        return graphTemplate.replaceAll(MXE_MODELDEPLOYMENT_RATIOA, String.valueOf(ratioA));
    }

    private String getResourceFileAsString(String fileName) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if (is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        throw new MxeInternalException("Unable to find resource:" + fileName);
    }

    private String fillCommonTemplate(MxeSeldonDeploymentData req, String specsTemplate, String graphTemplate) {
        String template = getResourceFileAsString("SeldonTemplates/seldondeployment_template.json");
        return template.replaceAll(SELDON_DEPLOYMENT_KIND, this.seldonProperties.getCrd().getName())
                .replaceAll(MXE_MODELDEPLOYMENT_NAME, req.name)
                .replaceAll(MXE_DEPLOYMENT_TYPE, req.type.toString().toLowerCase())
                .replaceAll(MXE_REPLICAS, req.replicas.toString())
                .replaceAll(MXE_MODELDEPLOYMENT_NAMESPACE, this.kubernetesService.getNamespace())
                .replaceAll(MXE_CREATEDBY_USERID, req.createdByUserId)
                .replaceAll(MXE_CREATEDBY_USERNAME, req.createdByUserName)
                .replaceAll(MXE_DEPLOYMENT_COMPONENT_SPECS, specsTemplate)
                .replaceAll(MXE_DEPLOYMENT_GRAPH, graphTemplate);
    }
}
