package com.ericsson.mxe.jcat.driver.cli;

import static com.ericsson.mxe.jcat.util.CommonUtil.getNameFromPath;
import java.time.Duration;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ericsson.mxe.jcat.command.CustomCommand;
import com.ericsson.mxe.jcat.command.PipCommand;
import com.ericsson.mxe.jcat.command.PythonCommand;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

public class BaselineDriver {

    private static final String SUM_IMAGE_LOAD_TIME = "Sum image load time:";
    private static final String SUM_RANDOM_GEN_TIME = "Sum random generation time:";
    private static final String SUM_PREDICTION_TIME = "Sum prediction time:";
    private static final String NUMBER_OF_PREDICTIONS = "Number of prediction(s):";
    // private static final String PREDICTION_SCRIPT_FORMAT = "%1$s/%2$s -m %1$s/%3$s -i %1$s/%4$s";
    private static final String INCEPTION3_PREDICTION_SCRIPT_FORMAT =
            "${workdir}/${modelbase}/${script} -m ${workdir}/${modelbase}/model -i ${workdir}/${imagedir}";
    private static final String TELCO_PREDICTION_SCRIPT_FORMAT = "${workdir}/${script} -c ${count} -r";
    private static final String EMPTY_PREDICTION_SCRIPT_FORMAT = "${workdir}/${script} -c ${count}";
    private static final Logger LOGGER = LoggerFactory.getLogger(BaselineDriver.class);
    private MxeCliDriver cliDriver;
    private Params params;

    public BaselineDriver(MxeCliDriver cliDriver, Params parameters) {
        this.cliDriver = cliDriver;
        this.params = parameters;
    }

    public Params getParams() {
        return params;
    }


    public void copyDependencies() {
        cliDriver.execute(new CustomCommand("mkdir -p " + params.workdir) {});
        if (Strings.isNotEmpty(params.modelBaseDir)) {
            copyWithLog("modeldir", params.modelBaseDir, params.workdir);
        }
        if (Strings.isNotEmpty(params.imagedir)) {
            copyWithLog("imagedir", params.imagedir, params.workdir);
        }
    }

    private void copyWithLog(String what, String from, String to) {
        LOGGER.info("Copy {} {} to {}", what, from, to);
        cliDriver.copyTo(from, to);
    }

    /** Installs the requirements using pip */
    public CommandResult installRequirements(String pip) {
        return cliDriver
                .execute(
                        PipCommand.pipCommand(pip)
                                .installRequirement(params.workdir + "/" + getNameFromPath(params.modelBaseDir) + "/"
                                        + getNameFromPath(params.requirementsPath))
                                .withTimeout(Duration.ofMinutes(10)));
    }

    /**
     * Executes the prediction script using python and parameters formatted with
     * {@value #INCEPTION3_PREDICTION_SCRIPT_FORMAT}
     */
    public Pair<CommandResult, BaselineResult> executeInception3BaselineTest(String python) {
        CommandResult cmdResult = cliDriver.execute(PythonCommand.pythonCommand(python)
                .runScript(StringSubstitutor.replace(INCEPTION3_PREDICTION_SCRIPT_FORMAT, ImmutableMap.of(//
                        "workdir", params.workdir, //
                        "modelbase", getNameFromPath(params.modelBaseDir), //
                        "script", getNameFromPath(params.testScriptName), //
                        "imagedir", getNameFromPath(params.imagedir))))
                .withTimeout(Duration.ofMinutes(10)));
        return Pair.of(cmdResult, parseResult(cmdResult));
    }

