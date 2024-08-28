package com.ericsson.mxe.jcat.driver.kubernetes;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Closeable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class KubernetesDriver implements AutoCloseable {

    protected static final Logger LOGGER = LoggerFactory.getLogger(KubernetesDriver.class);

    protected KubernetesClient kubernetesClient;
    protected LocalPortForward portForward;

    public KubernetesDriver() {
        kubernetesClient = new DefaultKubernetesClient(Config.autoConfigure(null));
    }

    protected boolean podPortForward(String labelSelectorKey, List<String> labelSelectorValues, String anyLabelMatch,
            int sourcePort, int destinationPort) {
        LOGGER.info("Using pod port forward");
        kubernetesClient = new DefaultKubernetesClient(Config.autoConfigure(null));
        listRunningPods().list().getItems()
                .forEach(p -> LOGGER.debug("POD:" + p + "\nMETA_LABELS:" + p.getMetadata().getLabels()));
        FilterWatchListDeletable<Pod, PodList, Boolean, Watch, Watcher<Pod>> podsWithLabel =
                listRunningPods().withLabelIn(labelSelectorKey, (String[]) labelSelectorValues.toArray());
        List<Pod> pods = podsWithLabel.list().getItems();
        if (!pods.isEmpty()) {
            LOGGER.info("Found pods (with label {}{}):{}", labelSelectorKey, labelSelectorValues, pods);
            createPodPortForward(pods.get(0), sourcePort, destinationPort);
        } else {
            Optional<Pod> optPod = listRunningPods().list().getItems().stream().filter(
                    p -> labelSelectorValues.contains(p.getMetadata().getLabels().getOrDefault(labelSelectorKey, ""))
                            || (!StringUtils.isEmpty(anyLabelMatch)
                                    && p.getMetadata().getLabels().containsValue(anyLabelMatch)))
                    .findFirst();
            if (optPod.isPresent()) {
                LOGGER.info("Found pod :{}", optPod.get());
                createPodPortForward(optPod.get(), sourcePort, destinationPort);
            } else {
                List<Pod> items = listRunningPods().list().getItems();
                items.stream().forEach(p -> LOGGER.info("Label keys:{}",
                        p.getMetadata().getLabels().keySet().stream().collect(Collectors.joining(",", "'", "'"))));
                items.stream().forEach(p -> LOGGER.info("Label values:{}",
                        p.getMetadata().getLabels().values().stream().collect(Collectors.joining(",", "'", "'"))));
                return false;
            }
        }
        return true;
    }

    protected boolean servicePortForward(String labelSelectorKey, List<String> labelSelectorValues,
            String anyLabelMatch, int sourcePort, int destinationPort) {
        LOGGER.info("Using service port forward");
        kubernetesClient = new DefaultKubernetesClient(Config.autoConfigure(null));
        listServices().list().getItems()
                .forEach(s -> LOGGER.debug("SERVICE:" + s + "\nMETA_LABELS:" + s.getMetadata().getLabels()));
        FilterWatchListDeletable<Service, ServiceList, Boolean, Watch, Watcher<Service>> podsWithLabel =
                listServices().withLabelIn(labelSelectorKey, (String[]) labelSelectorValues.toArray());
        List<Service> services = podsWithLabel.list().getItems();
        if (!services.isEmpty()) {
            LOGGER.info("Found service (with label {}{}):{}", labelSelectorKey, labelSelectorValues, services);
            createServicePortForward(services.get(0), sourcePort, destinationPort);
        } else {
            Optional<Service> optService = listServices().list().getItems().stream().filter(
                    p -> labelSelectorValues.contains(p.getMetadata().getLabels().getOrDefault(labelSelectorKey, ""))
                            || (!StringUtils.isEmpty(anyLabelMatch)
                                    && p.getMetadata().getLabels().containsValue(anyLabelMatch)))
                    .findFirst();
            if (optService.isPresent()) {
                LOGGER.info("Found service:{}", optService.get());
                createServicePortForward(optService.get(), sourcePort, destinationPort);
            } else {
                List<Service> items = listServices().list().getItems();
                items.stream().forEach(p -> LOGGER.info("Label keys:{}",
                        p.getMetadata().getLabels().keySet().stream().collect(Collectors.joining(",", "'", "'"))));
                items.stream().forEach(p -> LOGGER.info("Label values:{}",
                        p.getMetadata().getLabels().values().stream().collect(Collectors.joining(",", "'", "'"))));
                return false;
            }
        }
        return true;
    }

    public FilterWatchListDeletable<Pod, PodList, Boolean, Watch, Watcher<Pod>> listRunningPods() {
        return kubernetesClient.pods().inAnyNamespace().withField("status.phase", "Running");
    }

    public FilterWatchListDeletable<Service, ServiceList, Boolean, Watch, Watcher<Service>> listServices() {
        return kubernetesClient.services().inAnyNamespace();
    }

    public void createPodPortForward(Pod p, int sourcePort, int destinationPort) {
        PodResource<Pod, DoneablePod> withName =
                kubernetesClient.pods().inNamespace(p.getMetadata().getNamespace()).withName(p.getMetadata().getName());
        portForward = withName.portForward(sourcePort, destinationPort);
    }

    public void createServicePortForward(Service s, int sourcePort, int destinationPort) {
        ServiceResource<Service, DoneableService> withName = kubernetesClient.services()
                .inNamespace(s.getMetadata().getNamespace()).withName(s.getMetadata().getName());
        portForward = withName.portForward(sourcePort, destinationPort);
    }

    public FilterWatchListDeletable<Pod, PodList, Boolean, Watch, Watcher<Pod>> filterWithLabel(
            FilterWatchListDeletable<Pod, PodList, Boolean, Watch, Watcher<Pod>> withField, String label) {
        FilterWatchListDeletable<Pod, PodList, Boolean, Watch, Watcher<Pod>> withLabelBeforeUpgrade =
                withField.withLabel(label);
        return withLabelBeforeUpgrade;
    }

    public FilterWatchListDeletable<Pod, PodList, Boolean, Watch, Watcher<Pod>> withLabelIn(
            FilterWatchListDeletable<Pod, PodList, Boolean, Watch, Watcher<Pod>> withField, String key,
            String... values) {
        FilterWatchListDeletable<Pod, PodList, Boolean, Watch, Watcher<Pod>> withLabelBeforeUpgrade =
                withField.withLabelIn(key, values);
        return withLabelBeforeUpgrade;
    }

    @Override
    public void close() {
        close(portForward, "portForward");
        close(kubernetesClient, "kubernetesClient");
    }

    private static void close(Closeable closeable, String name) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to close {} properly", name, e);
        }
    }

}
