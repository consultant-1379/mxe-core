package com.ericsson.mxe.backendservicescommon.config.properties;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kubernetes-service")
public class KubernetesServiceProperties {
    private final boolean localTestEnabled;
    private final String localTestNamespace;

    public KubernetesServiceProperties(boolean localTestEnabled, String localTestNamespace, int localTestServicePort) {
        this.localTestEnabled = localTestEnabled;
        this.localTestNamespace = localTestNamespace;
        this.localTestServicePort = localTestServicePort;
    }

    private final int localTestServicePort;

    public boolean isLocalTestEnabled() {
        return localTestEnabled;
    }

    public String getLocalTestNamespace() {
        return localTestNamespace;
    }

    public int getLocalTestServicePort() {
        return localTestServicePort;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
