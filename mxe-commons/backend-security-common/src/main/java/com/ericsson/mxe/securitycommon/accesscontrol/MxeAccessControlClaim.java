package com.ericsson.mxe.securitycommon.accesscontrol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import java.util.Set;

/**
 * Claim in access token by name mxe-access-control
 */
public class MxeAccessControlClaim {

    private Map<String, AccessControlMap> accessControlMaps;

    @JsonCreator
    @JsonIgnoreProperties("global")
    public MxeAccessControlClaim(Map<String, AccessControlMap> accessControlMaps) {
        this.accessControlMaps = accessControlMaps;
    }

    public Set<String> getTargets() {
        return accessControlMaps.keySet();
    }

    public AccessControlMap getByTarget(TargetType targetType) {
        return accessControlMaps.get(targetType.getValue());
    }

    public Map<String, AccessControlMap> getAccessControlMaps() {
        return accessControlMaps;
    }

    public void setAccessControlMaps(Map<String, AccessControlMap> accessControlMaps) {
        this.accessControlMaps = accessControlMaps;
    }

    @Override
    public String toString() {
        return accessControlMaps.toString();
    }
}
