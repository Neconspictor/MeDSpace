package de.unipassau.medspace.wrapper.sqlwrapper;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.zaxxer.hikari.pool.HikariPool;
import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.SQL.HikariConnectionPool;
import de.unipassau.medspace.d2r.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.ApplicationLifecycle;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * TODO
 */
public class ConnectionPoolProvider implements Provider<ConnectionPool> {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(ConnectionPoolProvider.class);

  /**
   * TODO
   */
  private ConnectionPool connectionPool;

  /**
   * TODO
   * @param provider
   * @param lifecycle
   * @param shutdownService
   */
  @Inject
  public ConnectionPoolProvider(ConfigProvider provider, ApplicationLifecycle lifecycle,
                                ShutdownService shutdownService) {

    Configuration config = provider.getD2rConfig();

    try {
      connectionPool = new HikariConnectionPool(
          config.getJdbc(),
          config.getJdbcDriver(),
          config.getDatabaseUsername(),
          config.getDatabasePassword(),
          config.getPoolSize(),
          config.getDataSourceProperties());
    } catch (HikariPool.PoolInitializationException e) {
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