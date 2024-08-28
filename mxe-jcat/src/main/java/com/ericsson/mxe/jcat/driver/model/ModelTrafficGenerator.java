package com.ericsson.mxe.jcat.driver.model;

import com.ericsson.mxe.jcat.config.User;
import com.ericsson.mxe.jcat.driver.rest.MxeRequestFactory;
import com.ericsson.mxe.jcat.driver.rest.Request;
import com.ericsson.mxe.jcat.driver.rest.RestDriver;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class ModelTrafficGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelTrafficGenerator.class);

    public static final int DEFAULT_REQUEST_TIMEOUT_MILLIS = 5000;
    private static final String MODEL_REQUEST_TEMPLATE_STRING = "{\"data\":{\"ndarray\":[[\"%s\"]]}}";
    private static final String MODEL_REQUEST_TEMPLATE_RAW = "{\"data\":{\"ndarray\":[[%s]]}}";

    private static final ThreadFactory threadFactory = runnable -> {
        final Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setDaemon(true);
        return thread;
    };

    private final String baseUrl;
    private final String userName;
    private final String password;
    private final String modelDeploymentName;

    public ModelTrafficGenerator(final String baseUrl, final String modelDeploymentName, User mxeUser) {
        this.baseUrl = baseUrl;
        this.userName = mxeUser.getUserName();
        this.password = mxeUser.getPassword();
        this.modelDeploymentName = modelDeploymentName;
    }

    public ModelTrafficScenarioResult executeTrafficScenario(final int requestPerSec, final Duration duration,
            final String requestData) {
        return executeTrafficScenario(requestPerSec, Math.toIntExact(duration.getSeconds() * requestPerSec),
                requestData, DEFAULT_REQUEST_TIMEOUT_MILLIS);
    }

    public ModelTrafficScenarioResult executeTrafficScenario(final int requestPerSec, final Duration duration,
            final String requestData, final int requestTimeout) {
        return executeTrafficScenario(requestPerSec, Math.toIntExact(duration.getSeconds() * requestPerSec),
                requestData, requestTimeout);
    }

    public ModelTrafficScenarioResult executeTrafficScenario(final int requestPerSec, final int overallRequestCount,
            final String requestData) {
        return executeTrafficScenario(requestPerSec, overallRequestCount, requestData, DEFAULT_REQUEST_TIMEOUT_MILLIS);
    }

    public ModelTrafficScenarioResult executeTrafficScenario(final int requestPerSec, final int overallRequestCount,
            final String requestData, final int requestTimeout) {

        final CountDownLatch requestCount = new CountDownLatch(overallRequestCount);
        final ModelTrafficScenarioResult modelTrafficScenarioResult =
                ModelTrafficScenarioResult.builder().buildDefault();

        Request<String> restRequest = MxeRequestFactory.feedModel(modelDeploymentName, "[" + requestData + "]");

        final Runnable request = () -> {
            final long requestId = requestCount.getCount();

            if (requestId == 0L) {
                return;
            }

            requestCount.countDown();

            sendRequest(requestId, restRequest, modelTrafficScenarioResult, requestTimeout);
        };

        LOGGER.info("<b>Scheduling traffic</b>");
        final ExecutorService requestExecutor = Executors.newCachedThreadPool(threadFactory);

        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, threadFactory);
        scheduler.scheduleAtFixedRate(() -> {
            if (requestCount.getCount() > 0) {
                requestExecutor.submit(request);
            }
        }, 0, 1000 / requestPerSec, TimeUnit.MILLISECONDS);

        try {
            requestCount.await();
            scheduler.shutdownNow();
            requestExecutor.shutdownNow();
            requestExecutor.awaitTermination(requestTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            LOGGER.error("Execution has been interrupted", ie);
        }

        return modelTrafficScenarioResult;
    }

    public void sendRequest(final long requestId, final Request<String> restRequest,
            final ModelTrafficScenarioResult result, final int requestTimeout) {
        RestDriver driver = new RestDriver(baseUrl, userName, password, requestTimeout);
        final Thread thread = Thread.currentThread();
        ResponseEntity<String> response = null;
        long requestStart = -1L;
        long requestStop = -1L;
        driver.login();
        try {
            requestStart = System.currentTimeMillis();
            response = driver.send(restRequest);
            requestStop = System.currentTimeMillis();
            final HttpStatus responseStatusCode = response.getStatusCode();

            if (responseStatusCode.is1xxInformational()) {
                result.add1xxInformational(Triple.of(requestStart, System.currentTimeMillis(), response));
            }
            if (responseStatusCode.is2xxSuccessful()) {
                result.add2xxSuccessful(Triple.of(requestStart, System.currentTimeMillis(), response));
            }
            if (responseStatusCode.is3xxRedirection()) {
                result.add3xxRedirection(Triple.of(requestStart, System.currentTimeMillis(), response));
            }
            if (responseStatusCode.is4xxClientError()) {
                result.add4xxClientError(Triple.of(requestStart, System.currentTimeMillis(), response));
            }
            if (responseStatusCode.is5xxServerError()) {
                result.add5xxServerError(Triple.of(requestStart, System.currentTimeMillis(), response));
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            response = new ResponseEntity<>(httpClientErrorException.getStatusCode());
            result.add4xxClientError(Triple.of(requestStart, System.currentTimeMillis(), response));
        } catch (HttpServerErrorException httpServerErrorException) {
            response = new ResponseEntity<>(httpServerErrorException.getStatusCode());
            result.add5xxServerError(Triple.of(requestStart, System.currentTimeMillis(), response));
        } catch (Exception exception) {
            LOGGER.error("Failed to send request", exception);
            result.addUnknownError(Triple.of(requestStart, System.currentTimeMillis(), null));
        } finally {
            String log;
            if (Objects.nonNull(response)) {
                log = "<b>[RequestId " + requestId + "][Status " + response.getStatusCode().toString() + "] thread:"
                        + thread.getId() + " " + thread.getName() + "</b>\n" + response.toString();
            } else {
                log = "<b>[" + requestId + "][NoStatusCode] thread:" + thread.getId() + " " + thread.getName()
                        + "</b>\n NoResponseBody";
            }
            if (requestStart != -1L && requestStop != -1L) {
                result.putLatency(requestId, requestStop - requestStart);
            } else {
                result.putLatency(requestId, -1L);
            }
            LOGGER.info(log);
        }
    }

    public static HttpEntity<String> createEntityForString(final String requestData, final MediaType mediaType) {
        return createEntity(String.format(MODEL_REQUEST_TEMPLATE_STRING, requestData), mediaType);
    }

    public static HttpEntity<String> createEntityRaw(final String requestData, final MediaType mediaType) {
        return createEntity(String.format(MODEL_REQUEST_TEMPLATE_RAW, requestData), mediaType);
    }

    public static HttpEntity<String> createEntity(final String requestData, final MediaType mediaType) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        return new HttpEntity<>(requestData, headers);
    }

    public ModelTrafficScenarioResult sendSingleThreaded(Stream<Pair<String, String>> entities, int requestTimeout) {
        final ModelTrafficScenarioResult result = ModelTrafficScenarioResult.builder().buildDefault();
        final AtomicLong requestId = new AtomicLong(0);
        entities.forEach(entity -> {
            LOGGER.info("Run prediction for: {}", entity.getLeft());
            sendRequest(requestId.getAndIncrement(),
                    MxeRequestFactory.feedModel(modelDeploymentName, "[" + entity.getRight() + "]"), result,
                    requestTimeout);
        });
        return result;
    }

}
