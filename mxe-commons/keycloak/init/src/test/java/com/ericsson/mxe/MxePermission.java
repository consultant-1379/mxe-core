package com.ericsson.mxe;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Map;

public class MxePermission implements Comparable {

    private String domain;
    private String accessRight;

    public MxePermission(Map.Entry<String, String> permission) {
        this.domain = permission.getKey();
        this.accessRight = permission.getValue();
    }

    @JsonCreator
    public MxePermission(Map<String, String> attributes) {
        Map.Entry<String, String> entry =
                attributes.entrySet().stream().filter(e -> !e.getKey().equals("type")).findAny().get();
        domain = entry.getKey();
        accessRight = entry.getValue();
    }

    public String getDomain() {
        return domain;
    }

    public String getAccessRight() {
        return accessRight;
    }

    @Override
    public String toString() {
        return domain + "=" + accessRight;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MxePermission)) {
            return false;
        }
        MxePermission other = (MxePermission) obj;
        if (!domain.equals(other.domain)) {
            return false;
        }
        if (!accessRight.equals(other.accessRight)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return domain.hashCode() + accessRight.hashCode();
    }

    @Override
    public int compareTo(Object o) {
        MxePermission other = (MxePermission) o;
        return this.domain.compareTo(other.domain);
    }
}
