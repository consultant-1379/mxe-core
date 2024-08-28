package com.ericsson.mxe.modelservice.deployer.request;

import java.util.List;

public class DeploymentOptions {
    private String appName;
    private PackageSource packageSource;
    private PackageDestination packageDestination;
    private List<String> labels;
    private SyncPolicy syncPolicy;
    private List<String> syncOptions;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public PackageSource getPackageSource() {
        return packageSource;
    }

    public void setPackageSource(PackageSource packageSource) {
        this.packageSource = packageSource;
    }

    public PackageDestination getPackageDestination() {
        return packageDestination;
    }

    public void setPackageDestination(PackageDestination packageDestination) {
        this.packageDestination = packageDestination;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public SyncPolicy getSyncPolicy() {
        return syncPolicy;
    }

    public void setSyncPolicy(SyncPolicy syncPolicy) {
        this.syncPolicy = syncPolicy;
    }

    public List<String> getSyncOptions() {
        return syncOptions;
    }

    public void setSyncOptions(List<String> syncOptions) {
        this.syncOptions = syncOptions;
    }
}
