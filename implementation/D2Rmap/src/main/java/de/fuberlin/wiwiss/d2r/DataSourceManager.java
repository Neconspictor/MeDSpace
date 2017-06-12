package de.fuberlin.wiwiss.d2r;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.javatuples.Pair;

import javax.sql.DataSource;
import java.util.Vector;

/**
 * Created by David Goeth on 12.06.2017.
 */
public class DataSourceManager {
  private DataSource dataSource;

  public DataSourceManager(Configuration config) {
    dataSource = null;
    init(config);
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  private void init(Configuration config) {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(config.getJdbc());
    hikariConfig.setUsername(config.getDatabaseUsername());
    hikariConfig.setPassword(config.getDatabasePassword());

    hikariConfig.setMaximumPoolSize(config.getMaxConnections());
    hikariConfig.setAutoCommit(true);

    Vector<Pair<String, String>> properties = config.getDataSourceProperties();
    for (Pair<String, String> property : properties) {
      hikariConfig.addDataSourceProperty(property.getValue0(), property.getValue1());
    }
    dataSource =  new HikariDataSource(hikariConfig);
  }
}