/** Copyright (c) 2022 Ericsson AB. All rights reserved. */

package com.ericsson.mxe.authorservice;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.ericsson.mxe.backendservicescommon.log.LogControlHandler;

@Service
public class FileWatchService {
    private static final Logger logger = LogManager.getLogger(FileWatchService.class);
    private static final String SERVICE_CONTAINER_NAME = "mxe-commons-author-service";
    private static final String LOG_CONTROL_FILE = "/app/config/logcontrol/logcontrol.json";
    private static final String SERVICE_LOGGER_NAME = "com.ericsson.mxe.authorservice";

    @Async("asyncExecutor")
    public void startWatchService() throws InterruptedException {
        logger.info("Starting FileWatchService");
        LogControlHandler logControlHandler =
                new LogControlHandler(LOG_CONTROL_FILE, SERVICE_CONTAINER_NAME, SERVICE_LOGGER_NAME);
        try {
            logControlHandler.start();
        } catch (IOException e) {
            logger.error("Error in starting async watch service for " + SERVICE_CONTAINER_NAME + ":" + e.getMessage());
        }

    }
}
