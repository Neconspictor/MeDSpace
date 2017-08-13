package de.unipassau.medspace.common.SQL;

import javax.sql.DataSource;
import java.io.Closeable;

/**
 * A connection pool manages a set of open connections to a database.
 * It manages also the connections. If the connection to the database is lost, it tries
 * to reconnect automatically.
 */
public interface ConnectionPool extends Closeable {

  /**
   * Provides the number of connections that are active at the moment of calling this method.
   * @return The number of active connections.
   */
  int getActiveConnectionsNumber();

  /**
   * Returns a factory for retrieving a connection to the database.
   * @return A factory for retrieving a connection.
   */
  DataSource getDataSource();

  /**
   * Provides the number of connections that are open to be used but are not active.
   * @return The number of idel connections.
   */
  int getIdleConnectionsNumber();

  /**
   * Provides the masimum size of connections that can be managed by this connection pool.
   * @return
   */
  int getMaxPoolSize();

  /**
   * Provides the number of connections that are currently managed by the connection pool.
   * Normally, it is the sum of idle and active connections.
   * @return The number of connections currently part of the connection pool.
   */
  int getTotalConnectionsNumber();
}