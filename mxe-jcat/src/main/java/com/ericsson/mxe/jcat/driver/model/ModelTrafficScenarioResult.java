package com.ericsson.mxe.jcat.driver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.http.ResponseEntity;

public class ModelTrafficScenarioResult {

    private List<Triple<Long, Long, ResponseEntity<String>>> responses1xxInformational;
    private List<Triple<Long, Long, ResponseEntity<String>>> responses2xxSuccessful;
    private List<Triple<Long, Long, ResponseEntity<String>>> responses3xxRedirection;
    private List<Triple<Long, Long, ResponseEntity<String>>> responses4xxClientError;
    private List<Triple<Long, Long, ResponseEntity<String>>> responses5xxServerError;
    private List<Triple<Long, Long, ResponseEntity<String>>> responsesUnknownError;
    private Map<Long, Long> latency;

    public int getResponse1xxInformational() {
        return responses1xxInformational.size();
    }

    public int getResponse2xxSuccessful() {
        return responses2xxSuccessful.size();
    }

    public int getResponse3xxRedirection() {
        return responses3xxRedirection.size();
    }

    public int getResponse4xxClientError() {
        return responses4xxClientError.size();
    }

    public int getResponse5xxServerError() {
        return responses5xxServerError.size();
    }

    public int getResponseUnknownError() {
        return responsesUnknownError.size();
    }

    public List<Triple<Long, Long, ResponseEntity<String>>> getResponses1xxInformational() {
        return responses1xxInformational;
    }

    public void add1xxInformational(Triple<Long, Long, ResponseEntity<String>> info) {
        responses1xxInformational.add(info);
    }

    public List<Triple<Long, Long, ResponseEntity<String>>> getResponses2xxSuccessful() {
        return responses2xxSuccessful;
    }

    public void add2xxSuccessful(Triple<Long, Long, ResponseEntity<String>> info) {
        responses2xxSuccessful.add(info);
    }

    public List<Triple<Long, Long, ResponseEntity<String>>> getResponses3xxRedirection() {
        return responses3xxRedirection;
    }

    public void add3xxRedirection(Triple<Long, Long, ResponseEntity<String>> info) {
        responses3xxRedirection.add(info);
    }

    public List<Triple<Long, Long, ResponseEntity<String>>> getResponses4xxClientError() {
        return responses4xxClientError;
    }

    public void add4xxClientError(Triple<Long, Long, ResponseEntity<String>> info) {
        responses4xxClientError.add(info);
    }

    public List<Triple<Long, Long, ResponseEntity<String>>> getResponses5xxServerError() {
        return responses5xxServerError;
    }

    public void add5xxServerError(Triple<Long, Long, ResponseEntity<String>> info) {
        responses5xxServerError.add(info);
    }

    public List<Triple<Long, Long, ResponseEntity<String>>> getResponsesUnknownError() {
        return responsesUnknownError;
    }

    public void addUnknownError(Triple<Long, Long, ResponseEntity<String>> info) {
        responsesUnknownError.add(info);
    }

    public Map<Long, Long> getLatency() {
        return latency;
    }

    public void putLatency(Long id, Long latencyValue) {
        latency.put(id, latencyValue);
    }

    public static ModelTrafficScenarioResultBuilder builder() {
        return new ModelTrafficScenarioResultBuilder();
    }

    public static final class ModelTrafficScenarioResultBuilder {

        private List<Triple<Long, Long, ResponseEntity<String>>> responses1xxInformational;
        private List<Triple<Long, Long, ResponseEntity<String>>> responses2xxSuccessful;
        private List<Triple<Long, Long, ResponseEntity<String>>> responses3xxRedirection;
        private List<Triple<Long, Long, ResponseEntity<String>>> responses4xxClientError;
        private List<Triple<Long, Long, ResponseEntity<String>>> responses5xxServerError;
        private List<Triple<Long, Long, ResponseEntity<String>>> responsesUnknownError;
        private Map<Long, Long> latency;

        private ModelTrafficScenarioResultBuilder() {}

        public ModelTrafficScenarioResultBuilder responses1xxInformational(
                List<Triple<Long, Long, ResponseEntity<String>>> responses1xxInformational) {
            this.responses1xxInformational = responses1xxInformational;
            return this;
        }

        public ModelTrafficScenarioResultBuilder responses2xxSuccessful(
                List<Triple<Long, Long, ResponseEntity<String>>> responses2xxSuccessful) {
            this.responses2xxSuccessful = responses2xxSuccessful;
            return this;
        }

        public ModelTrafficScenarioResultBuilder responses3xxRedirection(
                List<Triple<Long, Long, ResponseEntity<String>>> responses3xxRedirection) {
            this.responses3xxRedirection = responses3xxRedirection;
            return this;
        }

        public ModelTrafficScenarioResultBuilder responses4xxClientError(
                List<Triple<Long, Long, ResponseEntity<String>>> responses4xxClientError) {
            this.responses4xxClientError = responses4xxClientError;
            return this;
        }

        public ModelTrafficScenarioResultBuilder responses5xxServerError(
                List<Triple<Long, Long, ResponseEntity<String>>> responses5xxServerError) {
            this.responses5xxServerError = responses5xxServerError;
            return this;
        }

        public ModelTrafficScenarioResultBuilder responsesUknownError(
                List<Triple<Long, Long, ResponseEntity<String>>> responsesUknownError) {
            this.responsesUnknownError = responsesUknownError;
            return this;
        }

        public ModelTrafficScenarioResultBuilder latency(Map<Long, Long> latency) {
            this.latency = latency;
            return this;
        }

        public ModelTrafficScenarioResult build() {
            ModelTrafficScenarioResult modelTrafficScenarioResult = new ModelTrafficScenarioResult();
            modelTrafficScenarioResult.responses1xxInformational = this.responses1xxInformational;
            modelTrafficScenarioResult.responses2xxSuccessful = this.responses2xxSuccessful;
            modelTrafficScenarioResult.responses3xxRedirection = this.responses3xxRedirection;
            modelTrafficScenarioResult.responses4xxClientError = this.responses4xxClientError;
            modelTrafficScenarioResult.responses5xxServerError = this.responses5xxServerError;
            modelTrafficScenarioResult.responsesUnknownError = this.responsesUnknownError;
            modelTrafficScenarioResult.latency = this.latency;
            return modelTrafficScenarioResult;
        }

        public ModelTrafficScenarioResult buildDefault() {
            ModelTrafficScenarioResult modelTrafficScenarioResult = new ModelTrafficScenarioResult();
            modelTrafficScenarioResult.responses1xxInformational = Collections.synchronizedList(new ArrayList<>());
            modelTrafficScenarioResult.responses2xxSuccessful = Collections.synchronizedList(new ArrayList<>());
            modelTrafficScenarioResult.responses3xxRedirection = Collections.synchronizedList(new ArrayList<>());
            modelTrafficScenarioResult.responses4xxClientError = Collections.synchronizedList(new ArrayList<>());
            modelTrafficScenarioResult.responses5xxServerError = Collections.synchronizedList(new ArrayList<>());
            modelTrafficScenarioResult.responsesUnknownError = Collections.synchronizedList(new ArrayList<>());
            modelTrafficScenarioResult.latency = Collections.synchronizedMap(new HashMap<>());
            return modelTrafficScenarioResult;

        }
    }

}
