package com.ericsson.mxe.keycloak.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientScopesResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.ProtocolMappersResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import com.ericsson.mxe.keycloak.config.MxeRealmConfig;
import com.ericsson.mxe.keycloak.config.properties.KeycloakInitServiceProperties;
import com.ericsson.mxe.keycloak.config.properties.ServiceMeshProperties;

@Service
@EnableRetry
public class KeycloakInitService {

    public enum KeycloakResource {
        Realm, Client, ClientScope, User, ProtocolMapper, Role, Group, GroupMembership
    }

    private static final Logger logger = LogManager.getLogger(KeycloakInitService.class);
    private static final int MAX_ATTEMPTS = 180;
    private static final int RETRY_DELAY = 10000;

    private final RealmRepresentation realmRepresentation;
    private final List<ClientRepresentation> clientRepresentations;
    private final ClientScopeRepresentation clientScopeRepresentation;
    private final List<ProtocolMapperRepresentation> protocolMapperRepresentations;
    private final UserRepresentation userRepresentation;
    private final CredentialRepresentation credentialRepresentation;
    private final GroupRepresentation defaultGroup;
    private final List<RoleRepresentation> defaultRoles;
    private final KeycloakInitServiceProperties keycloakInitServiceProperties;
    private final ServiceMeshProperties serviceMeshProperties;

    private final Map<KeycloakResource, String> keycloakInitPhases;

    public KeycloakInitService(RealmRepresentation realmRepresentation,
            List<ClientRepresentation> clientRepresentations, ClientScopeRepresentation clientScopeRepresentation,
            List<ProtocolMapperRepresentation> protocolMapperRepresentations, UserRepresentation userRepresentation,
            CredentialRepresentation credentialRepresentation, GroupRepresentation defaultGroup,
            List<RoleRepresentation> defaultRoles, KeycloakInitServiceProperties keycloakInitServiceProperties,
            ServiceMeshProperties serviceMeshProperties) {
        this.realmRepresentation = realmRepresentation;
        this.clientRepresentations = clientRepresentations;
        this.clientScopeRepresentation = clientScopeRepresentation;
        this.protocolMapperRepresentations = protocolMapperRepresentations;
        this.userRepresentation = userRepresentation;
        this.credentialRepresentation = credentialRepresentation;
        this.defaultGroup = defaultGroup;
        this.defaultRoles = defaultRoles;
        this.keycloakInitServiceProperties = keycloakInitServiceProperties;
        this.serviceMeshProperties = serviceMeshProperties;
        logger.info("KeycloakInitServiceProperties:\r\n{}", keycloakInitServiceProperties);

        keycloakInitPhases = new HashMap<>();
    }

    @Retryable(value = {Exception.class}, maxAttempts = MAX_ATTEMPTS, backoff = @Backoff(delay = RETRY_DELAY))
    public boolean isInitNeeded() {
        boolean realmExists;

        try (final Keycloak keycloak = getKeycloak()) {
            realmExists = keycloak.realms().findAll().stream()
                    .anyMatch(realm -> realm.getRealm().equals(realmRepresentation.getRealm()));
        } catch (Exception exception) {
            logger.error(exception);
            logger.error("Could not check realms, retrying in {} seconds...", RETRY_DELAY / 1000);
            throw exception;
        }

        if (realmExists) {
            logger.info("Realm [{}] exists, skipping initialization...", realmRepresentation.getRealm());
            return false;
        }

        return true;
    }

