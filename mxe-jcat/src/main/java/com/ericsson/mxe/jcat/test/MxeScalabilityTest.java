package com.ericsson.mxe.jcat.test;

import com.ericsson.mxe.jcat.chart.*;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.model.ModelScalabilityTrafficRecord;
import com.ericsson.mxe.jcat.driver.model.ModelTrafficGenerator;
import com.ericsson.mxe.jcat.driver.model.ModelTrafficScenarioResult;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import com.ericsson.mxe.jcat.driver.util.ModelTrafficStatistics;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.MediaType;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.JcatClass;
import se.ericsson.jcat.fw.annotations.JcatMethod;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import static com.ericsson.mxe.jcat.chart.Axis.Orientation.HORIZONTAL;
import static com.ericsson.mxe.jcat.chart.Axis.Orientation.VERTICAL;
import static com.ericsson.mxe.jcat.chart.Axis.Type.DATE;
import static com.ericsson.mxe.jcat.chart.Axis.Type.VALUE;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.modelDelete;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.modelOnboard;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.*;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.ERROR_RESOURCE_RELEASE;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.STATUS_RUNNING;


/**
 * @JcatDocChapterDescription Chapter covering scalability tests.
 */
@JcatClass(chapterName = "Scalability Tests")
public class MxeScalabilityTest extends MxeKubernetesTestBase {

    private static final Duration MODEL_START_TIMEOUT = Duration.ofMinutes(15);

