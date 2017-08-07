package de.unipassau.medspace.common.SQL;

import javax.sql.DataSource;
import java.io.Closeable;

/**
 * TODO
 */
public interface ConnectionPool extends Closeable {

  /**
   * TODO
   * @return
   */
  int getActiveConnectionsNumber();

  /**
   * TODO
   * @return TODO
   */
  DataSource getDataSource();

  /**
   * TODO
   * @return
   */
  int getIdleConnectionsNumber();

  /**
   * TODO
   * @return
   */
  int getMaxPoolSize();

  /**
   * TODO
   * @return
   */
  int getTotalConnectionsNumber();
}
