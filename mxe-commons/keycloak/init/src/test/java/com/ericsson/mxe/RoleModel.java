package com.ericsson.mxe;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoleModel {

    private String name;
    private Map<String, List<String>> attributes;

    public RoleModel(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes =
                attributes.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> List.of(e.getValue())));
    }

    public String getName() {
        return name;
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public String getFirstAttribute(String key) {
        return attributes.get(key).stream().findFirst().get();
    }

    @Override
    public String toString() {
        return name + ": " + attributes.toString();
    }
}
