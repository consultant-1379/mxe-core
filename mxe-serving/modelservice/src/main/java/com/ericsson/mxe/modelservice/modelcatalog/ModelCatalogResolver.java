package com.ericsson.mxe.modelservice.modelcatalog;

import com.ericsson.mxe.modelservice.config.properties.DockerProperties;
import com.ericsson.mxe.modelservice.modelcatalog.dto.ModelPackageData;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public class ModelCatalogResolver {
    private final List<ModelPackageData> packages;
    private final DockerProperties dockerProperties;

    public ModelCatalogResolver(List<ModelPackageData> packages, DockerProperties dockerProperties) {
        this.packages = packages;
        this.dockerProperties = dockerProperties;
    }

    @Nonnull
    public Optional<ModelPackageData> getPackageForImage(String dockerImage) {
        return packages.stream().filter(model -> {
            // System.out.println(String.format("model.internal[%s] dockerhost[%s] model.image[%s] dockerimage[%s]",
            // model.internal, this.dockerProperties.getRegistryHostname(), model.image, dockerImage));
            String dockerHost = this.dockerProperties.getRegistryHostname();

            if (model.internal) {
                if (dockerImage.indexOf(dockerHost) == -1) {
                    // ModelSerice /v2 - the image in manifest container full path
                    return model.image.equals(dockerImage);
                } else {
                    return (dockerHost + "/" + model.image).equals(dockerImage);
                }
            } else {
                // Model onboarded via external docker registry
                return model.image.equals(dockerImage);
            }
        }).findAny();
    }

    @Nonnull
    public Optional<ModelPackageData> getPackageByNameVersion(String packageName, String packageVersion) {
        return packages.stream().filter(model -> model.id.equals(packageName) && model.version.equals(packageVersion))
                .findAny();
    }

    @Nonnull
    public Optional<DockerImage> getImageForPackage(String packageName, String packageVersion) {
        return packages.stream().filter(model -> model.id.equals(packageName) && model.version.equals(packageVersion))
                .findAny()
                .map(model -> model.internal
                        ? new DockerImage(this.dockerProperties.getRegistryHostname() + "/" + model.image,
                                this.dockerProperties.getRegistrySecretName())
                        : new DockerImage(model.image, model.dockerRegistrySecretName));
    }

}
