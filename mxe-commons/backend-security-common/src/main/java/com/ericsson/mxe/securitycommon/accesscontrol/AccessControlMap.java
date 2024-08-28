package com.ericsson.mxe.securitycommon.accesscontrol;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.*;
import java.util.stream.Collectors;

public class AccessControlMap {
    private SortedMap<Domain, Action> allowedActionsByDomain;

    @JsonCreator
    public AccessControlMap(Map<Domain, Action> allowedActionsByDomain) {
        setAllowedActionsByDomain(allowedActionsByDomain);
    }

    public List<Domain> getDomainAndParents(String subDomainName) {
        Domain subDomain = new Domain(subDomainName);
        return allowedActionsByDomain.keySet().stream().takeWhile(domain -> domain.length() <= subDomain.length())
                .filter(domainPrefix -> domainPrefix.isEqualToOrParentOf(subDomain)).collect(Collectors.toList());
    }

    public Action getAllowedActionByDomain(Domain domain) {
        return allowedActionsByDomain.get(domain);
    }

    public void setAllowedActionsByDomain(Map<Domain, Action> allowedActionsByDomain) {
        this.allowedActionsByDomain = new TreeMap<>(allowedActionsByDomain);
    }

    public Map<Domain, Action> getAllowedActionsByDomain() {
        return allowedActionsByDomain;
    }

    @Override
    public String toString() {
        return allowedActionsByDomain.toString();
    }
}
