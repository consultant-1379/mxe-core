/** Copyright (c) 2022 Ericsson AB. All rights reserved. */
package com.ericsson.mxe.modelcatalogueservice;

import com.ericsson.mxe.modelcatalogueservice.services.FileWatchService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CommandLineRunnerBean implements CommandLineRunner {

    private static final Logger logger = LogManager.getLogger(CommandLineRunnerBean.class);

    private final FileWatchService fileWatchService;

    public CommandLineRunnerBean(FileWatchService fileWatchService) {
        this.fileWatchService = fileWatchService;
    }


    @Override
    public void run(String... args) throws Exception {
        // Start the watcher
        logger.info("Starting Async File Watch Service");
        fileWatchService.startWatchService();
    }
}
