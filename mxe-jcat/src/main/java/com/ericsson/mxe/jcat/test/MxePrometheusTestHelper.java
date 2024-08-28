package com.ericsson.mxe.jcat.test;

import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setSubTestStep;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setSubTestStepEnd;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestError;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestFile;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestInfo;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepBegin;
import static se.ericsson.jcat.fw.logging.JcatLoggingApi.setTestStepEnd;
import java.io.Serializable;
import java.time.ZonedDateTime;
import com.ericsson.mxe.jcat.driver.prometheus.PrometheusDriver;
import com.ericsson.mxe.jcat.json.prometheus.PrometheusMetric;
import com.ericsson.mxe.jcat.json.prometheus.PrometheusRangeMetric;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class MxePrometheusTestHelper {

    private MxePrometheusTestHelper() {

    }

    public static PrometheusData collectPrometheusData(final PrometheusDriver prometheusDriver,
            final ZonedDateTime start, final ZonedDateTime end, final String modelDeploymentName) {
        PrometheusData pd = new PrometheusData();
        setTestStepBegin("Collecting data from prometheus");
        setSubTestStep("[Metric] Memory bytes used per node");
        pd.memoryBytesUsedPerNode =
                prometheusDriver.queryRange(start, end, "10s", PrometheusDriver.QUERY_MEMORY_BYTES_USAGE_PER_NODE);
        saveJsonDataToFile(pd.memoryBytesUsedPerNode, "memoryBytesUsedPerNode");
        logPrometheusMetric(pd.memoryBytesUsedPerNode);
        setSubTestStepEnd();

        setSubTestStep("[Metric] FS usage per node");
        pd.fsUsagePerNode = prometheusDriver.queryRange(start, end, "10s", PrometheusDriver.QUERY_FS_USAGE_PER_NODE);
        saveJsonDataToFile(pd.fsUsagePerNode, "fsUsagePerNode");
        logPrometheusMetric(pd.fsUsagePerNode);
        setSubTestStepEnd();

        setSubTestStep("[Metric] Cpu cores used per node");
        pd.cpuCoresUsedPerNode =
                prometheusDriver.queryRange(start, end, "10s", PrometheusDriver.QUERY_CPU_CORES_USAGE_PER_NODE);
        saveJsonDataToFile(pd.cpuCoresUsedPerNode, "cpuCoresUsedPerNode");
        logPrometheusMetric(pd.cpuCoresUsedPerNode);
        setSubTestStepEnd();

        setSubTestStep("[Metric] Network receive bytes per node");
        pd.networkReceiveBytesPerNode =
                prometheusDriver.queryRange(start, end, "10s", PrometheusDriver.QUERY_NETWORK_RECEIVE_BYTES_PER_NODE);
        saveJsonDataToFile(pd.networkReceiveBytesPerNode, "networkReceiveBytesPerNode");
        logPrometheusMetric(pd.networkReceiveBytesPerNode);
        setSubTestStepEnd();

        setSubTestStep("[Metric] Network transmit bytes per node");
        pd.networkTransmitBytesPerNode =
                prometheusDriver.queryRange(start, end, "10s", PrometheusDriver.QUERY_NETWORK_TRANSMIT_BYTES_PER_NODE);
        saveJsonDataToFile(pd.networkTransmitBytesPerNode, "networkTransmitBytesPerNode");
        logPrometheusMetric(pd.networkTransmitBytesPerNode);
        setSubTestStepEnd();

        setSubTestStep("[Metric] Request per seconds");
        pd.requestPerSeconds = prometheusDriver.queryRange(start, end, "10s",
                String.format(PrometheusDriver.QUERY_TEMPLATE_REQUEST_PER_SEC, modelDeploymentName));
        saveJsonDataToFile(pd.requestPerSeconds, "requestPerSeconds");
        logPrometheusMetric(pd.requestPerSeconds);
        setSubTestStepEnd();

        setSubTestStep("[Metric] Model latency 0.75");
        pd.latency075 = prometheusDriver.queryRange(start, end, "10s",
                String.format(PrometheusDriver.QUERY_TEMPLATE_LATENCY, 0.75f, modelDeploymentName));
        saveJsonDataToFile(pd.latency075, "latency075");
        logPrometheusMetric(pd.latency075);
        setSubTestStepEnd();

        setSubTestStep("[Metric] Model latency 0.9");
        pd.latency09 = prometheusDriver.queryRange(start, end, "10s",
                String.format(PrometheusDriver.QUERY_TEMPLATE_LATENCY, 0.9f, modelDeploymentName));
        saveJsonDataToFile(pd.latency09, "latency090");
        logPrometheusMetric(pd.latency09);
        setSubTestStepEnd();

        setSubTestStep("[Metric] Model latency 0.95");
        pd.latency095 = prometheusDriver.queryRange(start, end, "10s",
                String.format(PrometheusDriver.QUERY_TEMPLATE_LATENCY, 0.95f, modelDeploymentName));
        saveJsonDataToFile(pd.latency095, "latency095");
        logPrometheusMetric(pd.latency095);
        setSubTestStepEnd();

        setSubTestStep("[Metric] Model latency 0.99");
        pd.latency099 = prometheusDriver.queryRange(start, end, "10s",
                String.format(PrometheusDriver.QUERY_TEMPLATE_LATENCY, 0.99f, modelDeploymentName));
        saveJsonDataToFile(pd.latency099, "latency099");
        logPrometheusMetric(pd.latency099);
        setSubTestStepEnd();
        setTestStepEnd();
        return pd;
    }

    private static void logPrometheusMetric(Serializable metric) {
        final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            setTestInfo(mapper.writeValueAsString(metric));
        } catch (JsonProcessingException e) {
            setTestError("Failed to process prometheus output", e);
        }
    }

    private static void saveJsonDataToFile(Serializable prometheusMetric, String fileName) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            setTestFile(fileName, mapper.writeValueAsString(prometheusMetric), "json", ".json", false);
        } catch (JsonProcessingException e) {
            setTestError("Failed to process prometheus output", e);
        }
    }

    public static PrometheusHWInfo collectHardwareInfoFromPrometheus(PrometheusDriver prometheusDriver) {
        setTestStepBegin("Collect hardware info");
        PrometheusHWInfo hwInfo = new PrometheusHWInfo();
        setSubTestStep("Collect cadvisor_version_info from Prometheus");
        hwInfo.cadvisor_version_info = prometheusDriver.query(PrometheusDriver.CADVISOR_VERSION_INFO);
        saveJsonDataToFile(hwInfo.cadvisor_version_info, "cadvisor_version_info");
        logPrometheusMetric(hwInfo.cadvisor_version_info);

        setSubTestStep("Collect CPU cores info from Prometheus");
        hwInfo.machine_cpu_cores = prometheusDriver.query(PrometheusDriver.MACHINE_CPU_CORES);
        saveJsonDataToFile(hwInfo.machine_cpu_cores, "machine_cpu_cores");
        logPrometheusMetric(hwInfo.machine_cpu_cores);

        setSubTestStep("Collect memory info from Prometheus");
        hwInfo.machine_memory_bytes = prometheusDriver.query(PrometheusDriver.MACHINE_MEMORY_BYTES);
        saveJsonDataToFile(hwInfo.machine_memory_bytes, "machine_memory_bytes");
        logPrometheusMetric(hwInfo.machine_memory_bytes);

        setTestStepEnd();
        return hwInfo;
    }

    public static class PrometheusHWInfo {
        public PrometheusMetric cadvisor_version_info;
        public PrometheusMetric machine_cpu_cores;
        public PrometheusMetric machine_memory_bytes;
    }

    public static class PrometheusData {
        public PrometheusRangeMetric memoryBytesUsedPerNode;
        public PrometheusRangeMetric fsUsagePerNode;
        public PrometheusRangeMetric cpuCoresUsedPerNode;
        public PrometheusRangeMetric networkReceiveBytesPerNode;
        public PrometheusRangeMetric networkTransmitBytesPerNode;
        public PrometheusRangeMetric requestPerSeconds;
        public PrometheusRangeMetric latency075;
        public PrometheusRangeMetric latency09;
        public PrometheusRangeMetric latency095;
        public PrometheusRangeMetric latency099;
    }
}
