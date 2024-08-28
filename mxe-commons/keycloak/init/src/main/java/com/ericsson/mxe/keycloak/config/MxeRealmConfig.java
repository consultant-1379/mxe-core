package com.ericsson.mxe.keycloak.config;

import com.ericsson.mxe.keycloak.config.properties.MxeRealmProperties;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.*;

@Configuration
public class MxeRealmConfig {
    public static final String MXE = "mxe";
    public static final String MXE_ERIC_BASE_THEME = "ericsson-base-modified-theme";
    public static final String MXE_CLAIM_NAME = "claim.name";
    public static final String MXE_AUDIENCE = "mxe-audience";
    public static final String MXE_PREV_LOGIN = "mxe-prev-login";
    public static final String MXE_USER_ROLES = "mxe-user-roles";
    public static final String MXE_CLIENT = "mxe-client";
    public static final String MXE_SCOPE = "mxe-scope";
    public static final String MXE_REST_CLIENT = "mxe-rest-client";
    public static final String OPENID_CONNECT = "openid-connect";

    private final MxeRealmProperties realmProperties;

    public MxeRealmConfig(MxeRealmProperties realmProperties) {
        this.realmProperties = realmProperties;
    }

    @Bean
    public RealmRepresentation mxeRealm() {
        final RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(realmProperties.getName());
        realmRepresentation.setDisplayName(realmProperties.getName());
        realmRepresentation.setEnabled(true);
        realmRepresentation.setLoginTheme(MXE_ERIC_BASE_THEME);
        realmRepresentation.setSslRequired("none");
        realmRepresentation.setEventsListeners(Arrays.asList("jboss-logging", "keycloak-event-listener"));
        realmRepresentation.setEventsEnabled(true);
        realmRepresentation.setEnabledEventTypes(Arrays.asList("LOGIN", "LOGOUT"));

        // Security implementations
        realmRepresentation.setPasswordPolicy("length(8) and maxLength(64) and forceExpiredPasswordChange(90)");
        realmRepresentation.setBruteForceProtected(true);
        realmRepresentation.setPermanentLockout(false);
        realmRepresentation.setFailureFactor(5);
        realmRepresentation.setWaitIncrementSeconds(60);
        realmRepresentation.setQuickLoginCheckMilliSeconds(1000L);
        realmRepresentation.setMinimumQuickLoginWaitSeconds(60);
        realmRepresentation.setMaxFailureWaitSeconds(900);
        realmRepresentation.setMaxDeltaTimeSeconds(43200);
        return realmRepresentation;

    }

    // Gatekeeper client
    @Bean
    public ClientRepresentation mxeClient() {
        final ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setClientId(MXE_CLIENT);
        clientRepresentation.setName(MXE_CLIENT);
        clientRepresentation.setProtocol(OPENID_CONNECT);
        clientRepresentation.setPublicClient(false);
        clientRepresentation.setBearerOnly(false);
        clientRepresentation.setStandardFlowEnabled(true);
        clientRepresentation.setImplicitFlowEnabled(false);
        clientRepresentation.setDirectAccessGrantsEnabled(true);
        clientRepresentation.setRedirectUris(Collections.singletonList(realmProperties.getRedirectUrl()));
        clientRepresentation.setWebOrigins(null);

        return clientRepresentation;
    }

    @Bean
    public ClientRepresentation mxeRestClient() {
        final ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setClientId(MXE_REST_CLIENT);
        clientRepresentation.setName(MXE_REST_CLIENT);
        clientRepresentation.setProtocol(OPENID_CONNECT);
        clientRepresentation.setPublicClient(true);
        clientRepresentation.setBearerOnly(false);
        clientRepresentation.setStandardFlowEnabled(false);
        clientRepresentation.setImplicitFlowEnabled(false);
        clientRepresentation.setDirectAccessGrantsEnabled(true);

        return clientRepresentation;
    }

    @Bean
    public ClientScopeRepresentation mxeClientScope() {
        final ClientScopeRepresentation clientScopeRepresentation = new ClientScopeRepresentation();
        clientScopeRepresentation.setName(MXE_SCOPE);
        clientScopeRepresentation.setProtocol(OPENID_CONNECT);

        return clientScopeRepresentation;
    }

