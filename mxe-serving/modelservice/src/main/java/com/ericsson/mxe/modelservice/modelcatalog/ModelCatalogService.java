package com.ericsson.mxe.modelservice.modelcatalog;

import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.backendservicescommon.kubernetes.ServicePortResolverService;
import com.ericsson.mxe.modelservice.modelcatalog.dto.ModelPackageData;
import com.ericsson.mxe.securitycommon.accesscontrol.AccessControl;
import com.ericsson.mxe.securitycommon.accesscontrol.Action;
import com.ericsson.mxe.securitycommon.accesscontrol.TargetType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ModelCatalogService {
    private final ServicePortResolverService servicePortResolverService;
    private final ModelCatalogueServiceConfig modelCatalogueServiceConfig;
    private final AccessControl accessControl;

    ModelCatalogService(final ServicePortResolverService servicePortResolverService,
            final ModelCatalogueServiceConfig modelCatalogueServiceConfig, final AccessControl accessControl) {
        this.servicePortResolverService = servicePortResolverService;
        this.modelCatalogueServiceConfig = modelCatalogueServiceConfig;
        this.accessControl = accessControl;
    }

    public List<ModelPackageData> getModelCatalog(boolean withModelAccessControl) {
        Optional<Integer> port = servicePortResolverService.resolve(modelCatalogueServiceConfig.getServiceName(),
                modelCatalogueServiceConfig.getPortName(), "tcp");

        if (port.isPresent()) {
            RestTemplate restTemplate = new RestTemplate();
            String resourceUrl = "http://" + modelCatalogueServiceConfig.getServiceName() + ":" + port.get().toString()
                    + "/v1/models";

            ResponseEntity<List<ModelPackageData>> response = restTemplate.exchange(resourceUrl, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<ModelPackageData>>() {});

            if (withModelAccessControl) {
                return response.getBody().stream()
                        .filter(m -> accessControl.isAccessAllowed(m.id, TargetType.MODELS, Action.ALL))
                        .collect(Collectors.toList());
            } else {
                return response.getBody();
            }
        } else {
            throw new MxeInternalException(
                    "Service port cannot be resolved:\n" + "\tService: " + modelCatalogueServiceConfig.getServiceName()
                            + "\tPort: " + modelCatalogueServiceConfig.getPortName());
        }
    }
}
