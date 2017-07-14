package de.unipassau.medspace.common.SQL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import de.unipassau.medspace.common.SQL.DataSourceManager;
import org.javatuples.Pair;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by David Goeth on 12.06.2017.
 */
public class HikariDataSourceManager implements DataSourceManager {
  private HikariDataSource dataSource;
  private List<Pair<String, String>> datasourceProperties;
  private URI jdbcURI;
  private Class driverClass;
  private String password;
  private int poolSize;
  private String userName;

  public HikariDataSourceManager(URI jdbcURI, Class driverClass, String userName, String password, int poolSize, List<Pair<String, String>> datasourceProperties) {
    assert poolSize > 0;

    dataSource = null;
    this.datasourceProperties = new LinkedList<>();
    this.driverClass = driverClass;
    this.jdbcURI = jdbcURI;
    this.password = password;
    this.poolSize = poolSize;
    this.userName = userName;

    if (datasourceProperties != null) {
      for (Pair<String, String> pair : datasourceProperties) {
        this.datasourceProperties.add(new Pair<>(pair.getValue0(), pair.getValue1()));
      }
    }

    init();
  }


  public DataSource getDataSource() {
    return dataSource;
  }

  private void init() {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(jdbcURI.toString());
    hikariConfig.setDriverClassName(driverClass.getName());
    hikariConfig.setUsername(userName);
    hikariConfig.setPassword(password);

    hikariConfig.setMaximumPoolSize(poolSize);
    hikariConfig.setAutoCommit(true);

    List<Pair<String, String>> properties = datasourceProperties;
    for (Pair<String, String> property : properties) {
      hikariConfig.addDataSourceProperty(property.getValue0(), property.getValue1());
    }
    dataSource =  new HikariDataSource(hikariConfig);

  }

  @Override
  public void close() throws IOException {
    //dataSource.close();
    try {
      dataSource.unwrap(HikariDataSource.class).close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}