    @Retryable(value = {Exception.class}, maxAttempts = MAX_ATTEMPTS, backoff = @Backoff(delay = RETRY_DELAY))
    public void createResources(Set<KeycloakResource> requestedResources) {

        try (final Keycloak keycloak = getKeycloak()) {
            final RealmResource realmResource = keycloak.realm(realmRepresentation.getRealm());

            if (requestedResources.contains(KeycloakResource.Realm)
                    && Objects.isNull(keycloakInitPhases.get(KeycloakResource.Realm))) {
                createRealm();
                keycloakInitPhases.put(KeycloakResource.Realm, "");
            }

            if (requestedResources.contains(KeycloakResource.ClientScope)
                    && Objects.isNull(keycloakInitPhases.get(KeycloakResource.ClientScope))) {
                final String clientScopeId = createClientScope();
                logger.info("Adding [{}] [{}] resource to the defaults", clientScopeRepresentation.getName(),
                        KeycloakResource.ClientScope);
                realmResource.addDefaultDefaultClientScope(clientScopeId);
                keycloakInitPhases.put(KeycloakResource.ClientScope, clientScopeId);
            }

            if (requestedResources.contains(KeycloakResource.ProtocolMapper)
                    && Objects.isNull(keycloakInitPhases.get(KeycloakResource.ProtocolMapper))) {
                createProtocolMappers();
                keycloakInitPhases.put(KeycloakResource.ProtocolMapper, StringUtils.EMPTY);
            }

            if (requestedResources.contains(KeycloakResource.Client)
                    && Objects.isNull(keycloakInitPhases.get(KeycloakResource.Client))) {
                createClients();
                keycloakInitPhases.put(KeycloakResource.Client, StringUtils.EMPTY);
            }

            if (requestedResources.contains(KeycloakResource.User)
                    && Objects.isNull(keycloakInitPhases.get(KeycloakResource.User))) {
                createUser();
                keycloakInitPhases.put(KeycloakResource.User, StringUtils.EMPTY);
            }
            if (requestedResources.contains(KeycloakResource.Role)
                    && Objects.isNull(keycloakInitPhases.get(KeycloakResource.Role))) {
                boolean created = createDefaultRoles();
                keycloakInitPhases.put(KeycloakResource.Role, Boolean.toString(created));
            }
            if (requestedResources.contains(KeycloakResource.Group)
                    && isResultOf(KeycloakResource.Role, Boolean.TRUE.toString())
                    && Objects.isNull(keycloakInitPhases.get(KeycloakResource.Group))) {
                createDefaultGroup();
                keycloakInitPhases.put(KeycloakResource.Group, StringUtils.EMPTY);
            }
            if (requestedResources.contains(KeycloakResource.GroupMembership)
                    && isResultOf(KeycloakResource.Role, Boolean.TRUE.toString())
                    && Objects.isNull(keycloakInitPhases.get(KeycloakResource.GroupMembership))) {
                addDefaultGroupToUser();
                keycloakInitPhases.put(KeycloakResource.GroupMembership, StringUtils.EMPTY);
            }
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = MAX_ATTEMPTS, backoff = @Backoff(delay = RETRY_DELAY))
    public String getClientSecret() {
        try (final Keycloak keycloak = getKeycloak()) {
            final RealmResource realmResource = keycloak.realm(realmRepresentation.getRealm());
            final ClientsResource clientsResource = realmResource.clients();

            final String keycloakClientId = clientsResource.findAll().stream()
                    .filter(c -> c.getClientId().equals(MxeRealmConfig.MXE_CLIENT)).findAny().orElseThrow().getId();
            final String clientSecret = clientsResource.get(keycloakClientId).getSecret().getValue();
            Objects.requireNonNull(clientSecret);
            logger.info("Client [{}] secret is [{}]", MxeRealmConfig.MXE_CLIENT, clientSecret);

            return clientSecret;
        }
    }

    private void createRealm() {
        try (final Keycloak keycloak = getKeycloak()) {
            final RealmsResource realmsResource = keycloak.realms();
            logger.info("\tCreating [{}] resource with name: {}", KeycloakResource.Realm,
                    realmRepresentation.getRealm());
            realmsResource.create(realmRepresentation);
            logger.info("\tRealm [{}] has been created.", realmRepresentation.getRealm());
            logger.info("--->");
        } catch (Exception exception) {
            logger.info("--->");
            logger.error(exception);
            logger.error("Failed to create [{}] realm, retrying in {} seconds...", realmRepresentation.getRealm(),
                    RETRY_DELAY / 1000);
            throw exception;
        }
    }

    private void createClients() {
        try (final Keycloak keycloak = getKeycloak()) {
            final ClientsResource clientsResource = keycloak.realm(realmRepresentation.getRealm()).clients();
            createAllResources(clientsResource::findAll, clientRepresentations, ClientRepresentation::getName,
                    clientsResource::create, KeycloakResource.Client);
        }
    }

    private String createClientScope() {
        String clientScopeId;
        try (final Keycloak keycloak = getKeycloak()) {
            final ClientScopesResource clientScopesResource =
                    keycloak.realm(realmRepresentation.getRealm()).clientScopes();
            clientScopeId = createResource(clientScopeRepresentation.getName(),
                    () -> clientScopesResource.create(clientScopeRepresentation), KeycloakResource.ClientScope);
        }

        return clientScopeId;
    }

    private String getClientScopeId() {
        try (final Keycloak keycloak = getKeycloak()) {
            return keycloak.realm(realmRepresentation.getRealm()).clientScopes().findAll().stream()
                    .filter(clientScope -> clientScope.getName().equals(clientScopeRepresentation.getName()))
                    .findFirst().orElseThrow().getId();
        }
    }

    private void createProtocolMappers() {
        try (final Keycloak keycloak = getKeycloak()) {

            final ProtocolMappersResource protocolMappersResource = keycloak.realm(realmRepresentation.getRealm())
                    .clientScopes().get(getClientScopeId()).getProtocolMappers();
            createAllResources(protocolMappersResource::getMappers, protocolMapperRepresentations,
                    ProtocolMapperRepresentation::getName, protocolMappersResource::createMapper,
                    KeycloakResource.ProtocolMapper);
        }
    }

    private void createUser() {
        try (final Keycloak keycloak = getKeycloak()) {
            final UsersResource userResource = keycloak.realm(realmRepresentation.getRealm()).users();
            final String userId = createResource(userRepresentation.getUsername(),
                    () -> userResource.create(userRepresentation), KeycloakResource.User);
            logger.info("Setting password credential for user [{}]", userRepresentation.getUsername());
            userResource.get(userId).resetPassword(credentialRepresentation);
        }
    }

    private Keycloak getKeycloak() {
        char[] allPassword = "password".toCharArray();
        SSLContext sslContext = null;

        if (serviceMeshProperties.isMtlsEnabled()) {
            try {
                sslContext = SSLContextBuilder.create()
                        .loadKeyMaterial(ResourceUtils.getFile("file:" + System.getenv("KEYSTORE_FILE_PATH")),
                                allPassword, allPassword)
                        .loadTrustMaterial(ResourceUtils.getFile("file:" + System.getenv("TRUSTCA_FILE_PATH")),
                                allPassword)
                        .build();
            } catch (Exception e) {
                logger.error("getKeycloak() sslContext failed", e);
                logger.error("using non-ssl context.");
            }
        }

        final ResteasyClient restEasyCLient = new ResteasyClientBuilderImpl().sslContext(sslContext)
                .connectionPoolSize(1).connectTimeout(5, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();

        return KeycloakBuilder.builder().resteasyClient(restEasyCLient)
                .serverUrl(keycloakInitServiceProperties.getServerUrl()).realm("master")
                .grantType(OAuth2Constants.PASSWORD).clientId("admin-cli")
                .username(keycloakInitServiceProperties.getUsername())
                .password(keycloakInitServiceProperties.getPassword()).build();
    }

    private boolean createDefaultRoles() {
        boolean result = false;
        for (RoleRepresentation role : defaultRoles) {
            boolean added = addRole(role);
            result = result || added;
        }
        return result;
    }

    private boolean addRole(RoleRepresentation newRole) {
        try (final Keycloak keycloak = getKeycloak()) {
            final RolesResource rolesResource = keycloak.realm(realmRepresentation.getRealm()).roles();
            if (rolesResource.list().stream().anyMatch(role -> role.getName().equals(newRole.getName()))) {
                return false;
            }
            final RoleResource roleResource = rolesResource.get(newRole.getName());
            RoleRepresentation fetchedRoleRepresentation;
            try {
                fetchedRoleRepresentation = roleResource.toRepresentation();
            } catch (NotFoundException ex) {
                logger.info("Creating role {}", newRole.getName());
                rolesResource.create(newRole);
                fetchedRoleRepresentation = rolesResource.get(newRole.getName()).toRepresentation();
                logger.info("Role created.");
            }
            if (fetchedRoleRepresentation.getAttributes().isEmpty() && !newRole.getAttributes().isEmpty()) {
                logger.info("Setting role attributes.");
                fetchedRoleRepresentation.setAttributes(newRole.getAttributes());
                roleResource.update(fetchedRoleRepresentation);
                logger.info("Attributes set.");
            }
        }
        return true;
    }

    private void createDefaultGroup() {
        try (final Keycloak keycloak = getKeycloak()) {
            final GroupsResource groupsResource = keycloak.realm(realmRepresentation.getRealm()).groups();
            final RolesResource rolesResource = keycloak.realm(realmRepresentation.getRealm()).roles();
            List<GroupRepresentation> fetched = groupsResource.groups(defaultGroup.getName(), 0, 1);
            if (fetched.isEmpty()) {
                createResource(defaultGroup.getName(), () -> groupsResource.add(defaultGroup), KeycloakResource.Group);
                fetched = groupsResource.groups(defaultGroup.getName(), 0, 1);
            }
            String id = fetched.get(0).getId();
            GroupResource groupResource = groupsResource.group(id);
            List<String> effectiveRoleNames = groupResource.roles().realmLevel().listEffective().stream()
                    .map(RoleRepresentation::getName).collect(Collectors.toList());
            boolean rolesAreAssigned = effectiveRoleNames.containsAll(defaultGroup.getRealmRoles());
            if (!rolesAreAssigned) {
                final List<RoleRepresentation> roles = defaultGroup.getRealmRoles().stream()
                        .map(roleName -> rolesResource.get(roleName).toRepresentation())
                        .filter(roleRepresentation -> !effectiveRoleNames.contains(roleRepresentation.getName()))
                        .collect(Collectors.toList());
                logger.info("Adding roles {} to group {}",
                        roles.stream().map(RoleRepresentation::getName).collect(Collectors.joining(", ")),
                        defaultGroup.getName());
                groupResource.roles().realmLevel().add(roles);
                logger.info("Roles added.");
            }
        }
    }

    private void addDefaultGroupToUser() {
        try (final Keycloak keycloak = getKeycloak()) {
            final UsersResource usersResource = keycloak.realm(realmRepresentation.getRealm()).users();
            final GroupsResource groupsResource = keycloak.realm(realmRepresentation.getRealm()).groups();
            usersResource.list().stream().map(userResource -> usersResource.get(userResource.getId()))
                    .filter(userResource -> userResource.groups(defaultGroup.getName(), null, null).isEmpty())
                    .forEach(userResource -> {
                        logger.info("Joining user {} to group {}", userRepresentation.getUsername(),
                                defaultGroup.getName());
                        userResource.joinGroup(groupsResource.groups(defaultGroup.getName(), 0, 1).get(0).getId());
                        logger.info("Joined successfully.");
                    });
        }
    }

    private String createResource(final String name, final Supplier<Response> createFunction,
            final KeycloakResource keycloakResource) {
        String resourceId;
        try {

            logger.info("<---");
            logger.info("\tCreating [{}] resource with name: {}", keycloakResource, name);
            final Response createResponse = createFunction.get();

            logger.info("\t[{}] resource creating response: {} {}", keycloakResource, createResponse.getStatus(),
                    createResponse.getStatusInfo());
            if (createResponse.getStatusInfo() != Response.Status.CREATED) {
                if (createResponse.hasEntity()) {
                    logger.error(createResponse.readEntity(String.class));
                }
                throw new IllegalArgumentException("Resource could not be created");
            }

            logger.info("\t[{}] resource location: {}", keycloakResource, createResponse.getLocation());
            resourceId = createResponse.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            logger.info("\t[{}] resource id: {}", keycloakResource, resourceId);
            logger.info("--->");

            return resourceId;
        } catch (Exception exception) {
            logger.info("--->");
            logger.error(exception);
            logger.error("Failed to create [{}] resource, retrying in {} seconds", keycloakResource,
                    RETRY_DELAY / 1000);
            throw exception;
        }
    }

    public <T> void createAllResources(final Supplier<List<T>> listAlreadyCreatedFunction, final List<T> resources,
            Function<T, String> getNameFunction, final Function<T, Response> createFunction,
            final KeycloakResource keycloakResource) {
        final List<T> alreadyCreated = listAlreadyCreatedFunction.get();
        resources.stream()
                .filter(resource -> alreadyCreated.stream().map(getNameFunction)
                        .noneMatch(uniqueProperty -> uniqueProperty.equals(getNameFunction.apply(resource))))
                .forEach(resource -> createResource(getNameFunction.apply(resource),
                        () -> createFunction.apply(resource), keycloakResource));
    }

    private boolean isResultOf(KeycloakResource resource, String expected) {
        String result = keycloakInitPhases.get(resource);
        if (Objects.nonNull(result)) {
            return result.equals(expected);
        }
        return false;
    }
}