    @Bean
    public ProtocolMapperRepresentation mxeAudienceProtocolMapper() {
        final ProtocolMapperRepresentation protocolMapperRepresentation = new ProtocolMapperRepresentation();
        final Map<String, String> protocolMapperConfig = new HashMap<>();
        protocolMapperConfig.put("id.token.claim", Boolean.FALSE.toString());
        protocolMapperConfig.put("access.token.claim", Boolean.TRUE.toString());
        protocolMapperConfig.put("included.client.audience", MXE_CLIENT);
        protocolMapperRepresentation.setName(MXE_AUDIENCE);
        protocolMapperRepresentation.setProtocol(OPENID_CONNECT);
        protocolMapperRepresentation.setProtocolMapper("oidc-audience-mapper");
        protocolMapperRepresentation.setConfig(protocolMapperConfig);

        return protocolMapperRepresentation;
    }

    @Bean
    public ProtocolMapperRepresentation mxeUserRolesProtocolMapper() {
        final ProtocolMapperRepresentation protocolMapperRepresentation = new ProtocolMapperRepresentation();
        final Map<String, String> protocolMapperConfig = new HashMap<>();
        protocolMapperConfig.put("id.token.claim", Boolean.TRUE.toString());
        protocolMapperConfig.put("access.token.claim", Boolean.TRUE.toString());
        protocolMapperConfig.put("userinfo.token.claim", Boolean.TRUE.toString());
        protocolMapperConfig.put("multivalued", Boolean.TRUE.toString());
        protocolMapperConfig.put(MXE_CLAIM_NAME, "roles");
        protocolMapperConfig.put("jsonType.label", "String");
        protocolMapperRepresentation.setName(MXE_USER_ROLES);
        protocolMapperRepresentation.setProtocol(OPENID_CONNECT);
        protocolMapperRepresentation.setProtocolMapper("oidc-usermodel-realm-role-mapper");
        protocolMapperRepresentation.setConfig(protocolMapperConfig);

        return protocolMapperRepresentation;
    }

    @Bean
    public ProtocolMapperRepresentation mxePrevLoginProtocolMapper() {
        final ProtocolMapperRepresentation protocolMapperRepresentation = new ProtocolMapperRepresentation();
        final Map<String, String> protocolMapperConfig = new HashMap<>();
        protocolMapperConfig.put("id.token.claim", Boolean.TRUE.toString());
        protocolMapperConfig.put("access.token.claim", Boolean.TRUE.toString());
        protocolMapperConfig.put("userinfo.token.claim", Boolean.TRUE.toString());
        protocolMapperConfig.put("user.attribute", "prev_login");
        protocolMapperConfig.put("jsonType.label", "long");
        protocolMapperConfig.put(MXE_CLAIM_NAME, "prev_auth_time");
        protocolMapperRepresentation.setName(MXE_PREV_LOGIN);
        protocolMapperRepresentation.setProtocol(OPENID_CONNECT);
        protocolMapperRepresentation.setProtocolMapper("oidc-usermodel-attribute-mapper");
        protocolMapperRepresentation.setConfig(protocolMapperConfig);

        return protocolMapperRepresentation;
    }

    @Bean
    public UserRepresentation mxeUser() {
        final UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setUsername(realmProperties.getUsername());
        userRepresentation.setFirstName("");
        userRepresentation.setLastName("");
        userRepresentation.setEmail("");

        return userRepresentation;
    }

    @Bean
    public CredentialRepresentation passwordCredential() {
        final CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(realmProperties.isTemporalUser());
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(realmProperties.getPassword());

        return credentialRepresentation;
    }

    @Bean
    public GroupRepresentation defaultGroup(@Autowired RoleRepresentation defaultRole) {
        GroupRepresentation group = new GroupRepresentation();
        group.setRealmRoles(List.of(defaultRole.getName()));
        group.setName("mxe_default_group");
        return group;
    }

    @Bean
    public RoleRepresentation defaultRole() {
        RoleRepresentation role = new RoleRepresentation();
        role.setName("mxe_default_role");
        role.setDescription("Allows access to all actions to all targets.");
        role.setAttributes(Map.of("*", List.of("all")));
        return role;
    }

    @Bean
    public RoleRepresentation modelServingRole() {
        RoleRepresentation role = new RoleRepresentation();
        role.setName("mxe_model_serving_role");
        role.setDescription("RBAC: Allows access to all models/model-service endpoints.");
        role.setAttributes(Collections.emptyMap());
        return role;
    }

    @Bean
    public RoleRepresentation adminRole() {
        RoleRepresentation adminRole = new RoleRepresentation();
        adminRole.setName("mxe_administrator_role");
        adminRole.setDescription("Allows access to MXE administrative operations.");
        adminRole.setAttributes(Collections.emptyMap());
        return adminRole;
    }
}
