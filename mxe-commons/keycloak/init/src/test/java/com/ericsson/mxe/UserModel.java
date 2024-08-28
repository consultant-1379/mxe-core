package com.ericsson.mxe;

import java.util.Set;

public class UserModel extends RoleMapperModel {

    private Set<GroupModel> groupModels;

    public UserModel(Set<RoleModel> roleModels, Set<GroupModel> groupModels) {
        super(roleModels);
        this.groupModels = groupModels;
    }

    public Set<GroupModel> getGroups() {
        return groupModels;
    }
}
