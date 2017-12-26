package de.unipassau.medspace.data_collector;

import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.ApplicationLifecycle;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * Created by David Goeth on 07.11.2017.
 */
public class DataCollectorLifecycle implements Provider<RepositoryManager> {

  private static final Logger log = LoggerFactory.getLogger(DataCollectorLifecycle.class);

  private final RepositoryManager manager;

  @Inject
  public DataCollectorLifecycle(ApplicationLifecycle lifecycle) {

    File dataDir = new File("C:/bachelorThesisTest/_work/data_collector/native_store");
    manager = new LocalRepositoryManager(dataDir);

    //db = new SailRepository(new NativeStore(dataDir));
    try{
      //db.initialize();
      manager.initialize();
    } catch (RepositoryException e) {
      log.error("Couldn't initialize Repository!");
    }

    lifecycle.addStopHook(() -> {
      log.info("shutdown is executing...");
      try {
        //db.shutDown();
        manager.shutDown();
      } catch (RepositoryException e) {
        log.error("Couldn't shutdown Repository!", e);
      }
      log.info("shutdown cleanup done.");
      return CompletableFuture.completedFuture(null);
    });

    log.info("DataCollectorLifecycle is initialized.");
  }

  @Override
  public RepositoryManager get() {
    return manager;
  }
}