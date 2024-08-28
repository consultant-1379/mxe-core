package com.ericsson.mxe.jcat.command;

public abstract class MxeTrainingCommand extends MxeCommand {

    private static final String PARAMETER_TEMPLATE_ONBOARD = " onboard --source %s";
    private static final String PARAMETER_TEMPLATE_LIST_TRAINING_PACKAGES = " list packages";
    private static final String PARAMETER_TEMPLATE_LIST_TRAINING_JOBS = " list jobs";
    private static final String PARAMETER_TEMPLATE_START_TRAINING = " start --packageId %s --packageVersion %s";
    private static final String PARAMETER_TEMPLATE_DELETE_TRAINING_PACKAGE = " delete package --id %s --version %s";
    private static final String PARAMETER_TEMPLATE_DELETE_TRAINING_JOB_BY_PACKAGE =
            " delete job --packageId %s --packageVersion %s";
    private static final String PARAMETER_TEMPLATE_DELETE_TRAINING_JOB_BY_ID = " delete job --id %s";
    private static final String PARAMETER_TEMPLATE_VERSION = " version";
    private static final String PARAMETER_TEMPLATE_DOWNLOAD_RESULTS = " download-results --jobId %s";
    private static final String PARAMETER_TEMPLATE_DOWNLOAD_RESULTS_WITH_DIR =
            " download-results --jobId %s --toDir %s";

    public MxeTrainingCommand(String command) {
        super(command);
    }

    public MxeTrainingCommand onboard(final String source) {
        setParameter(String.format(PARAMETER_TEMPLATE_ONBOARD, source));
        return this;
    }

    public MxeTrainingCommand listTrainingPackages() {
        setParameter(PARAMETER_TEMPLATE_LIST_TRAINING_PACKAGES);
        return this;
    }

    public MxeTrainingCommand listTrainingJobs() {
        setParameter(PARAMETER_TEMPLATE_LIST_TRAINING_JOBS);
        return this;
    }

    public MxeTrainingCommand start(final String packageId, final String packageVersion) {
        setParameter(String.format(PARAMETER_TEMPLATE_START_TRAINING, packageId, packageVersion));
        return this;
    }

    public MxeTrainingCommand deleteTrainingPackage(final String packageId, final String packageVersion) {
        setParameter(String.format(PARAMETER_TEMPLATE_DELETE_TRAINING_PACKAGE, packageId, packageVersion));
        return this;
    }

    public MxeTrainingCommand deleteTrainingJobByPackage(final String packageId, final String packageVersion) {
        setParameter(String.format(PARAMETER_TEMPLATE_DELETE_TRAINING_JOB_BY_PACKAGE, packageId, packageVersion));
        return this;
    }

    public MxeTrainingCommand deleteTrainingJobById(final String id) {
        setParameter(String.format(PARAMETER_TEMPLATE_DELETE_TRAINING_JOB_BY_ID, id));
        return this;
    }

    public MxeTrainingCommand version() {
        setParameter(PARAMETER_TEMPLATE_VERSION);
        return this;
    }

    public MxeTrainingCommand list() {
        setParameter(PARAMETER_TEMPLATE_LIST);
        return this;
    }

    public MxeTrainingCommand downloadTrainingResult(final String jobId) {
        setParameter(String.format(PARAMETER_TEMPLATE_DOWNLOAD_RESULTS, jobId));
        return this;
    }

    public MxeTrainingCommand downloadTrainingResult(final String jobId, final String toDir) {
        setParameter(String.format(PARAMETER_TEMPLATE_DOWNLOAD_RESULTS_WITH_DIR, jobId, toDir));
        return this;
    }
}
