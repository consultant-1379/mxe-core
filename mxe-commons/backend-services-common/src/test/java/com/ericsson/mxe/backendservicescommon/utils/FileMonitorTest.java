package com.ericsson.mxe.backendservicescommon.utils;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.common.io.Files;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class FileMonitorTest {

    private static final Logger logger = LogManager.getLogger();

    @BeforeEach
    public void setUp() {}

    @AfterEach
    public void tearDown() {}

    @Test
    public void FILE_MONITOR_TEST() throws InterruptedException, IOException {
        var monitor = new FileMonitor();
        AtomicBoolean propoerty1Updated = new AtomicBoolean(false);
        AtomicBoolean propoerty2Updated = new AtomicBoolean(false);

        PropertyChangeListener listener = (event) -> {
            propoerty1Updated.set(true);
            logger.info("1" + event.getPropertyName() + ((FileMonitor.ChangedEvent) event.getNewValue()).getKind()
                    + ((FileMonitor.ChangedEvent) event.getNewValue()).getPath().toAbsolutePath());
        };
        PropertyChangeListener listener2 = (event) -> {
            propoerty2Updated.set(true);
            logger.info("2" + event.getPropertyName() + ((FileMonitor.ChangedEvent) event.getNewValue()).getKind()
                    + ((FileMonitor.ChangedEvent) event.getNewValue()).getPath().toAbsolutePath());
        };
        logger.info(System.getProperty("user.dir"));
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        File file = new File(String.valueOf(Paths.get(System.getProperty("user.dir"))), uuidAsString);
        Files.touch(file);
        // display latest lastmodified
        logger.info(file.lastModified());

        monitor.subscribe(Paths.get(System.getProperty("user.dir")), listener);
        monitor.subscribe(Paths.get(System.getProperty("user.dir")), listener2);
        final ExecutorService exService = Executors.newSingleThreadExecutor();
        exService.execute(new Runnable() { // or use submit to get a Future (a result of computation,
            // you'll need a Callable, rather than runnable then)
            public void run() {
                monitor.start();
            }
        });

        try (OutputStream outputStream =
                java.nio.file.Files.newOutputStream(Paths.get(System.getProperty("user.dir"), uuidAsString))) {
            outputStream.write(123);
            outputStream.flush();
            outputStream.close();
            logger.info("Successfully wrote to the file.");
        }

        // waits for termination for 30 seconds
        try {
            exService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        file.delete();

        assertThat(propoerty1Updated.get(), is(true));
        assertThat(propoerty2Updated.get(), is(true));

        logger.info("Unsubscribe");
        monitor.unsubscribe(listener);
        monitor.unsubscribe(listener2);

    }
}