    /**
     * @JcatTcDescription Performs performance test with a model deployment
     * @JcatTcPreconditions MXE cluster is up and running in a separated characteristics environment
     * @JcatTcInstruction The testcase is about sending data to a running model deployment with fixed rate and changing
     *                    model instance number and measure the following characteristics for each send rate: <br/>
     *                    <b>Model level measurements provided by prometheus</b>
     *                    <ul>
     *                    <li><u>request rate:</u> Average request rate of the model deployment during the
     *                    measurement</li>
     *                    <li><u>latency:</u> Average latency of the model deployment during the measurement</li>
     *                    <li><u>success rate:</u> Average success rate of the model deployment during the
     *                    measurement</li>
     *                    <li><u>400 response op/sec:</u> Average 400 response op/sec of the model deployment during the
     *                    measurement</li>
     *                    <li><u>500 response op/sec:</u> Average 500 response op/sec of the model deployment during the
     *                    measurement</li>
     *                    </ul>
     *                    <b>Cluster level measurements provided by prometheus</b>
     *                    <ul>
     *                    <li><u>memory usage:</u> Memory usage of the cluster</li>
     *                    <li><u>CPU:</u> CPU load of the cluster</li>
     *                    <li><u>Filesystem usage:</u> Filesystem usage load of the cluster</li>
     *                    <li><u>Network usage:</u> Network usage load of the cluster</li>
     *                    </ul>
     *                    <b>Pod level measurements provided by prometheus</b>
     *                    <ul>
     *                    <li><u>memory usage:</u> Memory usage of the pods</li>
     *                    <li><u>CPU:</u> CPU load of the pods</li>
     *                    <li><u>Filesystem usage:</u> Filesystem usage load of the pods</li>
     *                    <li><u>Network usage:</u> Network usage load of the pods</li>
     *                    </ul>
     *                    Since several preparatory steps are needed for execution of this scalability test case, the
     *                    followings are needed:
     *                    <ul>
     *                    <li>always execute with a predefined test suite</li>
     *                    </ul>
     *                    <b>Scalability test parameters</b>
     *                    <ul>
     *                    <li><u>packageName:</u> Model package nam and path to be used during the test</li>
     *                    <li><u>modelName:</u> Name of the model after onboarding</li>
     *                    <li><u>modelVersion:</u>Version of the model</li>
     *                    <li><u>instance:</u>Model deployment instance number</li>
     *                    <li><u>maxInstance:</u>Maximum instance number</li>
     *                    <li><u>sendRate:</u>Send rate we would like to reach during the measurement</li>
     *                    <li><u>measurementLength:</u>Length of a measurement step in seconds</li>
     *                    </ul>
     * @JcatTcAction Onboard model
     * @JcatTcActionResult Model package onboarded and available in MXE
     * @JcatTcAction Create a model service
     * @JcatTcActionResult Model service created
     * @JcatTcAction Read model input json file
     * @JcatTcActionResult Model input lines are collected, which will be used for prediction
     * @JcatTcAction Start sending traffic with the selected send rate
     * @JcatTcActionResult Traffic sending started
     * @JcatTcAction Continuously collect all the previously listed measurements from prometheus and store it in CSV
     *               file
     * @JcatTcActionResult Data got from prometheus and stored in CSV file
     * @JcatTcAction Stop traffic after measurementLength expired
     * @JcatTcActionResult Traffic stopped
     * @JcatTcAction Scale model service using modify, increase instance with one
     * @JcatTcActionResult Model scaled out
     * @JcatTcAction Perform the previous scaling, traffic starting and measurement steps for all of the instance number
     *               till we reached maxInstance with fix send rate
     * @JcatTcActionResult Measurement repeated for all of the instance number till we reached maxInstance
     * @JcatTcAction Stop data collection
     * @JcatTcActionResult Data collection stopped
     * @JcatTcAction Draw diagrams about the collected data. Separate diagram for all measurement type which contains
     *               measured values for each send rate
     * @JcatTcActionResult Diagrams generated
     * @JcatTcAction Delete previously created model service
     * @JcatTcActionResult Model service deleted
     * @JcatTcAction Delete onboarded model
     * @JcatTcActionResult Model deleted
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-MODEL-SCALABILITY", testTitle = "MXE model scalability test")
    @Parameters({"packageName", "modelName", "modelVersion", "instance", "maxInstance", "sendRate",
            "measurementLength"})
    public void testMxeModelScalability(String packageName, String modelName, String modelVersion, int instance,
            int maxInstance, int sendRate, int measurementLength) {
        int numberOfScales = 1;
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            String instanceName = null;
            final List<ModelScalabilityTrafficRecord> measurementList = new ArrayList<>();
            final Duration scale = Duration.ofSeconds(6);
            try {
                modelOnboard(mxeCliDriver, modelName, modelVersion, packageName);
                instanceName = serviceCreateInStep(mxeCliDriver, modelName, modelVersion, instance);
                waitUntilServiceStatusIs(mxeCliDriver, instanceName, STATUS_RUNNING, MODEL_START_TIMEOUT);
                verifyServiceInstanceNumber(mxeCliDriver, instanceName, instance);
                final ModelTrafficGenerator trafficGenerator =
                        new ModelTrafficGenerator(mxeCluster.getEndpoint(), modelName, mxeCluster.getMxeUser());
                final Duration duration = Duration.ofSeconds(measurementLength);
                final String requestData = Base64.getEncoder().encodeToString(IOUtils
                        .toByteArray(getClass().getClassLoader().getResourceAsStream("model/indian_elephant.jpg")));

                for (int instanceNumber = instance; instanceNumber <= maxInstance; instanceNumber++) {
                    ModelTrafficScenarioResult modelTrafficScenarioResult = executeTrafficScenario(trafficGenerator,
                            sendRate, duration, MediaType.APPLICATION_JSON, requestData, instanceNumber);
                    List<ModelScalabilityTrafficRecord> trafficScenarioStatistics = ModelTrafficStatistics
                            .modelStabilityTrafficRecords(modelTrafficScenarioResult, scale, instanceNumber);
                    double averageSuccessRate = trafficScenarioStatistics.stream()
                            .mapToDouble(e -> e.getTrafficRecord().getNumberOfSuccess()).average().getAsDouble();
                    setTestStepBegin(
                            "Average success rate with " + instanceNumber + " instances: " + averageSuccessRate);
                    setTestStepEnd();
                    measurementList.addAll(trafficScenarioStatistics);
                    if (instanceNumber < maxInstance) {
                        scale(mxeCliDriver, instanceName, instanceNumber, instanceNumber + 1);
                        numberOfScales++;
                    }
                }
            } finally {
                cleanUpModel(mxeCliDriver, instanceName, modelName, modelVersion);
                storeTrafficMeasurementInCsv("scalability_request_rates_and_response_times",
                        ModelTrafficStatistics.buildModelScalabilityTrafficCsv(measurementList.stream()));
                generateChart(measurementList);
            }
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    private ModelTrafficScenarioResult executeTrafficScenario(final ModelTrafficGenerator trafficGenerator,
            final int requestPerSec, final Duration duration, final MediaType mediaType, final String requestData,
            final int instanceNumber) {
        final String stepTitleTemplate = "Traffic scenario [requestPerSec: %d][duration: %s][instances: %d]";
        setTestStepBegin(stepTitleTemplate, requestPerSec, duration, instanceNumber);
        final ModelTrafficScenarioResult modelTrafficScenarioResult =
                trafficGenerator.executeTrafficScenario(requestPerSec, duration, requestData);
        setTestStepEnd();
        return modelTrafficScenarioResult;
    }

    private void scale(MxeCliDriver mxeCliDriver, String instanceName, int oldInstanceNumber, int newInstanceNumber) {
        serviceModifyInstances(mxeCliDriver, instanceName, oldInstanceNumber, newInstanceNumber);
        verifyServiceInstanceNumber(mxeCliDriver, instanceName, newInstanceNumber);
    }

    private void cleanUpModel(MxeCliDriver mxeCliDriver, String instanceName, String modelName, String modelVersion) {
        try {
            if (instanceName != null) {
                serviceDelete(mxeCliDriver, instanceName);
            }
            modelDelete(mxeCliDriver, modelName, modelVersion);
        } catch (Exception e) {
            saveFail("Removing model was unsuccessful", e);
        }
    }

    /**
     * @JcatTcDescription Performs performance test with a model deployment in a flow
     * @JcatTcPreconditions MXE cluster is up and running in a separated characteristics environment
     * @JcatTcInstruction The testcase is about sending data to a running model deployment in a flow with fixed rate and
     *                    changing model instance number and measure the following characteristics for each send rate:
     *                    <br/>
     *                    <b>Model level measurements provided by prometheus</b>
     *                    <ul>
     *                    <li><u>request rate:</u> Average request rate of the model deployment during the
     *                    measurement</li>
     *                    <li><u>latency:</u> Average latency of the model deployment during the measurement</li>
     *                    <li><u>success rate:</u> Average success rate of the model deployment during the
     *                    measurement</li>
     *                    <li><u>400 response op/sec:</u> Average 400 response op/sec of the model deployment during the
     *                    measurement</li>
     *                    <li><u>500 response op/sec:</u> Average 500 response op/sec of the model deployment during the
     *                    measurement</li>
     *                    </ul>
     *                    <b>Cluster level measurements provided by prometheus</b>
     *                    <ul>
     *                    <li><u>memory usage:</u> Memory usage of the cluster</li>
     *                    <li><u>CPU:</u> CPU load of the cluster</li>
     *                    <li><u>Filesystem usage:</u> Filesystem usage load of the cluster</li>
     *                    <li><u>Network usage:</u> Network usage load of the cluster</li>
     *                    </ul>
     *                    <b>Pod level measurements provided by prometheus</b>
     *                    <ul>
     *                    <li><u>memory usage:</u> Memory usage of the pods</li>
     *                    <li><u>CPU:</u> CPU load of the pods</li>
     *                    <li><u>Filesystem usage:</u> Filesystem usage load of the pods</li>
     *                    <li><u>Network usage:</u> Network usage load of the pods</li>
     *                    </ul>
     *                    Since several preparatory steps are needed for execution of this scalability test case, the
     *                    followings are needed:
     *                    <ul>
     *                    <li>always execute with a predefined test suite</li>
     *                    </ul>
     *                    <b>Scalability test parameters</b>
     *                    <ul>
     *                    <li><u>packageName:</u> Model package nam and path to be used during the test</li>
     *                    <li><u>modelName:</u> Name of the model after onboarding</li>
     *                    <li><u>modelVersion:</u>Version of the model</li>
     *                    <li><u>instance:</u>Model deployment instance number</li>
     *                    <li><u>maxInstance:</u>Maximum instance number</li>
     *                    <li><u>sendRate:</u>Send rate we would like to reach during the measurement</li>
     *                    <li><u>measurementLength:</u>Length of a measurement step in seconds</li>
     *                    <li><u>flowFile:</u>Name of the flow file</li>
     *                    <li><u>flowName:</u>Name of the flow to onboard</li>
     *                    <li><u>flowDeploymentName:</u>Name of the flow to deploy</li>
     *                    </ul>
     * @JcatTcAction Onboard model
     * @JcatTcActionResult Model package onboarded and available in MXE
     * @JcatTcAction Start a model deployment is running
     * @JcatTcActionResult Model is running
     * @JcatTcAction Onboard a flow which contains a previously started model
     * @JcatTcActionResult flow onboarded
     * @JcatTcAction Deploy a flow which contains a previously started model
     * @JcatTcActionResult flow deployed
     * @JcatTcAction Read model input json file
     * @JcatTcActionResult Model input lines are collected, which will be used for prediction
     * @JcatTcAction Start sending traffic with the selected send rate
     * @JcatTcActionResult Traffic sending started
     * @JcatTcAction Continuously collect all the previously listed measurements from prometheus and store it in CSV
     *               file
     * @JcatTcActionResult Data got from prometheus and stored in CSV file
     * @JcatTcAction Stop traffic after measurementLength expired
     * @JcatTcActionResult Traffic stopped
     * @JcatTcAction Scale model service with modify command, increase instance with one
     * @JcatTcActionResult Model scaled out
     * @JcatTcAction Perform the previous scaling, traffic starting and measurement steps for all of the instance number
     *               till we reached maxInstance with fix send rate
     * @JcatTcActionResult Measurement repeated for all of the instance number till we reached maxInstance
     * @JcatTcAction Stop data collection after measurementLength expired
     * @JcatTcActionResult Data collection stopped
     * @JcatTcAction Draw diagrams about the collected data. Separate diagram for all measurement type which contains
     *               measured values for each send rate
     * @JcatTcActionResult Diagrams generated
     * @JcatTcAction Stop previously started flow deployment
     * @JcatTcActionResult Flow deployment stopped
     * @JcatTcAction Delete onboarded flow
     * @JcatTcActionResult Flow deleted
     * @JcatTcAction Stop previously started model deployment
     * @JcatTcActionResult Model deployment stopped
     * @JcatTcAction Delete onboarded model
     * @JcatTcActionResult Model deleted
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-FLOW-SCALABILITY", testTitle = "MXE flow scalability test")
    @Parameters({"packageName", "modelName", "modelVersion", "instance", "maxInstance", "sendRate", "measurementLength",
            "flowFile", "flowName", "flowDeploymentName"})
    public void testMxeFlowScalability(String packageName, String modelName, String modelVersion, int instance,
            int maxInstance, int sendRate, int measurementLength, String flowFile, String flowName,
            String flowDeploymentName) {
        // Not implemented yet
    }


    public void generateChart(final List<ModelScalabilityTrafficRecord> measurementList) {
        try {
            storeCharts(Arrays.asList(Pair.of(generateRequestRateChart(measurementList), "stability_req_rate"),
                    Pair.of(generateResponseTimeChart(measurementList), "stability_resp_time")));
        } catch (Exception ex) {
            setTestError("Failed to generate scalability chart!", ex);
        }
    }

    private XYChart generateRequestRateChart(final List<ModelScalabilityTrafficRecord> measurementList) {
        Axis timestampAxis = new Axis(DATE, "timestamp");
        Axis reqPerSecAxis = new Axis(VALUE, "req/s", false, true);
        Axis instanceAxis = new Axis(VALUE, "instance", true, true);

        XYChart chart = Charts.xyChart().addxAxis(timestampAxis).addyAxis(reqPerSecAxis).addyAxis(instanceAxis)
                .changeColorProperty("instance")
                .addSeries(Series.line("Success rate").colors(Palettes.green()).yAxis(reqPerSecAxis)
                        .addDataField(DATE, HORIZONTAL, "timestamp").addDataField(VALUE, VERTICAL, "successPerSec")
                        .tooltipText("Success rate {successPerSec} req/s").build())
                .addSeries(Series.line("Failure rate").colors(Palettes.red()).yAxis(reqPerSecAxis)
                        .addDataField(DATE, HORIZONTAL, "timestamp").addDataField(VALUE, VERTICAL, "errorPerSec")
                        .tooltipText("Failure rate: {errorPerSec} req/s").build())
                .addSeries(Series.line().colors(Palettes.grey()).yAxis(instanceAxis)
                        .addDataField(DATE, HORIZONTAL, "timestamp").addDataField(VALUE, VERTICAL, "instance")
                        .tooltipText("Instance: {instance}").build())
                .data(measurementList.stream().map(e -> {
                    Map<String, Object> row = new HashMap<String, Object>();
                    row.put("instance", e.getInstance());
                    row.put("successPerSec", e.getTrafficRecord().getSuccessPerSec());
                    row.put("errorPerSec", e.getTrafficRecord().getErrorPerSec());
                    row.put("timestamp", e.getTrafficRecord().getStartTime().toEpochMilli());
                    return row;
                }).collect(Collectors.toList())).build();
        return chart;
    }

    private XYChart generateResponseTimeChart(final List<ModelScalabilityTrafficRecord> measurementList) {
        Axis timestampAxis = new Axis(DATE, "timestamp");
        Axis reqPerSecAxis = new Axis(VALUE, "Response time (sec)", false, true);
        Axis instanceAxis = new Axis(VALUE, "instance", true, true);

        XYChart chart = Charts.xyChart().addxAxis(timestampAxis).addyAxis(reqPerSecAxis).addyAxis(instanceAxis)
                .changeColorProperty("instance")
                .addSeries(Series.line("Avg response time").colors(Palettes.blue()).yAxis(reqPerSecAxis)
                        .addDataField(DATE, HORIZONTAL, "timestamp").addDataField(VALUE, VERTICAL, "avgRespTimeSec")
                        .tooltipText("Avg: {avgRespTimeSec} sec").build())
                .addSeries(Series.line("Min response time").colors(Palettes.green()).yAxis(reqPerSecAxis)
                        .addDataField(DATE, HORIZONTAL, "timestamp").addDataField(VALUE, VERTICAL, "minRespTimeSec")
                        .tooltipText("Min: {minRespTimeSec} sec").build())
                .addSeries(Series.line("Max response time").colors(Palettes.red()).yAxis(reqPerSecAxis)
                        .addDataField(DATE, HORIZONTAL, "timestamp").addDataField(VALUE, VERTICAL, "maxRespTimeSec")
                        .tooltipText("Max: {maxRespTimeSec} sec").build())
                .addSeries(Series.line().colors(Palettes.grey()).yAxis(instanceAxis)
                        .addDataField(DATE, HORIZONTAL, "timestamp").addDataField(VALUE, VERTICAL, "instance")
                        .tooltipText("Instance: {instance}").build())
                .data(measurementList.stream().map(e -> {
                    Map<String, Object> row = new HashMap<String, Object>();
                    row.put("instance", e.getInstance());
                    row.put("avgRespTimeSec", e.getTrafficRecord().getAverageResponseTimeSec());
                    row.put("minRespTimeSec", e.getTrafficRecord().getMinResponseTimeSec());
                    row.put("maxRespTimeSec", e.getTrafficRecord().getMaxResponseTimeSec());
                    row.put("timestamp", e.getTrafficRecord().getStartTime().toEpochMilli());
                    return row;
                }).collect(Collectors.toList())).build();
        return chart;
    }
}
