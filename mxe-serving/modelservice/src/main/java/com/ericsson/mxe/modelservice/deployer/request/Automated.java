package com.ericsson.mxe.modelservice.deployer.request;

public class Automated {
    private Boolean prune;
    private Boolean selfHeal;
    private Boolean allowEmpty;

    public Boolean getPrune() {
        return prune;
    }

    public void setPrune(Boolean prune) {
        this.prune = prune;
    }

    public Boolean getSelfHeal() {
        return selfHeal;
    }

    public void setSelfHeal(Boolean selfHeal) {
        this.selfHeal = selfHeal;
    }

    public Boolean getAllowEmpty() {
        return allowEmpty;
    }

    public void setAllowEmpty(Boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }
}
