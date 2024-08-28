package com.ericsson.mxe.securitycommon.accesscontrol;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Domain implements Comparable<Domain> {

    private String[] labels;

    @JsonCreator
    public Domain(String domainName) {
        this.labels = domainName.split("\\.");
    }

    public int length() {
        return labels.length;
    }

    public boolean isEqualToOrParentOf(Domain domain) {
        if (isWildCard()) {
            return true;
        }
        if (labels.length > domain.labels.length) {
            return false;
        }
        for (int i = 0; i < labels.length; i++) {
            if (!labels[i].equals(domain.labels[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean isWildCard() {
        return labels.length == 1 && "*".equals(labels[0]);
    }

    @Override
    public int compareTo(Domain domain) {
        return Arrays.compare(labels, domain.labels);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(labels);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Domain)) {
            return false;
        }
        return Arrays.equals(labels, ((Domain) obj).labels);
    }

    @Override
    public String toString() {
        return Arrays.stream(labels).collect(Collectors.joining("."));
    }
}
