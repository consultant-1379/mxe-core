package com.ericsson.mxe.backendservicescommon.kubernetes;

import java.util.Hashtable;
import java.util.Optional;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ericsson.mxe.backendservicescommon.config.properties.KubernetesServiceProperties;

@Service
public class ServicePortResolverService {
    private final Logger logger = LoggerFactory.getLogger(ServicePortResolverService.class);

    private final KubernetesService kubernetesService;
    private final KubernetesServiceProperties kubernetesServiceProperties;
    private final String initialContextFactory;

    @Autowired
    public ServicePortResolverService(final KubernetesService kubernetesService,
            final KubernetesServiceProperties kubernetesServiceProperties) {
        this.kubernetesService = kubernetesService;
        this.kubernetesServiceProperties = kubernetesServiceProperties;
        this.initialContextFactory = "com.sun.jndi.dns.DnsContextFactory";
    }

    public ServicePortResolverService(@Autowired final KubernetesService kubernetesService,
            final KubernetesServiceProperties kubernetesServiceProperties, final String initialContextFactory) {
        this.kubernetesService = kubernetesService;
        this.kubernetesServiceProperties = kubernetesServiceProperties;
        this.initialContextFactory = initialContextFactory;
    }

    public Optional<Integer> resolve(final String serviceName, final String portName, final String portProtocol) {
        if (kubernetesServiceProperties.isLocalTestEnabled()) {
            final int servicePort = kubernetesServiceProperties.getLocalTestServicePort();
            logger.info("Local test is enabled, using user-defined service port: {}", servicePort);
            return Optional.of(servicePort);
        }

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, this.initialContextFactory);
        env.put(Context.PROVIDER_URL, "dns:");

        try {
            DirContext ctx = new InitialDirContext(env);
            String namespace = this.kubernetesService.getNamespace();
            Attributes attrs = ctx.getAttributes(
                    "_" + portName + "._" + portProtocol + "." + serviceName + "." + namespace + ".svc.cluster.local",
                    new String[] {"SRV"});

            final NamingEnumeration<? extends Attribute> attributes = attrs.getAll();

            while (attributes.hasMore()) {
                final Attribute attr = attributes.next();

                final NamingEnumeration<?> values = attr.getAll();

                while (values.hasMore()) {
                    final Object value = values.next();

                    if (value instanceof String string) {
                        final String[] parts = string.split(" ");

                        return Optional.of(Integer.valueOf(parts[2]));
                    }
                }
            }

            return Optional.empty();
        } catch (NamingException e) {
            logger.error(e.getMessage(), e);

            return Optional.empty();
        }
    }
}
