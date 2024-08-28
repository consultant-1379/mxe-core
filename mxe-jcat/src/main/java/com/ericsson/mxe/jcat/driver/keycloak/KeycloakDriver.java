package com.ericsson.mxe.jcat.driver.keycloak;

import com.ericsson.mxe.jcat.driver.kubernetes.KubernetesDriver;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class KeycloakDriver extends KubernetesDriver {

    public static final int DEFAULT_KEYCLOAK_PORT = 8080;
    public static final int DEFAULT_KEYCLOAK_FORWARDED_PORT = 10000 + DEFAULT_KEYCLOAK_PORT;

    private static final String REALM_MASTER = "master";
    private static final String REALM_MXE = "mxe";

    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakDriver.class);

    public static final String LABELSELECTOR_KEYCLOAK_KEY = "app.kubernetes.io/name";
    public static final List<String> LABELSELECTOR_KEYCLOAK_VALUES = Arrays.asList("eric-sec-access-mgmt");

    private final String url;
    private final String user;
    private final String password;

    public enum RolePermission {
        ALL("all"), READ("read");

        String value;

        RolePermission(String value) {
            this.value = value;
        }
    }

    public enum RoleType {
        MODEL("model"), MODEL_SERVICE("model-service"), ALL("all");

        String value;

        RoleType(String value) {
            this.value = value;
        }
    }

    public KeycloakDriver(final String hostName, boolean portForward) throws MalformedURLException {
        this(hostName, null, null, portForward);
    }

    public KeycloakDriver(final String hostName, final String user, final String password, boolean portForward)
            throws MalformedURLException {
        this.url = "http://" + hostName + ":" + (portForward ? DEFAULT_KEYCLOAK_FORWARDED_PORT : DEFAULT_KEYCLOAK_PORT)
                + "/auth";
        this.user = user;
        this.password = password;
        if (portForward && !servicePortForward(LABELSELECTOR_KEYCLOAK_KEY, LABELSELECTOR_KEYCLOAK_VALUES, null,
                DEFAULT_KEYCLOAK_PORT, DEFAULT_KEYCLOAK_FORWARDED_PORT)) {
            throw new RuntimeException("Keycloak port forward failed");
        }
    }

    private Keycloak getKeycloak() {
        ResteasyClient restEasyCLient = new ResteasyClientBuilderImpl().sslContext(null).connectionPoolSize(1)
                .connectTimeout(5, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();

        return KeycloakBuilder.builder().resteasyClient(restEasyCLient).serverUrl(url).realm(REALM_MASTER)
                .grantType(OAuth2Constants.PASSWORD).clientId("admin-cli").username(user).password(password).build();
    }

    public Optional<String> createUser(String userName, String userPassword) {
        return createUserWithRole(userName, userPassword, null);
    }

    public Optional<String> createUserWithRole(String userName, String userPassword, String roleName) {
        try (Keycloak keycloak = getKeycloak()) {
            final UserRepresentation userRepresentation = new UserRepresentation();
            userRepresentation.setEnabled(true);
            userRepresentation.setUsername(userName);
            userRepresentation.setFirstName("");
            userRepresentation.setLastName("");
            userRepresentation.setEmail("");
            final UsersResource usersResource = keycloak.realm(REALM_MXE).users();
            LOGGER.info("Creating user with name: {}", userName);
            final Response createResponse = usersResource.create(userRepresentation);
            if (Response.Status.fromStatusCode(createResponse.getStatus())
                    .getFamily() != Response.Status.Family.SUCCESSFUL) {
                LOGGER.error("Failed to create user resource: " + createResponse.getStatusInfo().getReasonPhrase());
                return Optional.empty();
            }
            String userId = createResponse.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            LOGGER.info("Setting password credential for user {} to {}", userName, userPassword);
            final CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setTemporary(false);
            credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
            credentialRepresentation.setValue(userPassword);
            usersResource.get(userId).resetPassword(credentialRepresentation);

            Optional.ofNullable(roleName).ifPresent(role -> {
                LOGGER.info("Adding role for user {} to {}", role, userName);
                addRoleToUser(role, userName);
            });

            LOGGER.info("User {} successfully created", userName);
            return Optional.of(userId);
        } catch (Exception exception) {
            LOGGER.error("Failed to create user resource", exception);
            return Optional.empty();
        }
    }

    public boolean deleteUser(String userName) {
        try (Keycloak keycloak = getKeycloak()) {
            final UsersResource usersResource = keycloak.realm(REALM_MXE).users();
            LOGGER.info("Removing user with name: {}", userName);
            final Response deleteResponse = usersResource.delete(usersResource.search(userName, 0, 1).get(0).getId());
            if (Response.Status.fromStatusCode(deleteResponse.getStatus())
                    .getFamily() != Response.Status.Family.SUCCESSFUL) {
                LOGGER.error("Failed to delete user resource");
                return false;
            }
            LOGGER.info("User {} successfully deleted", userName);
            return true;
        } catch (Exception exception) {
            LOGGER.error("Failed to delete user resource", exception);
            return false;
        }
    }

    public boolean createRole(String roleName, String domain, RolePermission permission, RoleType type) {
        try (Keycloak keycloak = getKeycloak()) {
            final RoleRepresentation role = new RoleRepresentation();
            role.setName(roleName);
            Map<String, List<String>> roleAttributes = new HashMap() {
                {
                    put(domain, Arrays.asList(type.value + ":" + permission.value));
                }
            };
            role.setAttributes(roleAttributes);
            RolesResource rolesResource = keycloak.realm(REALM_MXE).roles();
            LOGGER.info("Creating role with name {}", roleName);
            rolesResource.create(role);
            LOGGER.info("Setting role {} attributes to [{}]", roleName,
                    roleAttributes.entrySet().stream()
                            .map(attribute -> attribute.getKey() + ":"
                                    + attribute.getValue().stream().collect(Collectors.joining(",")))
                            .collect(Collectors.joining(", ")));
            RoleResource roleResource = rolesResource.get(role.getName());
            roleResource.update(role);
            LOGGER.info("Check role creation result");
            return checkRoleExist(rolesResource, roleName, true);
        } catch (Exception exception) {
            LOGGER.error("Failed to create role resource", exception);
            return false;
        }
    }

    public boolean deleteRole(String roleName) {
        try (Keycloak keycloak = getKeycloak()) {
            RolesResource rolesResource = keycloak.realm(REALM_MXE).roles();
            LOGGER.info("Delete role with name {}", roleName);
            rolesResource.deleteRole(roleName);
            LOGGER.info("Check role delete result");
            return !checkRoleExist(rolesResource, roleName, false);
        } catch (Exception exception) {
            LOGGER.error("Failed to delete role resource", exception);
            return false;
        }
    }

    private boolean checkRoleExist(RolesResource rolesResource, String roleName, boolean shouldExist) {
        List<String> fetchedRoleNames = rolesResource.list(roleName, 0, 1).stream().map(role -> role.getName())
                .filter(s -> s.equals(roleName)).collect(Collectors.toList());
        if (!fetchedRoleNames.isEmpty()) {
            if (!shouldExist) {
                LOGGER.error("Role {} found", roleName);
            } else {
                LOGGER.info("Role {} found", roleName);
            }
            return true;
        } else {
            if (shouldExist) {
                LOGGER.error("Role {} not found", roleName);
            } else {
                LOGGER.info("Role {} not found", roleName);
            }
            return false;
        }
    }

    public boolean addRoleToUser(String roleName, String userName) {
        try (Keycloak keycloak = getKeycloak()) {
            final RolesResource rolesResource = keycloak.realm(REALM_MXE).roles();
            final RoleResource roleResource = rolesResource.get(roleName);
            final UsersResource usersResource = keycloak.realm(REALM_MXE).users();

            LOGGER.info("Add role {} to user {}", roleName, userName);
            usersResource.search(userName).stream().map(userResource -> usersResource.get(userResource.getId()))
                    .forEach(userResource -> userResource.roles().realmLevel()
                            .add(Arrays.asList(roleResource.toRepresentation())));
            LOGGER.info("Role {} added to user {}", roleName, userName);
            return true;
        } catch (Exception exception) {
            LOGGER.error("Failed to add role to user", exception);
            return false;
        }
    }

    public boolean revokeRoleFromUser(String roleName, String userName) {
        try (Keycloak keycloak = getKeycloak()) {
            final RolesResource rolesResource = keycloak.realm(REALM_MXE).roles();
            final RoleResource roleResource = rolesResource.get(roleName);
            final UsersResource usersResource = keycloak.realm(REALM_MXE).users();

            LOGGER.info("Revoke role {} from user {}", roleName, userName);
            usersResource.search(userName).stream().map(userResource -> usersResource.get(userResource.getId()))
                    .forEach(userResource -> userResource.roles().realmLevel()
                            .remove(Arrays.asList(roleResource.toRepresentation())));
            LOGGER.info("Role {} removed from user {}", roleName, userName);
            return true;
        } catch (Exception exception) {
            LOGGER.error("Failed to revoke role from user", exception);
            return false;
        }
    }

    public Optional<String> createGroup(String groupName) {
        try (final Keycloak keycloak = getKeycloak()) {
            final GroupsResource groupsResource = keycloak.realm(REALM_MXE).groups();

            final GroupRepresentation groupRepresentation = new GroupRepresentation();
            groupRepresentation.setName(groupName);

            LOGGER.info("Creating group with name {}", groupName);
            Response createResponse = groupsResource.add(groupRepresentation);
            if (Response.Status.fromStatusCode(createResponse.getStatus())
                    .getFamily() != Response.Status.Family.SUCCESSFUL) {
                LOGGER.error("Failed to create group resource: " + createResponse.getStatusInfo().getReasonPhrase());
                return Optional.empty();
            }


            LOGGER.info("Check group creation result");
            if (!checkGroupExist(groupsResource, groupName, true)) {
                return Optional.empty();
            } else {
                List<GroupRepresentation> fetched = groupsResource.groups(groupName, 0, 1);
                String id = fetched.get(0).getId();
                return Optional.of(id);
            }
        } catch (Exception exception) {
            LOGGER.error("Failed create group", exception);
            return Optional.empty();
        }
    }

    public boolean deleteGroup(String groupName) {
        try (final Keycloak keycloak = getKeycloak()) {
            final GroupsResource groupsResource = keycloak.realm(REALM_MXE).groups();
            GroupResource groupResource = groupsResource.group(groupsResource.groups(groupName, 0, 1).get(0).getId());
            LOGGER.info("Delete group with name {}", groupName);
            groupResource.remove();
            LOGGER.info("Check group delete result");
            return !checkGroupExist(groupsResource, groupName, false);
        } catch (Exception exception) {
            LOGGER.error("Failed delete group", exception);
            return false;
        }
    }

    private boolean checkGroupExist(GroupsResource groupsResource, String groupName, boolean shouldExist) {
        List<GroupRepresentation> fetched = groupsResource.groups(groupName, 0, 1);
        if (!fetched.isEmpty()) {
            if (!shouldExist) {
                LOGGER.error("Group {} found", groupName);
            } else {
                LOGGER.info("Group {} found", groupName);
            }
            return true;
        } else {
            if (shouldExist) {
                LOGGER.error("Group {} not found", groupName);
            } else {
                LOGGER.info("Group {} not found", groupName);
            }
            return false;
        }
    }

    public boolean addRoleToGroup(String roleName, String groupName) {
        try (final Keycloak keycloak = getKeycloak()) {
            final RolesResource rolesResource = keycloak.realm(REALM_MXE).roles();
            final GroupsResource groupsResource = keycloak.realm(REALM_MXE).groups();
            List<GroupRepresentation> fetched = groupsResource.groups(groupName, 0, 1);
            String id = fetched.get(0).getId();
            GroupResource groupResource = groupsResource.group(id);
            RoleRepresentation role = rolesResource.get(roleName).toRepresentation();

            LOGGER.info("Adding role {} to group {}", roleName, groupName);
            groupResource.roles().realmLevel().add(Arrays.asList(role));
            LOGGER.info("Role {} added to group {}.", roleName, groupName);
            return true;
        } catch (Exception exception) {
            LOGGER.error("Failed add role to group", exception);
            return false;
        }
    }


    public boolean revokeRoleFromGroup(String roleName, String groupName) {
        try (final Keycloak keycloak = getKeycloak()) {
            final RolesResource rolesResource = keycloak.realm(REALM_MXE).roles();
            final GroupsResource groupsResource = keycloak.realm(REALM_MXE).groups();

            List<GroupRepresentation> fetched = groupsResource.groups(groupName, 0, 1);
            String id = fetched.get(0).getId();

            GroupResource groupResource = groupsResource.group(id);
            RoleRepresentation role = rolesResource.get(roleName).toRepresentation();
            LOGGER.info("Revoke role {} from group {}", roleName, groupName);
            groupResource.roles().realmLevel().remove(Arrays.asList(role));
            LOGGER.info("Role {} removed from group {}.", roleName, groupName);
            return true;
        } catch (Exception exception) {
            LOGGER.error("Failed revoke role from group", exception);
            return false;
        }
    }


    public boolean addGroupToUser(String groupName, String userName) {
        try (final Keycloak keycloak = getKeycloak()) {
            final GroupsResource groupsResource = keycloak.realm(REALM_MXE).groups();
            final UsersResource usersResource = keycloak.realm(REALM_MXE).users();

            usersResource.search(userName).stream().map(userResource -> usersResource.get(userResource.getId()))
                    .forEach(userResource -> {
                        LOGGER.info("Joining user {} to group {}", userName, groupName);
                        userResource.joinGroup(groupsResource.groups(groupName, 0, 1).get(0).getId());
                        LOGGER.info("Joined successfully.");
                    });
            return true;
        } catch (Exception exception) {
            LOGGER.error("Failed add group to user", exception);
            return false;
        }
    }

    public boolean revokeGroupFromUser(String groupName, String userName) {
        try (final Keycloak keycloak = getKeycloak()) {
            final GroupsResource groupsResource = keycloak.realm(REALM_MXE).groups();
            final UsersResource usersResource = keycloak.realm(REALM_MXE).users();

            usersResource.search(userName).stream().map(userResource -> usersResource.get(userResource.getId()))
                    .forEach(userResource -> {
                        LOGGER.info("Leaving group {} by user {}", groupName, userName);
                        userResource.leaveGroup(groupsResource.groups(groupName, 0, 1).get(0).getId());
                        LOGGER.info("Left successfully.");
                    });
            return true;
        } catch (Exception exception) {
            LOGGER.error("Failed revoke group from user", exception);
            return false;
        }
    }
}
