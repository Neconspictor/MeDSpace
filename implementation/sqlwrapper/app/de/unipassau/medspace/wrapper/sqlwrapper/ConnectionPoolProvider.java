package de.unipassau.medspace.wrapper.sqlwrapper;

import com.google.inject.Inject;
import com.google.inject.Provider;
import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.SQL.HikariConnectionPool;
import de.unipassau.medspace.common.play.ShutdownService;
import de.unipassau.medspace.d2r.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.ApplicationLifecycle;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * A provider for a SQL connection pool.
 */
public class ConnectionPoolProvider implements Provider<ConnectionPool> {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(ConnectionPoolProvider.class);

  private ConnectionPool connectionPool;

  /**
   * Creates a new ConnectionPoolProvider object.
   * @param d2rConfig The d2r mapping configuration.
   * @param lifecycle The applicaiton lifecycle.
   * @param shutdownService The shutdown service.
   */
  @Inject
  public ConnectionPoolProvider(Configuration d2rConfig, ApplicationLifecycle lifecycle,
                                ShutdownService shutdownService) {

    try {
      connectionPool = new HikariConnectionPool(
          d2rConfig.getJdbc(),
          d2rConfig.getJdbcDriver(),
          d2rConfig.getDatabaseUsername(),
          d2rConfig.getDatabasePassword(),
          d2rConfig.getPoolSize(),
          d2rConfig.getDataSourceProperties());
    } catch (Exception e) {
      log.error("Couldn't initialize connection pool", e);
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }

    lifecycle.addStopHook(() -> {
      log.info("Shutdown Hook is executing...");
      try{
        connectionPool.close();
      } catch (IOException e) {
        log.error("Error while closing connection pool", e);
      }
      return CompletableFuture.completedFuture(null);
    });
  }

  @Override
  public ConnectionPool get() {
    return connectionPool;
  }
}