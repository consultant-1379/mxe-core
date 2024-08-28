package com.ericsson.mxe.modelservice.deployer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.ericsson.mxe.modelservice.config.properties.DockerProperties;
import com.ericsson.mxe.modelservice.config.properties.SeldonProperties;
import com.ericsson.mxe.modelservice.config.properties.ServiceMeshProperties;
import com.ericsson.mxe.modelservice.controller.input.SeldonManifestHolder;
import com.ericsson.mxe.modelservice.controller.input.SeldonManifestRequest;
import com.ericsson.mxe.modelservice.controller.output.GetSeldonManifestResponse;
import com.ericsson.mxe.modelservice.deployer.utils.MxeDeployerUtils;
import com.ericsson.mxe.modelservice.modelcatalog.ModelCatalogResolver;

@Service
public class ManifestPreprocessor {
    private final SeldonProperties seldonProperties;
    private final DockerProperties dockerProperties;
    private final ServiceMeshProperties serviceMeshProperties;

    ManifestPreprocessor(final SeldonProperties seldonProperties, final DockerProperties dockerProperties,
            final ServiceMeshProperties serviceMeshProperties) {
        this.seldonProperties = seldonProperties;
        this.dockerProperties = dockerProperties;
        this.serviceMeshProperties = serviceMeshProperties;
    }

    /*
     * CREATE DEPLOYMENT
     */
    public void createDeploymentPreProcessing(SeldonManifestRequest manifestRequest, String namespace, String userId,
            String userName, ModelCatalogResolver resolver) {
        populateMxeMetadata(manifestRequest, namespace, userId, userName, resolver, null);
    }

    /*
     * PATCH DEPLOYMENT
     */
    public void patchDeploymentPreProcessing(SeldonManifestRequest manifestRequest, String namespace, String userId,
            String userName, ModelCatalogResolver resolver, GetSeldonManifestResponse serviceData) {
        populateMxeMetadata(manifestRequest, namespace, userId, userName, resolver, serviceData);
    }

    private void populateMxeMetadata(SeldonManifestRequest manifestRequest, String namespace, String userId,
            String userName, ModelCatalogResolver resolver, GetSeldonManifestResponse serviceData) {
        SeldonManifestHolder seldonManifestHolder = manifestRequest.getSeldonManifestHolder();
        Map<String, Object> jsonObject = Optional.ofNullable(seldonManifestHolder).map(holder -> holder.getJsonObject())
                .orElse(new LinkedHashMap<>());

        final String type = MxeDeployerUtils.getTypeFromRequest(manifestRequest.getRequestModelData());

        Map<String, Object> labels = Optional.ofNullable((Map) jsonObject.get("metadata"))
                .map(metadata -> (Map) metadata.get("labels")).orElse(new LinkedHashMap<>());
        labels.put("mxe/createdbyuserid", userId);
        labels.put("mxe/createdbyusername", userName);
        labels.put("app", "seldon");
        labels.put("mxe/component", "mxe-modeldeployment");
        labels.put("mxe/deploymenttype", type);
        labels.put("mxe/templateversion", "2.0");

        Optional.ofNullable((Map) jsonObject.get("metadata")).ifPresent(meta -> {
            meta.put("namespace", namespace);
            meta.put("labels", labels);
        });

        seldonManifestHolder.enrichContainerImageFromCatalog(resolver);

        seldonManifestHolder.enrichDeploymentSpec(dockerProperties, serviceMeshProperties);
    }

}
