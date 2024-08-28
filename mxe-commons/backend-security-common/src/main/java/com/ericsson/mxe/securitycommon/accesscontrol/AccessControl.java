package com.ericsson.mxe.securitycommon.accesscontrol;

import com.ericsson.mxe.backendservicescommon.exception.MxeForbiddenException;
import com.google.common.annotations.VisibleForTesting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static java.util.stream.Collectors.toSet;

/**
 * Utility to check if action is executable against the access control information in the access token. This class
 * assumes that the access control information stored in the access token does not contain contradictory rights.
 *
 * Anonymous access is assumed to be only possible for internal service communication!
 */
@Service
public class AccessControl {

    private static final Logger logger = LogManager.getLogger(AccessControl.class);

    private static final String GLOBAL_ROLES_KEY = "roles";
    private static final String GLOBAL_ANONYMOUS = "anonymous";
    private static final String GLOBAL_ADMINISTRATOR = "administrator";

    @Autowired
    @Qualifier("noExpiryDecoder")
    private JwtDecoder decoder;

    public void checkAccess(String domain, TargetType target, Action requestedAction, String errorMessage) {
        checkAccess(getAuthentication(), domain, target, requestedAction, errorMessage);
    }

    public void checkAccess(Authentication auth, String domain, TargetType target, Action requestedAction,
            String errorMessage) {
        if (!isAccessAllowed(auth, domain, target, requestedAction)) {
            logger.error("[SECURITY EVENT] - Access denied: {}, user: {}, domain: {}, target: {}, action: {}",
                    errorMessage, getUsername(), domain, target.getValue(), requestedAction.getValue());
            throw new MxeForbiddenException(errorMessage);
        }
    }

    public boolean isAccessAllowed(String domain, TargetType target, Action requestedAction) {
        return isAccessAllowed(getAuthentication(), domain, target, requestedAction);
    }

    private boolean isAccessAllowed(Authentication auth, String domain, TargetType target, Action requestedAction) {
        return isAccessAllowed(getAllowedActions(auth, domain, target), requestedAction);
    }

    public Set<Action> getAllowedActions(String domain, TargetType target) {
        return getAllowedActions(getAuthentication(), domain, target);
    }

    public MxeJwtAuthenticationToken getAuthenticationFromTokenValue(String tokenValue) {
        return new MxeJwtAuthenticationToken(decoder.decode(tokenValue));
    }

    public boolean matchesUsername(String username) {
        if (username == null) {
            return false;
        }
        return username.equals(getUsername());
    }

    public String getUsername() {
        Authentication authentication = getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            return token.getToken().getClaimAsString("preferred_username");
        }
        return "";
    }

    public String getTokenValue() {
        Authentication authentication = getAuthentication();
        if (authentication instanceof MxeJwtAuthenticationToken token) {
            return token.getToken().getTokenValue();
        }
        return null;
    }

    public void checkMxeAdministrator() {
        List<String> global_roles = getGlobalRoles();
        if (global_roles.contains(GLOBAL_ADMINISTRATOR) || global_roles.contains(GLOBAL_ANONYMOUS)) {
            return;
        }
        logger.error("[SECURITY EVENT] - Access denied: Administrator role is required, user: {}", getUsername());
        throw new MxeForbiddenException("Administrator role is required to perform this operation.");
    }

    private List<String> getGlobalRoles() {
        Authentication auth = getAuthentication();
        if (auth instanceof AnonymousAuthenticationToken) {
            logger.debug("No global roles (Anonymous access).");
            return List.of(GLOBAL_ANONYMOUS);
        }

        if (auth instanceof MxeJwtAuthenticationToken token) {
            MxeAccessControlClaimGlobal claim = token.getMxeAccessControlClaimGlobal();
            if (claim == null || claim.global == null) {
                logger.debug("No global roles granted. There is no mxe global claim in access token! ");
                return Collections.emptyList();
            }

            List<String> roles = claim.global.get(GLOBAL_ROLES_KEY);
            if (roles == null) {
                logger.debug("No global roles granted. There is no global roles list in mxe claim! ");
                return Collections.emptyList();
            }
            return roles;
        }
        return Collections.emptyList();

    }

    private Set<Action> getAllowedActions(Authentication auth, String domain, TargetType target) {
        if (auth instanceof AnonymousAuthenticationToken) {
            logger.debug("All action is granted (Anonymous access).");
            return Set.of(Action.ALL);
        }
        if (auth instanceof MxeJwtAuthenticationToken token) {
            MxeAccessControlClaim claim = token.getMxeAccessControlClaim();
            if (claim == null) {
                logger.debug("No action is permitted. There is no mxe claim in access token! ");
                return Collections.emptySet();
            }

            Set<Action> actions = getAllowedActions(claim, domain, TargetType.ALL);
            actions.addAll(getAllowedActions(claim, domain, target));
            if (actions.contains(Action.ALL)) {
                return Set.of(Action.ALL);
            }
            return actions;
        }
        return Collections.emptySet();
    }

    private boolean isAccessAllowed(Set<Action> allowedActions, Action requestedAction) {
        return allowedActions.stream().anyMatch(allowedAction -> matchesAllowedAction(allowedAction, requestedAction));
    }

    private Set<Action> getAllowedActions(MxeAccessControlClaim claim, String domain, TargetType target) {
        AccessControlMap acMap = claim.getByTarget(target);
        if (acMap == null) {
            return new HashSet<>();
        }
        return acMap.getDomainAndParents(domain).stream().map(acMap::getAllowedActionByDomain).collect(toSet());
    }

    @VisibleForTesting
    protected Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean matchesAllowedAction(Action allowedAction, Action requestedAction) {
        return Action.ALL.equals(allowedAction) || allowedAction.equals(requestedAction);
    }
}
