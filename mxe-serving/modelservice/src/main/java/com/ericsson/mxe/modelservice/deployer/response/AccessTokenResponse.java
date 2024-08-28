package com.ericsson.mxe.modelservice.deployer.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessTokenResponse {
    public String Token;
    public String RefreshToken;
    public String Err;

}
