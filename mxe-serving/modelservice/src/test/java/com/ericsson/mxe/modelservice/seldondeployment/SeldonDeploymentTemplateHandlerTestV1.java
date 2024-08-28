package com.ericsson.mxe.modelservice.seldondeployment;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.ericsson.mxe.backendservicescommon.config.properties.KubernetesServiceProperties;
import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.modelservice.config.properties.SeldonCrdProperties;
import com.ericsson.mxe.modelservice.config.properties.SeldonProperties;
import com.ericsson.mxe.modelservice.dto.AutoscalingData;
import com.ericsson.mxe.modelservice.dto.AutoscalingMetric;
import com.ericsson.mxe.modelservice.dto.AutoscalingMetricName;
import com.ericsson.mxe.modelservice.dto.MxeModelDeploymentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.kubernetes.client.openapi.ApiClient;

public class SeldonDeploymentTemplateHandlerTestV1 {

    private static final Logger logger = LogManager.getLogger(SeldonDeploymentTemplateHandlerTestV1.class);
    private SeldonDeploymentTemplateHandler seldonDeploymentTemplateHandler;
    private static final Double ratioA = 0.3;

    @BeforeEach
    void init() {
        SeldonCrdProperties seldonCrdProperties = new SeldonCrdProperties("crdName");
        SeldonProperties seldonProperties = new SeldonProperties(seldonCrdProperties, null);
        KubernetesService kubernetesService = new KubernetesServiceMock();
        seldonDeploymentTemplateHandler = new SeldonDeploymentTemplateHandler(seldonProperties, kubernetesService);
    }

    @Test
    public void testFillModelTemplate() throws IOException {
        final String deploymentDataName = "deploymentdataname";
        final MxeModelDeploymentType type = MxeModelDeploymentType.MODEL;
        MxeSeldonDeploymentData mxeSeldonDeploymentData = createMxeSeldonDeploymentData(deploymentDataName, type);

        String body = seldonDeploymentTemplateHandler.fillTemplate(mxeSeldonDeploymentData);
        logger.info(body);
        final String name = "name";
        Map map = new ObjectMapper().readValue(body, Map.class);
        Map metaData = (Map) map.get("metadata");
        assertThat(metaData.get(name)).isEqualTo(deploymentDataName.toLowerCase());
        Map spec = (Map) map.get("spec");
        List<Map> list = (List<Map>) spec.get("predictors");
        Map predictor = list.get(0);
        assertThat(predictor.get(name)).isEqualTo("main");

        Map graph = (Map) predictor.get("graph");
        assertThat(graph.get(name)).isEqualTo("model");
        List<Map> children = (List<Map>) graph.get("children");
        assertThat(children.isEmpty()).isTrue();

        List<Map> componentSpecs = (List<Map>) predictor.get("componentSpecs");
        assertThat(componentSpecs.size()).isEqualTo(1);
        spec = (Map) componentSpecs.get(0).get("spec");
        List<Map> containers = (List<Map>) spec.get("containers");
        assertThat(containers.size()).isEqualTo(1);
        String containerName = (String) containers.get(0).get(name);
        assertThat(containerName).isEqualTo("model");

        Map hpaSpec = (Map) componentSpecs.get(0).get("hpaSpec");
        assertThat(hpaSpec.get("minReplicas")).isEqualTo(1);
        assertThat(hpaSpec.get("maxReplicas")).isEqualTo(3);
        List<Map> metrics = (List<Map>) hpaSpec.get("metrics");
        assertThat(metrics.size()).isEqualTo(2);
        assertThat(metrics.get(0).get("type")).isEqualTo("Resource");
        Map resource1 = (Map) metrics.get(0).get("resource");
        assertThat(resource1.get("name")).isEqualTo("cpu");
        assertThat(resource1.get("targetAverageValue")).isEqualTo("10m");
        Map resource2 = (Map) metrics.get(1).get("resource");
        assertThat(resource2.get("name")).isEqualTo("memory");
        assertThat(resource2.get("targetAverageValue")).isEqualTo("20Mi");
    }

