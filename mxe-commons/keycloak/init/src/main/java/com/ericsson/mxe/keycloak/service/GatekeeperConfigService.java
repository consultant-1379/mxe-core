package com.ericsson.mxe.keycloak.service;

import com.ericsson.mxe.backendservicescommon.kubernetes.KubernetesService;
import com.ericsson.mxe.keycloak.config.properties.GatekeeperConfigServiceProperties;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Secret;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GatekeeperConfigService {

    private static final String GATEKEEPER_YAML = "gatekeeper.yaml";
    private static final Logger logger = LogManager.getLogger(GatekeeperConfigService.class);
    private static final Pattern clientSecretPattern = Pattern.compile("(client-secret:)(?<clientsecret>.*)");

    private final KubernetesService kubernetesService;
    private final GatekeeperConfigServiceProperties gatekeeperConfigServiceProperties;

    public GatekeeperConfigService(GatekeeperConfigServiceProperties gatekeeperConfigServiceProperties,
            KubernetesService kubernetesService) {
        this.kubernetesService = kubernetesService;
        this.gatekeeperConfigServiceProperties = gatekeeperConfigServiceProperties;
        logger.info("GatekeeperConfigServiceProperties:\r\n{}", gatekeeperConfigServiceProperties);
    }

    public void update(final String clientSecret) {
        try {
            final String namespace = kubernetesService.getNamespace();
            final String gateKeeperConfigSecretName = gatekeeperConfigServiceProperties.getConfigSecretName();
            logger.info("Updating [{}] gatekeeper config secret in namespace [{}] with [{}] client secret",
                    gateKeeperConfigSecretName, namespace, clientSecret);

            final V1Secret originalGatekeeperConfigSecret =
                    kubernetesService.readNamespacedSecret(gateKeeperConfigSecretName, namespace);
            final String originalGatekeeperYamlContent = originalGatekeeperConfigSecret.getData().entrySet().stream()
                    .filter((entry -> entry.getKey().equals(GATEKEEPER_YAML)))
                    .map(entry -> new String(entry.getValue())).collect(Collectors.joining());
            final Matcher matcher = clientSecretPattern.matcher(originalGatekeeperYamlContent);

            if (matcher.find()) {
                final String patchedConfigYamlContent = matcher.replaceAll("client-secret: " + clientSecret);
                final Map<String, byte[]> patchedGatekeeperConfigSecretData = new HashMap<>();

                patchedGatekeeperConfigSecretData.put(GATEKEEPER_YAML, patchedConfigYamlContent.getBytes());

                final V1Secret patchedGatekeeperConfigSecret = new V1Secret();
                patchedGatekeeperConfigSecret.setApiVersion(originalGatekeeperConfigSecret.getApiVersion());
                patchedGatekeeperConfigSecret.setType(originalGatekeeperConfigSecret.getType());
                patchedGatekeeperConfigSecret.setData(patchedGatekeeperConfigSecretData);
                patchedGatekeeperConfigSecret.setMetadata(originalGatekeeperConfigSecret.getMetadata());
                patchedGatekeeperConfigSecret.setStringData(originalGatekeeperConfigSecret.getStringData());

                kubernetesService.replaceNamespacedSecret(gateKeeperConfigSecretName, namespace,
                        patchedGatekeeperConfigSecret);
                logger.info(
                        "The client-secret field in gatekeeper config secret [{}] is successfully set to [{}]\r\n---\r\n{}\r\n---",
                        gateKeeperConfigSecretName, clientSecret, patchedConfigYamlContent);
            } else {
                logger.error("Could not found client-secret key in [{}] gatekeeper config secret data\r\n{}",
                        gateKeeperConfigSecretName, originalGatekeeperYamlContent);
            }

            Thread.sleep(20000);
        } catch (ApiException e) {
            logger.error("Could not update gatekeeper config secret", e);
        } catch (InterruptedException e) {
            logger.warn("Sleep was interrupted, gatekeeper config secret maybe not updated properly", e);
        }

    }
}
