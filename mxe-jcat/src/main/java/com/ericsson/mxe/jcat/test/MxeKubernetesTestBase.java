package com.ericsson.mxe.jcat.test;

import com.ericsson.mxe.jcat.chart.Chart;
import com.ericsson.mxe.jcat.chart.HtmlChartRender;
import com.ericsson.mxe.jcat.driver.keycloak.KeycloakDriver;
import com.ericsson.mxe.jcat.driver.prometheus.PrometheusDriver;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import io.kubernetes.client.openapi.ApiException;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import se.ericsson.jcat.fw.annotations.JcatMethod;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class MxeKubernetesTestBase extends MxeTestBase {

    protected KeycloakDriver keycloakDriver;
    protected PrometheusDriver prometheusDriver;

    @JcatMethod(testTag = "KUBERNETES-TEST-SETUP", testTitle = "Kubernetes configuration")
    @BeforeTest
    public void setupForKubernetes() throws IOException, ApiException {
        initDrivers();
    }

    private void initDrivers() throws ApiException, IOException {
        setTestStepBegin("Creating drivers");
        setTestInfo("Creating Keycloak driver");
        keycloakDriver = DriverFactory.getKeycloakDriver("localhost", "admin", "My-super-secret-pw123", true);
        setTestInfo("Creating Prometheus driver");
        prometheusDriver = DriverFactory.getPrometheusDriver("localhost", true);
        setTestStepEnd();
    }

    @AfterTest
    public void teardown() {
        setTestStepBegin("Teardown");
        try {
            setTestInfo("Closing keycloakDriver");
            keycloakDriver.close();
        } catch (Exception e) {
            setTestError("Closing keycloakDriver failed", e);
        }
        try {
            setTestInfo("Closing prometheusDriver");
            prometheusDriver.close();
        } catch (Exception e) {
            setTestError("Closing prometheusDriver failed", e);
        }
        setTestInfo("Teardown finished");
        setTestStepEnd();
    }

    protected void storeTrafficMeasurementInCsv(String displayText, String csv) {
        if (csv.isEmpty()) {
            return;
        }
        setTestStepBegin("Storing measurement in csv");
        setTestFile(displayText, csv, "csv", ".csv", false);
        setTestStepEnd();
    }

    protected void storeCharts(List<Pair<Chart, String>> chartAndFileNamePairs) {
        setTestStepBegin("Storing charts");
        for (Pair<Chart, String> chartAndFileNamePair : chartAndFileNamePairs) {
            Chart chart = chartAndFileNamePair.getLeft();
            String fileName = chartAndFileNamePair.getRight();

            ByteArrayOutputStream html = new ByteArrayOutputStream();
            try (HtmlChartRender out = new HtmlChartRender(html)) {
                out.write(chart);
                setTestFile(fileName, html.toString("utf-8"), "report", ".html", false);
            } catch (IOException ex) {
                setTestError("Failed to store chart", ex);
            }
        }

        setTestStepEnd();

    }
}
