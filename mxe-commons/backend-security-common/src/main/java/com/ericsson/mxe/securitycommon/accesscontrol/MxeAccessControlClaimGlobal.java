package com.ericsson.mxe.securitycommon.accesscontrol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MxeAccessControlClaimGlobal {
    public Map<String, List<String>> global;
}
