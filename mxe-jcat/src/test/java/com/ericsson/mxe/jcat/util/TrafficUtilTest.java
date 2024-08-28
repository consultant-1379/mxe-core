package com.ericsson.mxe.jcat.util;

import com.ericsson.mxe.jcat.driver.model.ModelTrafficRecord;
import com.ericsson.mxe.jcat.driver.model.ModelTrafficScenarioResult;
import com.ericsson.mxe.jcat.driver.util.ModelTrafficStatistics;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;

public class TrafficUtilTest {

    @Test
    public void testGetTrafficScenarioStatistics() {
        List<Triple<Long, Long, ResponseEntity<String>>> responses2xxSuccessful = new ArrayList<>();
        List<Triple<Long, Long, ResponseEntity<String>>> response5xxServerError = new ArrayList<>();
        long time = System.currentTimeMillis();
        final int successRequestDuration = 1500;
        final int errorRequestDuration = 2000;
        // Adding success and error entities within the first 6 seconds
        responses2xxSuccessful.add(Triple.of(time, time + successRequestDuration, new ResponseEntity<>(HttpStatus.OK)));
        response5xxServerError
                .add(Triple.of(time, time + errorRequestDuration, new ResponseEntity<>(HttpStatus.BAD_REQUEST)));
        time += 500;
        responses2xxSuccessful.add(Triple.of(time, time + successRequestDuration, new ResponseEntity<>(HttpStatus.OK)));
        response5xxServerError
                .add(Triple.of(time, time + errorRequestDuration, new ResponseEntity<>(HttpStatus.BAD_REQUEST)));
        time += 500;
        responses2xxSuccessful.add(Triple.of(time, time + successRequestDuration, new ResponseEntity<>(HttpStatus.OK)));

        // Adding success and error entities within the second 6 seconds
        time += 7000;
        responses2xxSuccessful.add(Triple.of(time, time + successRequestDuration, new ResponseEntity<>(HttpStatus.OK)));
        response5xxServerError
                .add(Triple.of(time, time + errorRequestDuration, new ResponseEntity<>(HttpStatus.BAD_REQUEST)));
        time += 500;
        responses2xxSuccessful.add(Triple.of(time, time + successRequestDuration, new ResponseEntity<>(HttpStatus.OK)));
        response5xxServerError
                .add(Triple.of(time, time + errorRequestDuration, new ResponseEntity<>(HttpStatus.BAD_REQUEST)));
        time += 500;
        responses2xxSuccessful.add(Triple.of(time, time + successRequestDuration, new ResponseEntity<>(HttpStatus.OK)));

        ModelTrafficScenarioResult trafficScenarioResult = ModelTrafficScenarioResult.builder()
                .responses2xxSuccessful(responses2xxSuccessful).responses5xxServerError(response5xxServerError).build();
        Duration scale = Duration.ofSeconds(6);
        List<ModelTrafficRecord> trafficScenarioStatistics =
                ModelTrafficStatistics.modelTrafficRecords(trafficScenarioResult, scale);

        assertEquals("The number of collected statistics should be 2 but it is " + trafficScenarioStatistics.size(), 2,
                trafficScenarioStatistics.size());

        ModelTrafficRecord statisticsRecord = trafficScenarioStatistics.get(0);

        int expectedNumberOfSuccessRequests = 3;
        assertEquals(
                "The number of success requests should be " + expectedNumberOfSuccessRequests + " but it is "
                        + statisticsRecord.getNumberOfSuccess(),
                expectedNumberOfSuccessRequests, statisticsRecord.getNumberOfSuccess());

        int expectedNumberOfErrorRequests = 2;
        assertEquals(
                "The number of error requests should be " + expectedNumberOfErrorRequests + " but it is "
                        + statisticsRecord.getNumberOfError(),
                expectedNumberOfErrorRequests, statisticsRecord.getNumberOfError());

        double expectedAverageResponseTimeSec =
                (successRequestDuration / (double) 1000 * 3 + errorRequestDuration / (double) 1000 * 2) / 5;
        assertEquals(
                "The average response time should be " + expectedNumberOfErrorRequests + " but it is "
                        + statisticsRecord.getAverageResponseTimeSec(),
                expectedAverageResponseTimeSec, statisticsRecord.getAverageResponseTimeSec(), 0);

        double expectedRequestsPerSec = 5 / (scale.toMillis() / (double) 1000);
        assertEquals(
                "The requests per sec should be " + expectedRequestsPerSec + " but it is "
                        + statisticsRecord.getRequestPerSec(),
                expectedRequestsPerSec, statisticsRecord.getRequestPerSec(), 0);

    }
}
