package com.ericsson.mxe.jcat.config;

import com.ericsson.commonlibrary.cf.spi.ConfigurationData;

public interface User extends ConfigurationData {

    String getUserName();

    String getPassword();

    String getPrompt();

    String getSshPublicKeyFile();

    @Override
    default Class<? extends ConfigurationData> getType() {
        return User.class;
    }
}
