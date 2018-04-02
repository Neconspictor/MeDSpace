package de.unipassau.medspace.wrapper.sqlwrapper;

import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.config.ServerConfig;
import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.play.ShutdownService;
import de.unipassau.medspace.common.play.WrapperService;
import de.unipassau.medspace.common.play.wrapper.RegisterClient;
import de.unipassau.medspace.common.register.Datasource;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.d2r.D2rWrapper;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.d2r.exception.D2RException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.inject.ApplicationLifecycle;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

/**
 * Defines the Model of the SQL wrapper.
 */
@Singleton
public class SQLWrapperService extends WrapperService {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(SQLWrapperService.class);

  /**
   * Used to establish connection to the datasource.
   */
  private ConnectionPool connectionPool;

  /**
   * Read meta data from the datasource.
   */
  private DatabaseMetaData metaData;

  /**
   * The used register client.
   */
  private RegisterClient registerClient;

  /**
   * Specifies if this wrapper should connect to the register.
   */
  private boolean connectToRegister;

  /**
   * The datasource wrapped by this wrapper.
   */
  private Datasource wrapperDatasource;


  /**
   *  Creates a new SQLWrapperService.
   * @param lifecycle Used to add shutdown hooks to the play framework.
   * @param registerClient Used for communication with the register.
   * @param d2rConfig the D2R mapping configuration
   * @param connectionPool A connection pool to the relational database.
   * @param serverConfig The server configuration
   * @param shutdownService The shutdown service.
   * @param d2rWrapper The SQL wrapper.
   */
  @Inject
  public SQLWrapperService(ApplicationLifecycle lifecycle,
                           RegisterClient registerClient,
                           Configuration d2rConfig,
                           ConnectionPool connectionPool,
                           GeneralWrapperConfig generalWrapperConfig,
                           ServerConfig serverConfig,
                           ShutdownService shutdownService,
                           D2rWrapper<?> d2rWrapper) {
    super(generalWrapperConfig, d2rWrapper);

    this.registerClient = registerClient;
    this.connectionPool = connectionPool;

    this.connectToRegister = generalConfig.getConnectToRegister();

      try {
        startup(d2rConfig, serverConfig);
      }catch(Exception e) {
        log.error("Couldn't read MeDSpace mapping d2rConfig file: ", e);
        log.info("Graceful shutdown is initiated...");
        shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
      } catch(Throwable e) {

        // Catching Throwable is regarded to be a bad habit, but as we catch the Throwable only
        // for allowing the application to shutdown gracefully, it is ok to do so.
        log.error("Failed to initialize SQL Wrapper", e);
        log.info("Graceful shutdown is initiated...");
        shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
      }

      lifecycle.addStopHook(() -> {
        log.info("Shutdown is executing...");
        if (connectToRegister) deregister();
        wrapper.close();
        return CompletableFuture.completedFuture(null);
      });
  }


  /**
   * Checks the connection status to the datasource
   * @return true if the wrapper could establish a connection to the datasource.
   */
  public boolean isConnected() {
    Connection conn = null;
    try {
      conn = connectionPool.getDataSource().getConnection();
    } catch (SQLException e) {
      return false;
    } finally {
      log.debug("Closing Connection...");
      FileUtil.closeSilently(conn);
    }
    return true;
  }

  /**
   * Does startup the sql wrapper.
   * @param d2rConfig the d2r mapping configuration
   * @param serverConfig The server configuration.
   * @throws D2RException If the configuration file doesn't exists or is erroneous
   * @throws IOException If an IO-Error occurs.
   * @throws SQLException If the connection to the datasource could'nt be established.
   */
  private void startup(Configuration d2rConfig, ServerConfig serverConfig) throws
      D2RException,
      IOException,
      SQLException {

    log.info("initializing SQL Wrapper...");

    if (!generalConfig.isIndexUsed()) {
      throw new D2RException("This wrapper needs an index, but no index directory is stated "
          + "in the general wrapper configuration.");
    }

    URI jdbcURI = d2rConfig.getJdbc();

    Datasource.Builder builder = new Datasource.Builder();
    builder.setDescription(generalConfig.getDescription());
    builder.setRdfFormat(generalConfig.getOutputFormat());
    builder.setServices(generalConfig.getServices());
    builder.setUrl(serverConfig.getServerURL());

    try{
      wrapperDatasource = builder.build();
    }catch (NoValidArgumentException e) {
      throw new D2RException("Couldn't create datasource object for sql wrapper", e);
    }

    log.info("Establish connection pool to: " + jdbcURI);

    // check if the datasource manager can connect to the datasource.
    Connection conn = null;
    try {
      conn = connectionPool.getDataSource().getConnection();
      metaData = conn.getMetaData();
    } catch (SQLException e) {
      throw new SQLException("Couldn't establish connection to the datasource", e);
    } finally {
      log.debug("Closing Connection...");
      FileUtil.closeSilently(conn);
    }

    //check connection to the register
    if (connectToRegister) {
      boolean registered = registerClient.register(wrapperDatasource, generalConfig.getRegisterURL());
      if (registered) {
        log.info("Successfuly registered to the Register.");
      } else {
        throw new D2RException("Couldn't register to the Register!");
      }
    }

    log.info("Initialized SQL Wrapper");
  }

  /**
   * Provides meta data read from the datasource.
   * @return The meta data from the datasource.
   */
  public DatabaseMetaData getMetaData() {
    return metaData;
  }

  private void deregister() {
    boolean success = registerClient.deRegister(wrapperDatasource, generalConfig.getRegisterURL());

    if (success)
      log.info("Successfully deregistered from the register.");
    else
      log.error("Couldn't deregister from the register.");
  }
}