package com.ericsson.mxe.jcat.driver.prometheus;

import com.ericsson.mxe.jcat.driver.kubernetes.KubernetesDriver;
import com.ericsson.mxe.jcat.json.prometheus.PrometheusMetric;
import com.ericsson.mxe.jcat.json.prometheus.PrometheusRangeMetric;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.apache.commons.net.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PrometheusDriver extends KubernetesDriver {

    public static final int DEFAULT_PROMETHEUS_PORT = 9090;
    public static final int DEFAULT_PROMETHEUS_FORWARDED_PORT = 10000 + DEFAULT_PROMETHEUS_PORT;

    public static final String LABELSELECTOR_PROMETHEUS_SERVER = "app=prometheus-server";
    public static final String LABELSELECTOR_PROMETHEUS_SERVER_KEY = "app.kubernetes.io/name";
    public static final List<String> LABELSELECTOR_PROMETHEUS_SERVER_VALUES =
            Arrays.asList("prometheus-deployment", "eric-mxe-prometheus-deployment", "eric-pm-server");
    public static final String LABELSELECTOR_PROMETHEUS_DEPLOYMENT = "prometheus-deployment";

    public static final String QUERY_CPU_CORES_USAGE_PER_NODE = "rate(container_cpu_usage_seconds_total{id=\"/\"}[1m])";
    public static final String QUERY_FS_USAGE_PER_NODE =
            "container_fs_usage_bytes{device=~\"^/dev/[sv]da[0-9]$\", id=\"/\"}";
    public static final String QUERY_MEMORY_BYTES_USAGE_PER_NODE = "container_memory_working_set_bytes{id=\"/\"}";
    public static final String QUERY_NETWORK_RECEIVE_BYTES_PER_NODE =
            "sum(rate(container_network_receive_bytes_total[1m])) by(kubernetes_io_hostname)";
    public static final String QUERY_NETWORK_TRANSMIT_BYTES_PER_NODE =
            "sum(rate(container_network_transmit_bytes_total[1m])) by(kubernetes_io_hostname)";

    public static final String QUERY_TEMPLATE_LATENCY =
            "histogram_quantile(%f, sum(rate(seldon_api_executor_client_requests_seconds_bucket{uri=\"/predict\",model_name=~\"%s\"}[20s])) by (model_name,le))";
    public static final String QUERY_TEMPLATE_REQUEST_PER_SEC =
            "sum(rate(seldon_api_executor_client_requests_seconds_count{model_name=~\"%s\"}[1m])) by (model_name)";

    public static final String CADVISOR_VERSION_INFO = "cadvisor_version_info";
    public static final String MACHINE_CPU_CORES = "machine_cpu_cores";
    public static final String MACHINE_MEMORY_BYTES = "machine_memory_bytes";

    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusDriver.class);

    private final RestTemplate restTemplate;
    private final String url;
    private final String user;
    private final String password;
    private final HttpHeaders headers;


    public PrometheusDriver(final String hostName, boolean portForward) throws MalformedURLException {
        this(hostName, null, null, portForward);
    }

    public PrometheusDriver(final String hostName, final String user, final String password, boolean portForward)
            throws MalformedURLException {
        super();
        restTemplate = new RestTemplate();
        this.url = "http://" + hostName + ":"
                + (portForward ? DEFAULT_PROMETHEUS_FORWARDED_PORT : DEFAULT_PROMETHEUS_PORT);
        this.user = user;
        this.password = password;
        headers = createHeaders();
        if (portForward) {
            portForward();
        }
    }

    public PrometheusMetric query(final String query) {
        final LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("query", query);

        return restTemplate.exchange(url + "/api/v1/query", HttpMethod.POST, new HttpEntity<>(form, headers),
                PrometheusMetric.class).getBody();
    }

    public PrometheusRangeMetric queryRange(final ZonedDateTime start, final ZonedDateTime end, final String step,
            final String query) {
        final LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("start", start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        form.add("end", end.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        form.add("step", step);
        form.add("query", query);

        return restTemplate.exchange(url + "/api/v1/query_range", HttpMethod.POST, new HttpEntity<>(form, headers),
                PrometheusRangeMetric.class).getBody();
    }

    private HttpHeaders createHeaders() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        if (Objects.nonNull(user) && Objects.nonNull(password)) {
            final String auth = user + ":" + password;
            final Charset charset = Charset.forName("UTF-8");
            final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(charset));
            final String authHeader = "Basic " + new String(encodedAuth, charset);
            httpHeaders.set("Authorization", authHeader);
        }

        return httpHeaders;
    }

    private void portForward() {
        LOGGER.info("Using port forward");
        kubernetesClient = new DefaultKubernetesClient(Config.autoConfigure(null));
        listRunningPods().list().getItems()
                .forEach(p -> LOGGER.debug("POD:" + p + "\nMETA_LABELS:" + p.getMetadata().getLabels()));
        List<Pod> podsBeforeUpgrade =
                filterWithLabel(listRunningPods(), LABELSELECTOR_PROMETHEUS_SERVER).list().getItems();
        if (!podsBeforeUpgrade.isEmpty()) {
            LOGGER.info("Found pods (with label {}):{}", LABELSELECTOR_PROMETHEUS_SERVER, podsBeforeUpgrade);
            createPodPortForward(podsBeforeUpgrade.get(0), DEFAULT_PROMETHEUS_PORT, DEFAULT_PROMETHEUS_FORWARDED_PORT);
        } else {
            if (!super.podPortForward(LABELSELECTOR_PROMETHEUS_SERVER_KEY, LABELSELECTOR_PROMETHEUS_SERVER_VALUES,
                    LABELSELECTOR_PROMETHEUS_DEPLOYMENT, DEFAULT_PROMETHEUS_PORT, DEFAULT_PROMETHEUS_FORWARDED_PORT)) {
                throw new RuntimeException("Prometheus port forward failed");
            }
        }
    }
}
