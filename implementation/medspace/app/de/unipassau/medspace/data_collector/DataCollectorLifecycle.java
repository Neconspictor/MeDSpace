package de.unipassau.medspace.data_collector;

import de.unipassau.medspace.common.play.ShutdownService;
import de.unipassau.medspace.data_collector.rdf4j.LocalRepositoryManager;
import de.unipassau.medspace.global.config.mapping.ConfigMapping;
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

  private final File dataDir;

  private LocalRepositoryManager manager;

  /**
   * Creates a new DataCollectorLifecycle object.
   *
   * @param lifecycle The application lifecycle.
   * @param globalConfig global MeDSpace configuration.
   * @throws IOException If an IO error occurs.
   */
  @Inject
  public DataCollectorLifecycle(ApplicationLifecycle lifecycle,
                                ConfigMapping globalConfig,
                                ShutdownService shutdownService) throws IOException {

    dataDir = new File(globalConfig
        .getDataCollector()
        .getNativeStoreDirectory());

    try {
      initDataDirectory(dataDir);
      manager = new LocalRepositoryManager(dataDir.getAbsolutePath());
    } catch (Exception e) {
      log.error("Error while initializing the Data collector lifecycle", e);
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }

    lifecycle.addStopHook(() -> {
      log.info("shutdown is executing...");
        initDataDirectory(dataDir);
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
    } catch (Exception e) {
      throw new IllegalStateException("Couldn't clean data directory: " + datadir.getPath(), e);
    }
  }

  @Override
  public LocalRepositoryManager get() {
    return manager;
  }
}