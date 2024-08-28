package com.ericsson.mxe;

import java.util.Set;

public class RoleMapperModel {

    private Set<RoleModel> roleModels;

    public RoleMapperModel(Set<RoleModel> roleModels) {
        this.roleModels = roleModels;
    }

    public Set<RoleModel> getRealmRoleMappings() {
        return roleModels;
    }
}
