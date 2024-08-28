package com.ericsson.mxe.modelcatalogueservice.services;

import com.ericsson.mxe.backendservicescommon.config.properties.KubernetesServiceProperties;
import com.ericsson.mxe.backendservicescommon.kubernetes.ServicePortResolverService;
import com.ericsson.mxe.modelcatalogueservice.config.properties.ModelDeploymentServiceProperties;
import com.ericsson.mxe.modelcatalogueservice.dto.ModelDeployment;
import com.ericsson.mxe.modelcatalogueservice.dto.ModelDeploymentModelData;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ModelDeploymentService {

    private final KubernetesServiceProperties kubernetesServiceProperties;
    private final ModelDeploymentServiceProperties modelDeploymentServiceProperties;
    private final ServicePortResolverService servicePortResolver;
    private final RestTemplate restTemplate;

    public ModelDeploymentService(final KubernetesServiceProperties kubernetesServiceProperties,
            final ModelDeploymentServiceProperties modelDeploymentServiceProperties,
            final ServicePortResolverService servicePortResolver, final RestTemplateBuilder restTemplateBuilder) {
        this.kubernetesServiceProperties = kubernetesServiceProperties;
        this.modelDeploymentServiceProperties = modelDeploymentServiceProperties;
        this.servicePortResolver = servicePortResolver;
        this.restTemplate = restTemplateBuilder.build();
    }

    public boolean modelIsDeployed(final String modelId, final String modelVersion) {
        Optional<Integer> port;

        if (kubernetesServiceProperties.isLocalTestEnabled()) {
            port = Optional.of(Integer.valueOf(modelDeploymentServiceProperties.getPortName()));
        } else {
            port = this.servicePortResolver.resolve(this.modelDeploymentServiceProperties.getHostName(),
                    this.modelDeploymentServiceProperties.getPortName(), "tcp");
        }

        if (port.isPresent()) {
            String resourceUrl = "http://" + this.modelDeploymentServiceProperties.getHostName() + ":"
                    + port.get().toString() + "/v1/model-services";

            ResponseEntity<List<ModelDeployment>> response = this.restTemplate.exchange(resourceUrl, HttpMethod.GET,
                    null, new ParameterizedTypeReference<List<ModelDeployment>>() {});

            for (ModelDeployment modelDeployment : Objects.requireNonNull(response.getBody())) {
                for (ModelDeploymentModelData modelData : modelDeployment.getModels()) {
                    if (Objects.equals(modelData.getId(), modelId)
                            && Objects.equals(modelData.getVersion(), modelVersion)) {
                        return true;
                    }
                }
            }

            return false;
        } else {
            throw new RuntimeException();
        }
    }


}
