package de.unipassau.medspace.query_executor;

import de.unipassau.medspace.register.RegisterLifecycle;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.ApplicationLifecycle;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Created by David Goeth on 26.10.2017.
 */
public class QueryExecutorLifecycle {
  private static final Logger log = LoggerFactory.getLogger(QueryExecutorLifecycle.class);
  private final Repository db;

  @Inject
  public QueryExecutorLifecycle(ApplicationLifecycle lifecycle) throws IOException {

    File dataDir = new File("./_work/query_executor/native_store/");
    db = new SailRepository(new NativeStore(dataDir));
    try{
      db.initialize();
    } catch (RepositoryException e) {
      log.error("Couldn't initialize repository!");
    }

    lifecycle.addStopHook(() -> {
      log.info("shutdown is executing...");
      try {
        db.shutDown();
      } catch (RepositoryException e) {
        log.error("Couldn't shutdown repository!", e);
      }
      log.info("shutdown cleanup done.");
      return CompletableFuture.completedFuture(null);
    });

    log.info("QueryExecutorLifecycle is initialized.");
  }

  public Repository getRepository() {
    return db;
  }
}