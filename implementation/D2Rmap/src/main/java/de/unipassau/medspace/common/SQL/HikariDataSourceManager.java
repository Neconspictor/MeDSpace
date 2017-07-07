package de.unipassau.medspace.common.SQL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.unipassau.medspace.common.SQL.DataSourceManager;
import org.javatuples.Pair;

import javax.sql.DataSource;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by David Goeth on 12.06.2017.
 */
public class HikariDataSourceManager implements DataSourceManager {
  private DataSource dataSource;
  private List<Pair<String, String>> datasourceProperties;
  private URI jdbcURI;
  private String password;
  private int poolSize;
  private String userName;

  public HikariDataSourceManager(URI jdbcURI, String userName, String password, int poolSize, List<Pair<String, String>> datasourceProperties) {
    assert poolSize > 0;

    dataSource = null;
    this.poolSize = poolSize;
    this.jdbcURI = jdbcURI;
    this.userName = userName;
    this.password = password;
    this.datasourceProperties = new LinkedList<>();

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
}