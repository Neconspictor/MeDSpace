package de.unipassau.medspace.common.SQL;

import javax.sql.DataSource;
import java.io.Closeable;

/**
 * TODO
 */
public interface DataSourceManager extends Closeable {

  /**
   * TODO
   * @return
   */
  DataSource getDataSource();
}
