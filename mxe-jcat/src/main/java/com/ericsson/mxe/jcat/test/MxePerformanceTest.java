package com.ericsson.mxe.jcat.test;

import static com.ericsson.mxe.jcat.chart.Axis.Orientation.HORIZONTAL;
import static com.ericsson.mxe.jcat.chart.Axis.Orientation.VERTICAL;
import static com.ericsson.mxe.jcat.chart.Axis.Type.DATE;
import static com.ericsson.mxe.jcat.chart.Axis.Type.VALUE;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.MODEL_CREATE_TIMEOUT;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.MODEL_ONBOARD_TIMEOUT;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.MODEL_START_TIMEOUT;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.getOnboardedModelList;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.modelDelete;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.modelOnboard;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.modelOnboardSource;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.waitUntilModelStatusIs;
import static com.ericsson.mxe.jcat.test.MxePrometheusTestHelper.collectHardwareInfoFromPrometheus;
import static com.ericsson.mxe.jcat.test.MxePrometheusTestHelper.collectPrometheusData;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.serviceCreateInStep;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.serviceDelete;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.serviceListInStep;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.verifyServiceInstanceNumber;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.waitUntilServiceStatusIs;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.ERROR_RESOURCE_RELEASE;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.STATUS_AVAILABLE;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.STATUS_CREATING;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.STATUS_RUNNING;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.sleepSec;
import static org.hamcrest.core.Is.is;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.CanReadFileFilter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.ericsson.mxe.jcat.chart.Axis;
import com.ericsson.mxe.jcat.chart.Charts;
import com.ericsson.mxe.jcat.chart.Palettes;
import com.ericsson.mxe.jcat.chart.Series;
import com.ericsson.mxe.jcat.chart.XYChart;
import com.ericsson.mxe.jcat.command.Commands;
import com.ericsson.mxe.jcat.command.result.CommandResult;
import com.ericsson.mxe.jcat.config.TestExecutionHost;
import com.ericsson.mxe.jcat.config.User;
import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.model.ModelTrafficGenerator;
import com.ericsson.mxe.jcat.driver.model.ModelTrafficRecord;
import com.ericsson.mxe.jcat.driver.model.ModelTrafficScenarioResult;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import com.ericsson.mxe.jcat.driver.util.ModelTrafficStatistics;
import com.ericsson.mxe.jcat.test.MxePrometheusTestHelper.PrometheusData;
import com.ericsson.mxe.jcat.test.MxePrometheusTestHelper.PrometheusHWInfo;
import com.google.common.collect.Lists;
import se.ericsson.jcat.fw.annotations.JcatClass;
import se.ericsson.jcat.fw.annotations.JcatMethod;

/**
 * @JcatDocChapterDescription Chapter covering performance tests.
 */
@JcatClass(chapterName = "Performance Tests")
public class MxePerformanceTest extends MxeKubernetesTestBase {

    protected PrometheusHWInfo hardwareInfoFromPrometheus;
    protected PrometheusData prometheusData;

    @Test
    @JcatMethod(testTag = "MODEL-FEED", testTitle = "Model Feed Test")
    @Parameters({"modelDeploymentName", "requestPerSec", "overallRequestCount"})
    public void modelFeedTest(@Optional("imginception3") final String modelDeploymentName,
            @Optional("8") final int requestPerSec, @Optional("200") final int overallRequestCount) throws IOException {
        setTestStepBegin("Init");
        setTestInfo("modelDeploymentName is: " + modelDeploymentName);
        hardwareInfoFromPrometheus = collectHardwareInfoFromPrometheus(prometheusDriver);
        collectHardwareInfoFromNodes();
        final String requestData = Base64.getEncoder().encodeToString(
                IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("model/indian_elephant.jpg")));