    @Test
    public void testFillStaticTemplate() throws IOException {
        final String deploymentDataName = "deploymentdataname";
        final MxeModelDeploymentType type = MxeModelDeploymentType.STATIC;
        MxeSeldonDeploymentData mxeSeldonDeploymentData = createMxeSeldonDeploymentData(deploymentDataName, type);

        String body = seldonDeploymentTemplateHandler.fillTemplate(mxeSeldonDeploymentData);
        logger.info(body);
        final String name = "name";
        Map map = new ObjectMapper().readValue(body, Map.class);
        Map metaData = (Map) map.get("metadata");
        assertThat(metaData.get(name)).isEqualTo(deploymentDataName.toLowerCase());
        Map spec = (Map) map.get("spec");
        List<Map> list = (List) spec.get("predictors");
        Map predictor = list.get(0);
        assertThat(predictor.get(name)).isEqualTo("main");

        Map graph = (Map) predictor.get("graph");
        assertThat(graph.get(name)).isEqualTo("ab-test");
        List<Map> children = (List<Map>) graph.get("children");
        assertThat(children.size()).isEqualTo(2);
        String model1 = (String) children.get(0).get(name);
        assertThat(model1).isEqualTo("model-1");
        String model2 = (String) children.get(1).get(name);
        assertThat(model2).isEqualTo("model-2");

        List<Map> parameters = (List<Map>) graph.get("parameters");
        assertThat(parameters.size()).isEqualTo(1);
        Map paramMap = parameters.get(0);
        assertThat(paramMap.get(name)).isEqualTo("ratioA");
        assertThat(paramMap.get("value")).isEqualTo(String.valueOf(ratioA));
        assertThat(paramMap.get("type")).isEqualTo("FLOAT");

        List<Map> componentSpecs = (List<Map>) predictor.get("componentSpecs");
        assertThat(componentSpecs.size()).isEqualTo(2);

        Map spec1 = (Map) componentSpecs.get(0).get("spec");
        List<Map> containers = (List<Map>) spec1.get("containers");
        assertThat(containers.size()).isEqualTo(1);
        String containerName = (String) containers.get(0).get(name);
        assertThat(containerName).isEqualTo("model-1");

        Map hpaSpec1 = (Map) componentSpecs.get(0).get("hpaSpec");
        assertThat(hpaSpec1.get("minReplicas")).isEqualTo(1);
        assertThat(hpaSpec1.get("maxReplicas")).isEqualTo(3);
        List<Map> metrics1 = (List<Map>) hpaSpec1.get("metrics");
        assertThat(metrics1.size()).isEqualTo(2);
        assertThat(metrics1.get(0).get("type")).isEqualTo("Resource");
        Map metrics1resource1 = (Map) metrics1.get(0).get("resource");
        assertThat(metrics1resource1.get("name")).isEqualTo("cpu");
        assertThat(metrics1resource1.get("targetAverageValue")).isEqualTo("10m");
        Map metrics1resource2 = (Map) metrics1.get(1).get("resource");
        assertThat(metrics1resource2.get("name")).isEqualTo("memory");
        assertThat(metrics1resource2.get("targetAverageValue")).isEqualTo("20Mi");

        Map spec2 = (Map) componentSpecs.get(1).get("spec");
        containers = (List<Map>) spec2.get("containers");
        assertThat(containers.size()).isEqualTo(1);
        containerName = (String) containers.get(0).get(name);
        assertThat(containerName).isEqualTo("model-2");

        Map hpaSpec2 = (Map) componentSpecs.get(1).get("hpaSpec");
        assertThat(hpaSpec2.get("minReplicas")).isEqualTo(1);
        assertThat(hpaSpec2.get("maxReplicas")).isEqualTo(3);
        List<Map> metrics2 = (List<Map>) hpaSpec2.get("metrics");
        assertThat(metrics2.size()).isEqualTo(2);
        assertThat(metrics2.get(0).get("type")).isEqualTo("Resource");
        Map metrics2resource1 = (Map) metrics2.get(0).get("resource");
        assertThat(metrics2resource1.get("name")).isEqualTo("cpu");
        assertThat(metrics2resource1.get("targetAverageValue")).isEqualTo("10m");
        assertThat(metrics2.get(1).get("type")).isEqualTo("Resource");
        Map metrics2resource2 = (Map) metrics2.get(1).get("resource");
        assertThat(metrics2resource2.get("name")).isEqualTo("memory");
        assertThat(metrics2resource2.get("targetAverageValue")).isEqualTo("20Mi");
    }

    private MxeSeldonDeploymentData createMxeSeldonDeploymentData(final String deploymentDataName,
            final MxeModelDeploymentType type) {
        final int replicas = 1;
        final String domain = "domain.name";
        final String createdByUserId = "UserId";
        final String createdByUserName = "UserName";
        final String endpointType = "EndpointType";
        final String pullSecretName = "PullSecretName";
        final String image1 = "ImageName/0.0.1";
        final String image2 = "ImageName/0.0.2";
        final List<Double> weights = new ArrayList<>();
        weights.add(SeldonDeploymentTemplateHandlerTestV1.ratioA);
        final List<MxeModelInfo> models = new ArrayList<>();
        switch (type) {
            case MODEL:
                models.add(new MxeModelInfo(image1, endpointType, pullSecretName));
                break;
            case STATIC:
                models.add(new MxeModelInfo(image1, endpointType, pullSecretName));
                models.add(new MxeModelInfo(image2, endpointType, pullSecretName));
                break;
        }

        AutoscalingMetric autoscalingMetric1 = new AutoscalingMetric();
        autoscalingMetric1.name = AutoscalingMetricName.CPU_MILLI_CORES;
        autoscalingMetric1.targetAverageValue = 10;
        AutoscalingMetric autoscalingMetric2 = new AutoscalingMetric();
        autoscalingMetric2.name = AutoscalingMetricName.MEMORY_MEGA_BYTES;
        autoscalingMetric2.targetAverageValue = 20;
        AutoscalingData autoscalingData = new AutoscalingData();
        autoscalingData.maxReplicas = 3;
        autoscalingData.minReplicas = 1;
        autoscalingData.metrics = Lists.newArrayList(autoscalingMetric1, autoscalingMetric2);

        MxeSeldonDeploymentData mxeSeldonDeploymentData = new MxeSeldonDeploymentData();
        mxeSeldonDeploymentData.type = type;
        mxeSeldonDeploymentData.models = models;
        mxeSeldonDeploymentData.name = deploymentDataName;
        mxeSeldonDeploymentData.replicas = replicas;
        mxeSeldonDeploymentData.weights = weights;
        mxeSeldonDeploymentData.createdByUserId = createdByUserId;
        mxeSeldonDeploymentData.createdByUserName = createdByUserName;
        mxeSeldonDeploymentData.autoScaling = autoscalingData;

        return mxeSeldonDeploymentData;
    }

    static class KubernetesServiceMock extends KubernetesService {

        public KubernetesServiceMock() {
            super(new KubernetesServiceProperties(false, null, 0));
        }

        @Override
        protected String loadNameSpace() {
            return "mxe-test-root";
        }

        @Override
        protected ApiClient initApi(String patchFormat) {
            return null;
        }
    }

}
