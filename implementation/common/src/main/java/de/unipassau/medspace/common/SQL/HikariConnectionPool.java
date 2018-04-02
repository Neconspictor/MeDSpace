package de.unipassau.medspace.common.SQL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import de.unipassau.medspace.common.util.FileUtil;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link ConnectionPool} implementation using the HikariCP library.
 */
public class HikariConnectionPool implements ConnectionPool {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(HikariConnectionPool.class);

  /**
   * The HikariCP DataSourceProperty implementation. Used to get a DataSourceProperty.
   */
  private HikariDataSource dataSource;

  /**
   * Properties that are send to the database.
   */
  private List<Pair<String, String>> datasourceProperties;

  /**
   * The jdb uri to the database.
   */
  private URI jdbcURI;

  /**
   * The jdbc driver
   */
  private Class driverClass;

  /**
   * The password used for authentication.
   */
  private String password;

  /**
   * The maximum number of connections to use.
   */
  private int poolSize;

  /**
   * The user name used for authentication.
   */
  private String userName;


  /**
   * Creates a new HikariConnectionPool.
   * @param jdbcURI The jdb uri to the database.
   * @param driverClass The jdb driver to use.
   * @param userName The user name necessary for authentication.
   * @param password The password necessary for authentication.
   * @param poolSize The wished maximum connection pool size.
   * @param datasourceProperties Properties that should be send to the database.
   * @throws IOException if the connection pool couldn't be created.
   */
  public HikariConnectionPool(URI jdbcURI,
                              Class driverClass,
                              String userName,
                              String password,
                              int poolSize,
                              List<Pair<String, String>> datasourceProperties)
      throws IOException {

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


  @Override
  public int getActiveConnectionsNumber() {
    return dataSource.getHikariPoolMXBean().getActiveConnections();
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  @Override
  public int getIdleConnectionsNumber() {
    return dataSource.getHikariPoolMXBean().getIdleConnections();
  }

  @Override
  public int getMaxPoolSize() {
    return dataSource.getHikariConfigMXBean().getMaximumPoolSize();
  }

  @Override
  public int getTotalConnectionsNumber() {
    return dataSource.getHikariPoolMXBean().getTotalConnections();
  }

  /**
   * Inits this connection pool.
   * @throws IOException If the connection pool couldn't be initialized.
   */
  private void init() throws  IOException {
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

    try {
      dataSource =  new HikariDataSource(hikariConfig);
    } catch(HikariPool.PoolInitializationException e) {
      log.error("Couldn't create HikariDataSource; reason: ", e);
      throw new IOException("Couldn't establish SQL connection to " + jdbcURI);
    }
  }

  @Override
  public void close() throws IOException {
    try {
      dataSource.unwrap(HikariDataSource.class).close();
    } catch (SQLException e) {
      e.printStackTrace();
      log.error("Couldn't close datasource: ", e);
      FileUtil.closeSilently(dataSource);
    }
  }
}