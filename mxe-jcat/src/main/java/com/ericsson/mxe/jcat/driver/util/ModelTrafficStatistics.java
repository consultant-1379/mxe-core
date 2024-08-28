package com.ericsson.mxe.jcat.driver.util;

import static java.util.stream.Collectors.toList;
import com.ericsson.mxe.jcat.driver.model.ModelScalabilityTrafficRecord;
import com.ericsson.mxe.jcat.driver.model.ModelTrafficRecord;
import com.ericsson.mxe.jcat.driver.model.ModelTrafficScenarioResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.http.ResponseEntity;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class ModelTrafficStatistics {

    private static String MODEL_TRAFFIC_CSV_HEADERS = "START,END,NUMBER_OF_SUCCESS_REQUESTS,NUMBER_OF_ERROR_REQUESTS,"
            + "SUCCESS_REQUESTS_PER_SEC,ERROR_REQUESTS_PER_SEC,REQUESTS_PER_SEC,AVERAGE_RESPONSE_TIME,"
            + "MIN_RESPONSE_TIME,MAX_RESPONSE_TIME";
    private static String MODEL_STAB_TRAFFIC_CSV_HEADERS = MODEL_TRAFFIC_CSV_HEADERS + ",INSTANCE";

    private ModelTrafficStatistics() {}

    public static List<ModelTrafficRecord> modelTrafficRecords(ModelTrafficScenarioResult result, Duration scale) {
        return stream(result, scale).collect(toList());
    }

    public static List<ModelScalabilityTrafficRecord> modelStabilityTrafficRecords(ModelTrafficScenarioResult result,
            Duration scale, int instance) {
        return stream(result, scale).map(r -> new ModelScalabilityTrafficRecord(r, instance)).collect(toList());
    }

    private static Stream<ModelTrafficRecord> stream(ModelTrafficScenarioResult result, Duration scale) {
        List<Triple<Long, Long, ResponseEntity<String>>> emptyList = Collections.emptyList();

        ListIterator<Triple<Long, Long, ResponseEntity<String>>> itInfoList =
                result.getResponses1xxInformational() == null ? emptyList.listIterator()
                        : new ArrayList<>(result.getResponses1xxInformational()).listIterator();
        ListIterator<Triple<Long, Long, ResponseEntity<String>>> itSuccessList =
                result.getResponses2xxSuccessful() == null ? emptyList.listIterator()
                        : new ArrayList<>(result.getResponses2xxSuccessful()).listIterator();
        ListIterator<Triple<Long, Long, ResponseEntity<String>>> itClientErrorList =
                result.getResponses4xxClientError() == null ? emptyList.listIterator()
                        : new ArrayList<>(result.getResponses4xxClientError()).listIterator();
        ListIterator<Triple<Long, Long, ResponseEntity<String>>> itServerErrorList =
                result.getResponses5xxServerError() == null ? emptyList.listIterator()
                        : new ArrayList<>(result.getResponses5xxServerError()).listIterator();
        ListIterator<Triple<Long, Long, ResponseEntity<String>>> itUnknownErrorList =
                result.getResponsesUnknownError() == null ? emptyList.listIterator()
                        : new ArrayList<>(result.getResponsesUnknownError()).listIterator();

        Stream.Builder<ModelTrafficRecord> builder = Stream.builder();
        long startTime = getFirstItemTime(result);
        while (itSuccessList.hasNext() || itClientErrorList.hasNext() || itServerErrorList.hasNext()
                || itUnknownErrorList.hasNext() || itInfoList.hasNext()) {
            int totalResponseTimeMillis = 0;
            int numberOfSuccess = 0;
            int numberOfError = 0;
            int minResponseTimeMillis = Integer.MAX_VALUE;
            int maxResponseTimeMillis = 0;
            Optional<int[]> optionalSuccess =
                    getNumberOfItemsWithResponseTimeWithinScale(itSuccessList, startTime, scale);
            if (optionalSuccess.isPresent()) {
                numberOfSuccess += optionalSuccess.get()[0];
                totalResponseTimeMillis += optionalSuccess.get()[1];
                minResponseTimeMillis = Math.min(minResponseTimeMillis, optionalSuccess.get()[2]);
                maxResponseTimeMillis = Math.max(maxResponseTimeMillis, optionalSuccess.get()[3]);
            }
            Optional<int[]> optionalInfo =
                    getNumberOfItemsWithResponseTimeWithinScale(itClientErrorList, startTime, scale);
            Optional<int[]> optionalClientError =
                    getNumberOfItemsWithResponseTimeWithinScale(itClientErrorList, startTime, scale);
            Optional<int[]> optionalServerError =
                    getNumberOfItemsWithResponseTimeWithinScale(itServerErrorList, startTime, scale);
            Optional<int[]> optionalUnknownError =
                    getNumberOfItemsWithResponseTimeWithinScale(itUnknownErrorList, startTime, scale);
            for (Optional<int[]> optionalErrorResponse : Arrays.asList(optionalInfo, optionalClientError,
                    optionalServerError, optionalUnknownError)) {
                if (optionalErrorResponse.isPresent()) {
                    numberOfError += optionalErrorResponse.get()[0];
                    totalResponseTimeMillis += optionalErrorResponse.get()[1];
                    minResponseTimeMillis = Math.min(minResponseTimeMillis, optionalErrorResponse.get()[2]);
                    maxResponseTimeMillis = Math.max(maxResponseTimeMillis, optionalErrorResponse.get()[3]);
                }
            }
            startTime += scale.toMillis();
            double averageResponseTimeSec = totalResponseTimeMillis / (double) 1000 / (numberOfSuccess + numberOfError);
            builder.add(new ModelTrafficRecord(Instant.ofEpochMilli(startTime), scale, numberOfSuccess, numberOfError,
                    averageResponseTimeSec, minResponseTimeMillis / (double) 1000,
                    maxResponseTimeMillis / (double) 1000));
        }
        return builder.build();
    }


    private static long getFirstItemTime(ModelTrafficScenarioResult trafficScenarioResult) {
        long firstRequestTime = Long.MAX_VALUE;
        firstRequestTime = getFirstRequestTime(trafficScenarioResult.getResponses2xxSuccessful(), firstRequestTime);
        firstRequestTime = getFirstRequestTime(trafficScenarioResult.getResponses4xxClientError(), firstRequestTime);
        firstRequestTime = getFirstRequestTime(trafficScenarioResult.getResponses5xxServerError(), firstRequestTime);
        firstRequestTime = getFirstRequestTime(trafficScenarioResult.getResponsesUnknownError(), firstRequestTime);
        return firstRequestTime;
    }

    private static long getFirstRequestTime(List<Triple<Long, Long, ResponseEntity<String>>> resultList,
            long firstRequestTime) {
        if (!CollectionUtils.isEmpty(resultList)) {
            long requestTime = resultList.get(0).getLeft();
            return requestTime < firstRequestTime ? requestTime : firstRequestTime;
        } else {
            return firstRequestTime;
        }
    }

    private static Optional<int[]> getNumberOfItemsWithResponseTimeWithinScale(
            ListIterator<Triple<Long, Long, ResponseEntity<String>>> itResultList, long startTime, Duration scale) {
        if (!itResultList.hasNext()) {
            return Optional.empty();
        }
        int numberOfRequest = 0;
        int totalResponseTimeMillis = 0;
        int minResponseTimeMillis = Integer.MAX_VALUE;
        int maxResponseTimeMillis = 0;
        final long endTime = startTime + scale.toMillis();
        do {
            Triple<Long, Long, ResponseEntity<String>> item = itResultList.next();
            long requestStartTime = item.getLeft();
            long responseEndTime = item.getMiddle();
            if (requestStartTime > endTime) {
                itResultList.previous();
                break;
            }
            int responseTimeMillis = (int) (responseEndTime - requestStartTime);
            totalResponseTimeMillis += responseTimeMillis;
            minResponseTimeMillis = Math.min(minResponseTimeMillis, responseTimeMillis);
            maxResponseTimeMillis = Math.max(maxResponseTimeMillis, responseTimeMillis);
            numberOfRequest++;
        } while (itResultList.hasNext());

        return Optional
                .of(new int[] {numberOfRequest, totalResponseTimeMillis, minResponseTimeMillis, maxResponseTimeMillis});
    }

    public static String buildModelScalabilityTrafficCsv(Stream<ModelScalabilityTrafficRecord> measurements) {
        StringBuilder content = new StringBuilder();
        content.append(MODEL_STAB_TRAFFIC_CSV_HEADERS).append("\n");
        measurements.forEach(stabTrafficRecord -> buildCsvRecord(stabTrafficRecord.getTrafficRecord(), content)
                .append(",").append(stabTrafficRecord.getInstance()).append("\n"));
        return content.toString();
    }

    public static String buildModelTrafficCsv(Stream<ModelTrafficRecord> measurements) {
        StringBuilder content = new StringBuilder();
        content.append(MODEL_TRAFFIC_CSV_HEADERS).append("\n");
        measurements.forEach(trafficRecord -> buildCsvRecord(trafficRecord, content).append("\n"));
        return content.toString();
    }

    private static StringBuilder buildCsvRecord(ModelTrafficRecord trafficRecord, StringBuilder content) {
        return content.append(trafficRecord.getStartTime()).append(",").append(trafficRecord.getEndTime()).append(",")
                .append(trafficRecord.getNumberOfSuccess()).append(",").append(trafficRecord.getNumberOfError())
                .append(",").append(trafficRecord.getSuccessPerSec()).append(",").append(trafficRecord.getErrorPerSec())
                .append(",").append(trafficRecord.getRequestPerSec()).append(",")
                .append(trafficRecord.getAverageResponseTimeSec()).append(",")
                .append(trafficRecord.getMinResponseTimeSec()).append(",")
                .append(trafficRecord.getMaxResponseTimeSec());
    }
}
