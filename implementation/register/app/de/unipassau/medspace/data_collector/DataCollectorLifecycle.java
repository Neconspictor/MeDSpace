package de.unipassau.medspace.data_collector;

import de.unipassau.medspace.data_collector.rdf4j.LocalTestRepositoryManager;
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
 * TODO
 */
public class DataCollectorLifecycle implements Provider<LocalTestRepositoryManager> {

  /**
   * Logger instance of this class.
   */
  private static final Logger log = LoggerFactory.getLogger(DataCollectorLifecycle.class);

  /**
   * TODO
   */
  private static final File DATADIR = new File("./_work/data_collector/native_store");

  /**
   * TODO
   */
  private final LocalTestRepositoryManager manager;

  @Inject
  public DataCollectorLifecycle(ApplicationLifecycle lifecycle) throws IOException {

    initDataDirectory(DATADIR);

    manager = new LocalTestRepositoryManager(DATADIR.getAbsolutePath());

    /*try{
      //db.initialize();
      manager.initialize();
    } catch (RepositoryException e) {
      log.error("Couldn't initialize Repository!");
    }*/

    lifecycle.addStopHook(() -> {
      log.info("shutdown is executing...");
      /*try {
        manager.shutDown();
      } catch (RepositoryException e) {
        log.error("Couldn't shutdown Repository!", e);
      }*/
      log.info("shutdown cleanup done.");
      return CompletableFuture.completedFuture(null);
    });

    log.info("DataCollectorLifecycle is initialized.");
  }

  /**
   * TODO
   * @param datadir
   */
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
  public LocalTestRepositoryManager get() {
    return manager;
  }
}