package com.ericsson.mxe.securitycommon.accesscontrol;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.io.IOException;

class MxeJwtAuthenticationToken extends JwtAuthenticationToken {

    private static final String CLAIM_NAME = "mxe-access-control";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(MxeJwtAuthenticationToken.class);

    static {
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    }

    private MxeAccessControlClaim mxeAccessControlClaim;
    private MxeAccessControlClaimGlobal mxeAccessControlClaimGlobal;

    MxeJwtAuthenticationToken(Jwt jwt) {
        super(jwt, null);
        String claim = jwt.getClaimAsString(CLAIM_NAME);
        logger.debug("Claim from token: " + claim);
        if (claim != null) {
            try {
                mxeAccessControlClaim = mapper.readValue(claim, MxeAccessControlClaim.class);
                mxeAccessControlClaimGlobal = mapper.readValue(claim, MxeAccessControlClaimGlobal.class);
            } catch (IOException ex) {
                logger.error("Invalid \"" + CLAIM_NAME + "\" claim .", ex);
            }
        }
    }

    MxeAccessControlClaim getMxeAccessControlClaim() {
        return mxeAccessControlClaim;
    }

    MxeAccessControlClaimGlobal getMxeAccessControlClaimGlobal() {
        return mxeAccessControlClaimGlobal;
    }
}