    /**
     * Executes the prediction script using python and parameters formatted with
     * {@value #TELCO_PREDICTION_SCRIPT_FORMAT}
     */
    public Pair<CommandResult, BaselineResult> executeTelcoBaselineTest(String python) {
        CommandResult cmdResult = cliDriver.execute(PythonCommand.pythonCommand(python)
                .runScript(StringSubstitutor.replace(TELCO_PREDICTION_SCRIPT_FORMAT, ImmutableMap.of(//
                        "workdir", params.workdir, //
                        "script", getNameFromPath(params.modelBaseDir) + "/" + getNameFromPath(params.testScriptName), //
                        "count", params.count)))
                .withTimeout(Duration.ofMinutes(10)));
        return Pair.of(cmdResult, parseResult(cmdResult));
    }

    /**
     * Executes the prediction script using python and parameters formatted with
     * {@value #EMPTY_PREDICTION_SCRIPT_FORMAT}
     */
    public Pair<CommandResult, BaselineResult> executeEmptyBaselineTest(String python) {
        CommandResult cmdResult = cliDriver.execute(PythonCommand.pythonCommand(python)
                .runScript(StringSubstitutor.replace(EMPTY_PREDICTION_SCRIPT_FORMAT, ImmutableMap.of(//
                        "workdir", params.workdir, //
                        "script", getNameFromPath(params.modelBaseDir) + "/" + getNameFromPath(params.testScriptName), //
                        "count", params.count)))
                .withTimeout(Duration.ofMinutes(10)));
        return Pair.of(cmdResult, parseResult(cmdResult));
    }

    static BaselineResult parseResult(CommandResult commandResult) {
        long failed = 0l;
        int nrOfPredictions = -1;
        double sumPredictionTime = -1;
        double sumNonPredictionTime = 0;

        String commandOutput = commandResult.getCommandOutput();
        for (String line : Splitter.on('\n').split(commandOutput)) {
            if (line.startsWith("Failed to predict")) {
                failed++;
            } else if (line.startsWith(NUMBER_OF_PREDICTIONS)) {
                nrOfPredictions = Integer.parseInt(line.replace(NUMBER_OF_PREDICTIONS, "").trim());
            } else if (line.startsWith(SUM_PREDICTION_TIME)) {
                sumPredictionTime = Double.parseDouble(line.replace(SUM_PREDICTION_TIME, "").trim());
            } else if (line.startsWith(SUM_IMAGE_LOAD_TIME)) {
                sumNonPredictionTime = Double.parseDouble(line.replace(SUM_IMAGE_LOAD_TIME, "").trim());
            } else if (line.startsWith(SUM_RANDOM_GEN_TIME)) {
                sumNonPredictionTime = Double.parseDouble(line.replace(SUM_RANDOM_GEN_TIME, "").trim());
            }
        }
        return new BaselineResult(failed, nrOfPredictions, sumPredictionTime, sumNonPredictionTime);

    }

    public static class BaselineResult {

        public final long failed;
        public final int nrOfPredictions;
        public final double sumPredictionTime;
        public final double sumNonPredictionTime;

        public BaselineResult(long failed, int nrOfPredictions, double sumPredictionTime, double sumNonPredictionTime) {
            this.failed = failed;
            this.nrOfPredictions = nrOfPredictions;
            this.sumPredictionTime = sumPredictionTime;
            this.sumNonPredictionTime = sumNonPredictionTime;
        }
    }

    /** Contains dependency information for running the model */
    public static class Params {

        public String modelBaseDir;
        public String workdir;
        public String imagedir;
        public String requirementsPath;
        public String testScriptName;
        public int count;

        public Params modelBaseDir(String modelBaseDir) {
            this.modelBaseDir = modelBaseDir;
            return this;
        }

        public Params workdir(String workdir) {
            this.workdir = workdir;
            return this;
        }

        public Params imagedir(String imagedir) {
            this.imagedir = imagedir;
            return this;
        }

        public Params requirementsPath(String requirementsPath) {
            this.requirementsPath = requirementsPath;
            return this;
        }

        public Params testScriptName(String testScriptName) {
            this.testScriptName = testScriptName;
            return this;
        }

        public Params count(int count) {
            this.count = count;
            return this;
        }

        public static Params params() {
            return new Params();
        }


    }
}
