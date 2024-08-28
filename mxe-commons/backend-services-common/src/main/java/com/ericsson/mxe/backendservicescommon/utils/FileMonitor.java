/**
 * Copyright (c) 2022 Ericsson AB. All rights reserved.
 */
package com.ericsson.mxe.backendservicescommon.utils;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FileMonitor {
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private WatchService watcher;
    private Map<PropertyChangeListener, List<ListenerEntry>> listenerMap = new HashMap<>();

    private static final Logger logger = LogManager.getLogger();

    public FileMonitor() throws IOException, InterruptedException {
        this.watcher = FileSystems.getDefault().newWatchService();
    }

    public void subscribe(Path path, PropertyChangeListener listener) throws IOException {
        subscribe(path, listener, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);
    }

    public void subscribe(Path path, PropertyChangeListener listener, Kind<?>... eventKinds) throws IOException {
        WatchKey watchKey = path.register(watcher, eventKinds);
        List<ListenerEntry> list = listenerMap.get(listener);
        if (list == null) {
            list = new ArrayList<ListenerEntry>(1);
            list.add(new ListenerEntry(path, watchKey));
            listenerMap.put(listener, list);
            support.addPropertyChangeListener(listener);
        } else {
            list.add(new ListenerEntry(path, watchKey));
        }
    }

    public void unsubscribe(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
        List<ListenerEntry> list = listenerMap.get(listener);
        if (list != null) {
            list.forEach(entry -> entry.watchKey.cancel());
        }
        listenerMap.remove(listener);
    }

    public void start() {
        try {
            WatchKey key;

            while (true) {
                try {
                    key = watcher.take();
                } catch (InterruptedException ex) {
                    logger.info(ex.toString());
                    continue;
                }
                List<WatchEvent<?>> eventsList = key.pollEvents();
                // Reset the key immediately to reduce risk of missing
                // notifications
                if (!key.reset()) {
                    logger.error("Invalid watch key");
                }
                // Wait to be sure that file is updated before checking
                // Could be preceded by directory notifications
                Thread.sleep(1000);
                eventsList.stream().forEach((event) -> {
                    Path changedPath = (Path) event.context();
                    Kind<?> kind = event.kind();
                    logger.info("File event: {}, {}", kind.name(), kind.type());
                    if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                        support.firePropertyChange(changedPath.toString(), null,
                                new ChangedEvent(changedPath, ChangedEvent.ChangedEventKind.CREATE));
                    } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                        support.firePropertyChange(changedPath.toString(), null,
                                new ChangedEvent(changedPath, ChangedEvent.ChangedEventKind.DELETE));
                    } else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                        support.firePropertyChange(changedPath.toString(), null,
                                new ChangedEvent(changedPath, ChangedEvent.ChangedEventKind.MODIFY));
                    } else {
                        logger.warn("Unsupported event kind: {} , type: {} ", kind.name(), kind.type().getName());
                    }
                });
            }
        } catch (RuntimeException e) {
            logger.warn("Runtime exception happened {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Exception happened: {}", e.getMessage());
        } finally {
            logger.info("Restart to recover from FileMonitor issue");
        }
    }

    @Getter
    public static class ChangedEvent {
        private final Path path;
        private final ChangedEventKind kind;

        public enum ChangedEventKind {
            CREATE, DELETE, MODIFY
        }

        public ChangedEvent(Path path, ChangedEventKind kind) {
            this.path = path;
            this.kind = kind;
        }
    }

    @Getter
    private static class ListenerEntry {
        private final Path path;
        private final WatchKey watchKey;

        public ListenerEntry(Path path, WatchKey watchKey) {
            this.path = path;
            this.watchKey = watchKey;
        }
    }
}
