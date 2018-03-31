package de.unipassau.medspace.data_collector;

import de.unipassau.medspace.data_collector.rdf4j.LocalRepositoryManager;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.ApplicationLifecycle;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the lifecycle for the Data Collector module.
 */
public class DataCollectorLifecycle implements Provider<LocalRepositoryManager> {

  private static final Logger log = LoggerFactory.getLogger(DataCollectorLifecycle.class);

  private static final File DATADIR = new File("./_work/data_collector/native_store");

  private final LocalRepositoryManager manager;

  /**
   * Creates a new DataCollectorLifecycle object.
   *
   * @param lifecycle The application lifecycle.
   * @throws IOException If an IO error occurs.
   */
  @Inject
  public DataCollectorLifecycle(ApplicationLifecycle lifecycle) throws IOException {

    initDataDirectory(DATADIR);

    manager = new LocalRepositoryManager(DATADIR.getAbsolutePath());

    lifecycle.addStopHook(() -> {
      log.info("shutdown is executing...");
        initDataDirectory(DATADIR);
      log.info("shutdown cleanup done.");
      return CompletableFuture.completedFuture(null);
    });

    log.info("DataCollectorLifecycle is initialized.");
  }

  private void initDataDirectory(File datadir) {
    if (!datadir.exists()) {
      datadir.mkdirs();
    }

    try {
      FileUtils.cleanDirectory(datadir);
    } catch (IOException e) {
      throw new IllegalStateException("Couldn't clean data directory: " + datadir.getPath(), e);
    }
  }

  @Override
  public LocalRepositoryManager get() {
    return manager;
  }
}