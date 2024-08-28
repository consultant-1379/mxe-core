package com.ericsson.mxe.jcat.command;

public abstract class MxeModelCommand extends MxeCommand {

    private static final String PARAMETER_TEMPLATE_LIST_ONBOARDED = " list";
    private static final String PARAMETER_TEMPLATE_PACKAGE = " package --name %s --source %s";
    private static final String PARAMETER_TEMPLATE_ONBOARD_WITHOUT_DESCRIPTION =
            " onboard --id %s --version %s --docker %s --title %s";
    private static final String PARAMETER_TEMPLATE_ONBOARD_WITH_DESCRIPTION =
            " onboard --id %s --version %s --docker %s --description %s --title %s";
    private static final String PARAMETER_TEMPLATE_ONBOARD_SOURCE = " onboard --source %s";
    private static final String PARAMETER_TEMPLATE_ONBOARD_ARCHIVE = " onboard --archive %s";
    private static final String PARAMETER_TEMPLATE_DELETE = " delete --id %s --version %s";

    public MxeModelCommand(String command) {
        super(command);
    }

    public MxeModelCommand list() {
        setParameter(PARAMETER_TEMPLATE_LIST);
        return this;
    }

    public MxeModelCommand listOnboarded() {
        setParameter(PARAMETER_TEMPLATE_LIST_ONBOARDED);
        return this;
    }

    public MxeModelCommand pack(final String name, final String source) {
        setParameter(String.format(PARAMETER_TEMPLATE_PACKAGE, name, source));
        return this;
    }

    public MxeModelCommand onboard(final String name, final String version, final String packagename) {
        setParameter(String.format(PARAMETER_TEMPLATE_ONBOARD_WITHOUT_DESCRIPTION, name, version, packagename, name));
        return this;
    }

    public MxeModelCommand onboardSource(final String source) {
        setParameter(String.format(PARAMETER_TEMPLATE_ONBOARD_SOURCE, source));
        return this;
    }

    public MxeModelCommand onboardArchive(final String archive) {
        setParameter(String.format(PARAMETER_TEMPLATE_ONBOARD_ARCHIVE, archive));
        return this;
    }

    public MxeModelCommand onboard(final String name, final String version, final String packagename,
            final String description) {
        setParameter(String.format(PARAMETER_TEMPLATE_ONBOARD_WITH_DESCRIPTION, name, version, packagename, description,
                name));
        return this;
    }

    public MxeModelCommand delete(final String name, final String version) {
        setParameter(String.format(PARAMETER_TEMPLATE_DELETE, name, version));
        return this;
    }
}