        // OBSERVATION: Models can deal with n*2 requests/sec where n is the instances of the running model
        // deployments
        final ZonedDateTime start = ZonedDateTime.now();
        executeTrafficScenario(mxeCluster.getEndpoint(), modelDeploymentName, requestPerSec, overallRequestCount,
                requestData, mxeCluster.getMxeUser());
        final ZonedDateTime end = ZonedDateTime.now();
        prometheusData = collectPrometheusData(prometheusDriver, start, end, modelDeploymentName);
    }

    /**
     * @JcatTcDescription Performs performance test with a model deployment
     * @JcatTcPreconditions MXE cluster is up and running in a separated characteristics environment
     * @JcatTcInstruction The testcase is about sending data to a running model deployment with different rate and
     *                    measure the following characteristics for each send rate:<br/>
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
     *                    Since several preparatory steps are needed for execution of this performance test case, the
     *                    followings are needed:
     *                    <ul>
     *                    <li>always execute with a predefined test suite</li>
     *                    </ul>
     *                    <b>Performance test parameters</b>
     *                    <ul>
     *                    <li><u>packageName:</u> Model package nam and path to be used during the test</li>
     *                    <li><u>modelName:</u> Name of the model after onboarding</li>
     *                    <li><u>modelVersion:</u>Version of the model</li>
     *                    <li><u>instanceName:</u>Name of the model deployment instance</li>
     *                    <li><u>instance:</u>Model deployment instance number</li>
     *                    <li><u>maxSendRate:</u>Maximum send rate we would like to reach during the measurement</li>
     *                    <li><u>sendRateSteps:</u>Steps to increase the send rate. The first value is the start send
     *                    rate</li>
     *                    <li><u>measurementLength:</u>Length of a measurement step in seconds</li>
     *                    </ul>
     * @JcatTcAction Onboard model
     * @JcatTcActionResult Model package onboarded and available in MXE
     * @JcatTcAction Create a model service
     * @JcatTcActionResult Model service is created
     * @JcatTcAction Read model input json file
     * @JcatTcActionResult Model input lines are collected, which will be used for prediction
     * @JcatTcAction Start sending traffic with the selected send rate (the first value is the sendRateSteps)
     * @JcatTcActionResult Traffic sending started
     * @JcatTcAction Continously collect all the previously listed measurements from prometheus and store it in CSV file
     * @JcatTcActionResult Data got from prometheus and stored in CSV file
     * @JcatTcAction Stop traffic after measurementLength expired
     * @JcatTcActionResult Traffic stopped
     * @JcatTcAction Perform the previous traffic starting and measurement steps for all of the test rates (increasing
     *               by sendRateSteps to maxSendRate)
     * @JcatTcActionResult Measurement repeated for all of the send rate levels
     * @JcatTcAction Stop data collection after measurementLength expired
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
    @JcatMethod(testTag = "TEST-MXE-MODEL-PERFORMANCE", testTitle = "MXE model performance test")
    @Parameters({"packageName", "modelName", "modelVersion", "instanceName", "instance", "maxSendRate", "sendRateSteps",
            "measurementLength"})
    @SuppressWarnings("squid:S00107")
    public void testMxeModelPerformance(String packageName, String modelName, String modelVersion, String instanceName,
            int instance, final int maxSendRate, final int sendRateSteps, int measurementLength) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            instanceName = onboardAndStartModel(mxeCliDriver, packageName, modelName, modelVersion, instance);

            setTestStepBegin("Init");
            setTestInfo("modelDeploymentName is: " + instanceName);
            hardwareInfoFromPrometheus = collectHardwareInfoFromPrometheus(prometheusDriver);
            collectHardwareInfoFromNodes();
            final String requestData = Base64.getEncoder().encodeToString(
                    IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("model/indian_elephant.jpg")));

            final Duration duration = Duration.ofSeconds(measurementLength);
            final List<ModelTrafficRecord> measurementList = new ArrayList<>();
            for (int currentSendRate = sendRateSteps; currentSendRate <= maxSendRate; currentSendRate +=
                    sendRateSteps) {
                setTestStepBegin("Traffic scenario [requestPerSec: %d][duration: %s]", currentSendRate, duration);
                setSubTestStep("Traffic execution");
                final ModelTrafficGenerator trafficGenerator =
                        new ModelTrafficGenerator(mxeCluster.getEndpoint(), instanceName, mxeCluster.getMxeUser());
                final Instant start = Instant.now();
                final ModelTrafficScenarioResult modelTrafficScenarioResult =
                        trafficGenerator.executeTrafficScenario(currentSendRate, duration, requestData);
                final Instant end = Instant.now();
                setSubTestStepEnd();
                setSubTestStep("Print stats");
                List<ModelTrafficRecord> trafficScenarioStatistics =
                        ModelTrafficStatistics.modelTrafficRecords(modelTrafficScenarioResult, Duration.ofSeconds(6));
                double averageSuccessRate = trafficScenarioStatistics.stream().mapToDouble(e -> e.getNumberOfSuccess())
                        .average().getAsDouble();
                setTestInfo("Average success rate with " + instance + " instance(s): " + averageSuccessRate);
                setTestInfo("Expected duration: " + duration + " actual duration: " + Duration.between(start, end));
                measurementList.addAll(trafficScenarioStatistics);
                setSubTestStepEnd();
            }
            storeTrafficMeasurementInCsv("request_rates_and_response_times",
                    ModelTrafficStatistics.buildModelTrafficCsv(measurementList.stream()));
            generateChart(measurementList);
            stopAndDeleteModel(mxeCliDriver, modelName, modelVersion, instanceName);
        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    private void stopAndDeleteModel(final MxeCliDriver mxeCliDriver, String modelName, String modelVersion,
            String instanceName) {
        serviceDelete(mxeCliDriver, instanceName);
        modelDelete(mxeCliDriver, modelName, modelVersion);
    }

    private String onboardAndStartModel(final MxeCliDriver mxeCliDriver, String packageName, String modelName,
            String modelVersion, int instance) {
        String instanceName;
        modelOnboard(mxeCliDriver, modelName, modelVersion, packageName);
        getOnboardedModelList(mxeCliDriver);

        instanceName = serviceCreateInStep(mxeCliDriver, modelName, modelVersion, instance);
        serviceListInStep(mxeCliDriver);
        verifyServiceInstanceNumber(mxeCliDriver, instanceName, instance);
        waitUntilServiceStatusIs(mxeCliDriver, instanceName, STATUS_RUNNING, MODEL_START_TIMEOUT);
        return instanceName;
    }

    /**
     * @JcatTcDescription Performs performance test with a model deployment in a flow
     * @JcatTcPreconditions MXE cluster is up and running in a separated characteristics environment
     * @JcatTcInstruction The testcase is about sending data to a running model deployment in a flow with different rate
     *                    and measure the following characteristics for each send rate: <br/>
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
     *                    Since several preparatory steps are needed for execution of this performance test case, the
     *                    followings are needed:
     *                    <ul>
     *                    <li>always execute with a predefined test suite</li>
     *                    </ul>
     *                    <b>Performance test parameters</b>
     *                    <ul>
     *                    <li><u>packageName:</u> Model package nam and path to be used during the test</li>
     *                    <li><u>modelName:</u> Name of the model after onboarding</li>
     *                    <li><u>modelVersion:</u>Version of the model</li>
     *                    <li><u>instanceName:</u>Name of the model deployment instance</li>
     *                    <li><u>instance:</u>Model deployment instance number</li>
     *                    <li><u>flowName:</u>Flow to onboard a deploy</li>
     *                    <li><u>modelInputJsonPath:</u>Input json for model prediction</li>
     *                    <li><u>maxSendRate:</u>Maximum send rate we would like to reach during the measurement</li>
     *                    <li><u>sendRateSteps:</u>Steps to increase the send rate. The first value is the start send
     *                    rate</li>
     *                    <li><u>measurementLength:</u>Length of a measurement step in seconds</li>
     *                    </ul>
     * @JcatTcAction Onboard model
     * @JcatTcActionResult Model package onboarded and available in MXE
     * @JcatTcAction Start a model deploymentent is running
     * @JcatTcActionResult Model is running
     * @JcatTcAction Onboard a flow which contains a previously started model
     * @JcatTcActionResult flow onboarded
     * @JcatTcAction Deploy a flow which contains a previously started model
     * @JcatTcActionResult flow deployed
     * @JcatTcAction Read model input json file
     * @JcatTcActionResult Model input lines are collected, which will be used for prediction
     * @JcatTcAction Start sending traffic with the selected send rate (the first value is the sendRateSteps)
     * @JcatTcActionResult Traffic sending started
     * @JcatTcAction Continously collect all the previously listed measurements from prometheus and store it in CSV file
     * @JcatTcActionResult Data got from prometheus and stored in CSV file
     * @JcatTcAction Stop traffic after measurementLength expired
     * @JcatTcActionResult Traffic stopped
     * @JcatTcAction Perform the previous traffic starting and measurement steps for all of the test rates (increasing
     *               by sendRateSteps to maxSendRate)
     * @JcatTcActionResult Measurement repeated for all of the send rate levels
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
    @JcatMethod(testTag = "TEST-MXE-FLOW-PERFORMANCE", testTitle = "MXE flow performance test")
    @Parameters({"packageName", "modelName", "modelVersion", "instanceName", "instance", "flowName",
            "modelInputJsonPath", "maxSendRate", "sendRateSteps", "measurementLength"})
    @SuppressWarnings("squid:S00107")
    public void testMxeFlowPerformance(String packageName, String modelName, String modelVersion, String instanceName,
            int instance, String flowName, String modelInputJsonPath, int maxSendRate, int sendRateSteps,
            int measurementLength) {
        // Not implemented yet
    }


    /**
     * @JcatTcDescription Performs performance test with an image recognition (inception3) model deployment
     * @JcatTcPreconditions MXE cluster is up and running in a separated characteristics environment
     * @JcatTcInstruction The testcase is about sending data to a running model deployment one after the other and
     *                    measure the sum prediction time and calculate the average prediction time. This average time
     *                    then can be compared to the baseline test of the same model.
     * @JcatTcAction Onboard model
     * @JcatTcActionResult Model package onboarded and available in MXE
     * @JcatTcAction Start a model service
     * @JcatTcActionResult Model service is running
     * @JcatTcAction Read the images from the input directory, convert it to BASE64 encoded request data and send it to
     *               the model endpoint. Measurement is done during the process.
     * @JcatTcActionResult All image files are read from the input directory and sent for prediction. Measurement is
     *                     finished.
     * @JcatTcAction Log test results: number of predictions, sum prediction time.
     * @JcatTcActionResult Test results are displayed in logs
     * @JcatTcAction Draw diagrams about the collected data. Separate diagram for all measurement type which contains
     *               measured values for each send rate
     * @JcatTcActionResult Diagrams generated
     * @JcatTcAction Stop previously started model service
     * @JcatTcActionResult Model service stopped
     * @JcatTcAction Delete onboarded model
     * @JcatTcActionResult Model deleted
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-STANDARD-MODEL-PERFORMANCE", testTitle = "MXE standard model performance test")
    @Parameters({"packageName", "modelName", "modelVersion", "instance", "modelInputDir"})
    public void testStandardMxeModelPerformance(String packageName, String modelName, String modelVersion, int instance,
            String modelInputDir) {

        Function<MxeCliDriver, String> onboardAndStartFunction =
                (mxeCliDriver) -> onboardAndStartModel(mxeCliDriver, packageName, modelName, modelVersion, instance);

        Collection<File> imageFiles = FileUtils.listFiles(new File(modelInputDir), CanReadFileFilter.CAN_READ, null);
        final AtomicLong sumNonPredictionTime = new AtomicLong(0);
        final AtomicInteger imageCount = new AtomicInteger(0);

        Stream<Pair<String, String>> entities = imageFiles.stream().flatMap(image -> {
            final long start = System.currentTimeMillis();
            try {
                String requestData = Base64.getEncoder().encodeToString(IOUtils.toByteArray(image.toURI()));
                Stream<Pair<String, String>> ret = Stream.of(Pair.of(image.getName(), requestData));
                imageCount.incrementAndGet();
                setTestInfo("Loaded: " + image);
                return ret;
            } catch (IOException e) {
                setTestError("Failed to load file: " + image, e);
                return Stream.empty();
            } finally {
                final long end = System.currentTimeMillis();
                sumNonPredictionTime.addAndGet(end - start);
            }
        });
        executePerformanceTestUsingModel(modelName, modelVersion, sumNonPredictionTime, () -> imageCount.get(),
                entities, onboardAndStartFunction, mxeCluster.getMxeUser());

    }

    /**
     * @JcatTcDescription Performs performance test with a Telco (user behavior clustering in our case) model deployment
     * @JcatTcPreconditions MXE cluster is up and running in a separated characteristics environment
     * @JcatTcInstruction The testcase is about sending data to a running model deployment one after the other and
     *                    measure the sum prediction time and calculate the average prediction time. This average time
     *                    then can be compared to the baseline test of the same model.
     * @JcatTcAction Onboard model from source
     * @JcatTcActionResult Model is onboarded and available in MXE
     * @JcatTcAction Start a model service
     * @JcatTcActionResult Model service is running
     * @JcatTcAction Generate random (but valid) data and send it to the model endpoint. Measurement is done during the
     *               process.
     * @JcatTcActionResult Random datasent sent for prediction. Measurement is finished.
     * @JcatTcAction Log test results: number of predictions, sum prediction time.
     * @JcatTcActionResult Test results are displayed in logs
     * @JcatTcAction Draw diagrams about the collected data. Separate diagram for all measurement type which contains
     *               measured values for each send rate
     * @JcatTcActionResult Diagrams generated
     * @JcatTcAction Stop previously started model service
     * @JcatTcActionResult Model service stopped
     * @JcatTcAction Delete onboarded model
     * @JcatTcActionResult Model deleted
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-TELCO-MODEL-PERFORMANCE", testTitle = "MXE Telco model performance test")
    @Parameters({"modelSource", "modelName", "modelVersion"})
    public void testTelcoModelPerformance(String modelSource, String modelName, String modelVersion) {

        final AtomicLong randomGenTime = new AtomicLong(0);
        final int predictionCount = 1000;
        final String DATA_TEMPLATE = "%d, %s, %d";
        final Random random = new Random();
        final int min = 100000;
        final int max = 999999;
        Stream<Pair<String, String>> entities = IntStream.range(0, predictionCount).boxed().flatMap(index -> {
            final long start = System.currentTimeMillis();
            String requestData = String.format(
                    DATA_TEMPLATE, min + random.nextInt(max - min), IntStream.range(0, 37).boxed()
                            .map(i -> String.valueOf((double) random.nextInt(100))).collect(Collectors.joining(", ")),
                    System.currentTimeMillis());
            setTestInfo("Generated: " + requestData);
            Stream<Pair<String, String>> ret = Stream.of(Pair.of(requestData, requestData));
            randomGenTime.addAndGet(System.currentTimeMillis() - start);
            return ret;
        });

        executePerformanceTestUsingModel(modelName, modelVersion, randomGenTime, () -> predictionCount, entities,
                (mxeCliDriver) -> onboardAndStartFromSource(modelSource, modelName, modelVersion, mxeCliDriver),
                mxeCluster.getMxeUser());

    }

    /**
     * @JcatTcDescription Performs performance test with an Empty (returns just the echo of the input) model deployment
     * @JcatTcPreconditions MXE cluster is up and running in a separated characteristics environment
     * @JcatTcInstruction The testcase is about sending data to a running model deployment one after the other and
     *                    measure the sum prediction time and calculate the average prediction time. This average time
     *                    then can be compared to the baseline test of the same model.
     * @JcatTcAction Onboard model from source
     * @JcatTcActionResult Model is onboarded and available in MXE
     * @JcatTcAction Start a model service
     * @JcatTcActionResult Model service is running
     * @JcatTcAction Generate random (but valid) data and send it to the model endpoint. Measurement is done during the
     *               process.
     * @JcatTcActionResult Random datasent sent for prediction. Measurement is finished.
     * @JcatTcAction Log test results: number of predictions, sum prediction time.
     * @JcatTcActionResult Test results are displayed in logs
     * @JcatTcAction Draw diagrams about the collected data. Separate diagram for all measurement type which contains
     *               measured values for each send rate
     * @JcatTcActionResult Diagrams generated
     * @JcatTcAction Stop previously started model service
     * @JcatTcActionResult Model service stopped
     * @JcatTcAction Delete onboarded model
     * @JcatTcActionResult Model deleted
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "TEST-MXE-EMPTY-MODEL-PERFORMANCE", testTitle = "MXE Empty model performance test")
    @Parameters({"modelSource", "modelName", "modelVersion"})
    public void testEmptyModelPerformance(String modelSource, String modelName, String modelVersion) {

        final AtomicLong randomGenTime = new AtomicLong(0);
        final int predictionCount = 1000;
        Stream<Pair<String, String>> entities = IntStream.range(0, predictionCount).boxed().flatMap(index -> {
            String requestData = String.valueOf(index);
            setTestInfo("Generated: " + requestData);
            Stream<Pair<String, String>> ret = Stream.of(Pair.of(requestData, requestData));
            return ret;
        });

        executePerformanceTestUsingModel(modelName, modelVersion, randomGenTime, () -> predictionCount, entities,
                (mxeCliDriver) -> onboardAndStartFromSource(modelSource, modelName, modelVersion, mxeCliDriver),
                mxeCluster.getMxeUser());

    }

    public void executePerformanceTestUsingModel(String modelName, String modelVersion,
            final AtomicLong sumNonPredictionTime, final Supplier<Integer> predictionCount,
            Stream<Pair<String, String>> entities, Function<MxeCliDriver, String> onboardAndStartFunction,
            User mxeUser) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            String instanceName = onboardAndStartFunction.apply(mxeCliDriver);

            setTestStepBegin("Start prediction");
            sleepSec(10);

            final ModelTrafficGenerator trafficGenerator =
                    new ModelTrafficGenerator(mxeCluster.getEndpoint(), instanceName, mxeUser);
            final long start = System.currentTimeMillis();
            final ModelTrafficScenarioResult modelTrafficScenarioResult =
                    trafficGenerator.sendSingleThreaded(entities, ModelTrafficGenerator.DEFAULT_REQUEST_TIMEOUT_MILLIS);
            final long duration = System.currentTimeMillis() - start;

            setTestStepBegin("Stats");
            setSubTestStep("Durations");
            setAdditionalInfo("<b>NrOfPredictions: " + predictionCount.get() + "</b>");
            setAdditionalInfo("<b>Failed: "
                    + (predictionCount.get() - modelTrafficScenarioResult.getResponse2xxSuccessful()) + "</b>");
            setAdditionalInfo("<b>Full duration (ms): " + duration + "</b>");
            setAdditionalInfo("<b>SumNonPredictionTime (ms): " + sumNonPredictionTime.get() + "</b>");
            setAdditionalInfo("<b>SumPredictionTime (ms): " + (duration - sumNonPredictionTime.get()) + "</b>");
            printStatisticInSubsteps(modelTrafficScenarioResult, -1);

            stopAndDeleteModel(mxeCliDriver, modelName, modelVersion, instanceName);

        }
    }

    private static String onboardAndStartFromSource(String modelSource, String modelName, String modelVersion,
            final MxeCliDriver mxeCliDriver) {
        modelOnboardSource(mxeCliDriver, modelName, modelSource);
        waitUntilModelStatusIs(mxeCliDriver, modelName, modelVersion, STATUS_AVAILABLE, MODEL_ONBOARD_TIMEOUT);
        sleepSec(20);
        String instanceName = serviceCreateInStep(mxeCliDriver, modelName, modelVersion, 1);
        waitUntilServiceStatusIs(mxeCliDriver, instanceName, STATUS_CREATING, MODEL_CREATE_TIMEOUT);
        waitUntilServiceStatusIs(mxeCliDriver, instanceName, STATUS_RUNNING, MODEL_START_TIMEOUT);
        return instanceName;
    }

    private void executeTrafficScenario(final String baseUrl, final String modelDeploymentName, final int requestPerSec,
            final int overallRequestCount, final String requestData, User mxeUser) {
        final ModelTrafficGenerator trafficGenerator = new ModelTrafficGenerator(baseUrl, modelDeploymentName, mxeUser);

        setTestStepBegin("Traffic scenario [requestPerSec: %d][overallRequestCount: %d]", requestPerSec,
                overallRequestCount);
        setSubTestStep("Traffic execution");
        final List<ModelTrafficRecord> measurementList = new ArrayList<>();
        final long start = System.currentTimeMillis();
        final ModelTrafficScenarioResult modelTrafficScenarioResult =
                trafficGenerator.executeTrafficScenario(requestPerSec, overallRequestCount, requestData);
        final long end = System.currentTimeMillis();
        setSubTestStepEnd();
        printStatisticInSubsteps(modelTrafficScenarioResult, overallRequestCount,
                Duration.ofMillis((overallRequestCount / requestPerSec * 1000)), Duration.ofMillis(end - start));

        setTestStepEnd();
        storeTrafficMeasurementInCsv("request_rates_and_response_times",
                ModelTrafficStatistics.buildModelTrafficCsv(measurementList.stream()));
    }

    public void printStatisticInSubsteps(final ModelTrafficScenarioResult modelTrafficScenarioResult,
            final int overallRequestCount, final Duration expectedDuration, final Duration duration) {
        setSubTestStep("Duration");
        setTestInfo("Duration should took: " + expectedDuration);
        setTestInfo("Real duration: " + duration);
        setSubTestStepEnd();
        printStatisticInSubsteps(modelTrafficScenarioResult, overallRequestCount);
    }

    public void printStatisticInSubsteps(final ModelTrafficScenarioResult modelTrafficScenarioResult,
            final int overallRequestCount) {
        setSubTestStep("Results");
        final int response1xxInformationalResult = modelTrafficScenarioResult.getResponse1xxInformational();
        final int response2xxSuccessfulResult = modelTrafficScenarioResult.getResponse2xxSuccessful();
        final int response3xxRedirectionResult = modelTrafficScenarioResult.getResponse3xxRedirection();
        final int response4xxClientErrorResult = modelTrafficScenarioResult.getResponse4xxClientError();
        final int response5xxServerErrorResult = modelTrafficScenarioResult.getResponse5xxServerError();
        final int responseUknownErrorResult = modelTrafficScenarioResult.getResponseUnknownError();
        setTestInfo("<b>response1xxInformational</b>: " + response1xxInformationalResult);
        setTestInfo("<b>response2xxSuccessful</b>: " + response2xxSuccessfulResult);
        setTestInfo("<b>response3xxRedirection</b>: " + response3xxRedirectionResult);
        setTestInfo("<b>response4xxClientError</b>: " + response4xxClientErrorResult);
        setTestInfo("<b>response5xxServerError</b>: " + response5xxServerErrorResult);
        setTestInfo("<b>responseUknownError</b>: " + responseUknownErrorResult);
        final int sumOfResults =
                response1xxInformationalResult + response2xxSuccessfulResult + response3xxRedirectionResult
                        + response4xxClientErrorResult + response5xxServerErrorResult + responseUknownErrorResult;
        if (overallRequestCount >= 0) {
            saveAssertThat("Request responses and overallRequest count mismatch", sumOfResults,
                    is(overallRequestCount));
        }
        setSubTestStepEnd();

        setSubTestStep("Latency");
        final Map<Long, Long> latency = modelTrafficScenarioResult.getLatency();
        setTestInfoExpandable("latency", latency.toString());
        final LongSummaryStatistics latencyStatistics =
                latency.values().stream().mapToLong(value -> value).filter(value -> value != -1L).summaryStatistics();
        setTestInfo(latencyStatistics.toString());
        setTestInfo("<b>min latency: </b>" + latencyStatistics.getMin() + "  ms");
        setTestInfo("<b>max latency: </b>" + latencyStatistics.getMax() + "  ms");
        setTestInfo("<b>average latency: </b>" + latencyStatistics.getAverage() + "  ms");
        setSubTestStepEnd();

        printResultByTimeAndResponseCode(modelTrafficScenarioResult);
        List<ModelTrafficRecord> modelTrafficRecords =
                ModelTrafficStatistics.modelTrafficRecords(modelTrafficScenarioResult, Duration.ofSeconds(6));
        generateChart(modelTrafficRecords);
    }

    private void printResultByTimeAndResponseCode(ModelTrafficScenarioResult modelTrafficScenarioResult) {
        setSubTestStep("Results by time and response code");
        String csv = buildResponseCSV(modelTrafficScenarioResult);
        setTestFile("result_time_and_response_code", csv, "csv", ".csv", false);
        setSubTestStepEnd();
    }

    private String buildResponseCSV(ModelTrafficScenarioResult modelTrafficScenarioResult) {
        StringBuilder content = new StringBuilder();
        content.append("START;END;DURATION;RESPONSE_CODE\n");

        for (Triple<Long, Long, String> response : mapToAllResponse(modelTrafficScenarioResult)) {
            content.append(formatSystemTimeMillis(response.getLeft()));
            content.append(';');
            content.append(formatSystemTimeMillis(response.getMiddle()));
            content.append(';');
            content.append(response.getMiddle() - response.getLeft());
            content.append(';');
            content.append(response.getRight());
            content.append('\n');
        }

        return content.toString();
    }

    private List<Triple<Long, Long, String>> mapToAllResponse(ModelTrafficScenarioResult modelTrafficScenarioResult) {
        List<Triple<Long, Long, String>> responses1xxInformational =
                mapResponseToCode(modelTrafficScenarioResult.getResponses1xxInformational(), "1xx");
        List<Triple<Long, Long, String>> responses2xxSuccessful =
                mapResponseToCode(modelTrafficScenarioResult.getResponses2xxSuccessful(), "2xx");
        List<Triple<Long, Long, String>> responses3xxRedirection =
                mapResponseToCode(modelTrafficScenarioResult.getResponses3xxRedirection(), "3xx");
        List<Triple<Long, Long, String>> responses4xxClientError =
                mapResponseToCode(modelTrafficScenarioResult.getResponses4xxClientError(), "4xx");
        List<Triple<Long, Long, String>> responses5xxServerError =
                mapResponseToCode(modelTrafficScenarioResult.getResponses5xxServerError(), "5xx");
        List<Triple<Long, Long, String>> responsesUnknownError =
                mapResponseToCode(modelTrafficScenarioResult.getResponsesUnknownError(), "unknown");

        List<Triple<Long, Long, String>> allResponses = Lists.newArrayList(responses1xxInformational);
        allResponses.addAll(responses2xxSuccessful);
        allResponses.addAll(responses3xxRedirection);
        allResponses.addAll(responses4xxClientError);
        allResponses.addAll(responses5xxServerError);
        allResponses.addAll(responsesUnknownError);
        allResponses.sort((t1, t2) -> t1.getLeft().compareTo(t2.getLeft()));
        return allResponses;
    }

    private static String formatSystemTimeMillis(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private List<Triple<Long, Long, String>> mapResponseToCode(
            List<Triple<Long, Long, ResponseEntity<String>>> responses, String responseCode) {
        return responses.stream().map(t -> Triple.of(t.getLeft(), t.getMiddle(), responseCode))
                .collect(Collectors.toList());
    }


    private void collectHardwareInfoFromNodes() {
        setTestStepBegin("Collect hardware info from nodes");
        List<TestExecutionHost> nodeList = mxeCluster.getNodeList();
        nodeList.forEach(node -> {
            setSubTestStep("Collect hardware info for " + node.getHost());
            CommandResult inxiResult = DriverFactory.getMxeCliDriver(node).execute(Commands.inxi(node).cpu());
            setTestInfo(inxiResult.getCommandOutput());
            setTestFile(String.format("%s_%s", "cpuData", node.getId()), inxiResult.getCommandOutput(), "json", "json",
                    false);
            setSubTestStepEnd();
        });
        setTestStepEnd();
    }

    public void generateChart(final List<ModelTrafficRecord> measurementList) {
        try {
            storeCharts(Arrays.asList(Pair.of(generateResponseTimeChart(measurementList), "performance_resp_time"),
                    Pair.of(generateRequestRateChart(measurementList), "performance_req_rate")));
        } catch (Exception ex) {
            setTestError("Failed to generate scalability chart!", ex);
        }
    }

    private XYChart generateRequestRateChart(final List<ModelTrafficRecord> measurementList) {
        XYChart chart = Charts.xyChart().addyAxis(new Axis(VALUE, "req/s")).addxAxis(new Axis(DATE, "timestamp"))
                .addSeries(Series.line("Success rate").colors(Palettes.green())
                        .addDataField(DATE, HORIZONTAL, "timestamp").addDataField(VALUE, VERTICAL, "successPerSec")
                        .tooltipText("Success rate {successPerSec} req/s").build())
                .addSeries(Series.line("Failure rate").colors(Palettes.red())
                        .addDataField(DATE, HORIZONTAL, "timestamp").addDataField(VALUE, VERTICAL, "errorPerSec")
                        .tooltipText("Failure rate: {errorPerSec} req/s").build())
                .data(measurementList.stream().map(e -> {
                    Map<String, Object> row = new HashMap<String, Object>();
                    row.put("successPerSec", e.getSuccessPerSec());
                    row.put("errorPerSec", e.getErrorPerSec());
                    row.put("timestamp", e.getStartTime().toEpochMilli());
                    return row;
                }).collect(Collectors.toList())).build();
        return chart;
    }

    private XYChart generateResponseTimeChart(final List<ModelTrafficRecord> measurementList) {
        Axis timestampAxis = new Axis(DATE, "timestamp");
        Axis responseTimeAxis = new Axis(VALUE, "Response time (sec)", false, true);
        Axis requestRateAxis = new Axis(VALUE, "RequestPerSec", true, true);

        XYChart chart = Charts.xyChart().addxAxis(timestampAxis).addyAxis(responseTimeAxis).addyAxis(requestRateAxis)
                .addSeries(Series.line("Avg response time").colors(Palettes.blue()).yAxis(responseTimeAxis)
                        .addDataField(DATE, HORIZONTAL, "timestamp").addDataField(VALUE, VERTICAL, "avgRespTimeSec")
                        .tooltipText("Avg: {avgRespTimeSec} sec").build())
                .addSeries(Series.line("Min response time").colors(Palettes.green()).yAxis(responseTimeAxis)
                        .addDataField(DATE, HORIZONTAL, "timestamp").addDataField(VALUE, VERTICAL, "minRespTimeSec")
                        .tooltipText("Min: {minRespTimeSec} sec").build())
                .addSeries(Series.line("Max response time").colors(Palettes.red()).yAxis(responseTimeAxis)
                        .addDataField(DATE, HORIZONTAL, "timestamp").addDataField(VALUE, VERTICAL, "maxRespTimeSec")
                        .tooltipText("Max: {maxRespTimeSec} sec").build())
                .addSeries(Series.line("Request rate").colors(Arrays.asList("#ffc107")).yAxis(requestRateAxis)
                        .addDataField(DATE, HORIZONTAL, "timestamp").addDataField(VALUE, VERTICAL, "requestPerSec")
                        .tooltipText("Request rate {requestPerSec} req/s").build())
                .data(measurementList.stream().map(e -> {
                    Map<String, Object> row = new HashMap<String, Object>();
                    row.put("avgRespTimeSec", e.getAverageResponseTimeSec());
                    row.put("minRespTimeSec", e.getMinResponseTimeSec());
                    row.put("maxRespTimeSec", e.getMaxResponseTimeSec());
                    row.put("requestPerSec", e.getRequestPerSec());
                    row.put("timestamp", e.getStartTime().toEpochMilli());
                    return row;
                }).collect(Collectors.toList())).build();
        return chart;
    }
}
