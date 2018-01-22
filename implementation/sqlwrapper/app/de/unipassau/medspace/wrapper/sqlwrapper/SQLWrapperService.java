package de.unipassau.medspace.wrapper.sqlwrapper;

import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.Stream;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Defines the Model of the SQL wrapper.
 */
@Singleton
public class SQLWrapperService {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(SQLWrapperService.class);

  /**
   * Used to establish connection to the datasource.
   */
  private ConnectionPool connectionPool;

  /**
   * The general wrapper configuration.
   */
  private GeneralWrapperConfig generalConfig;

  /**
   * The D2R wrapper.
   */
  private D2rWrapper<?> wrapper;

  /**
   * Read meta data from the datasource.
   */
  private DatabaseMetaData metaData;

  /**
   * TODO
   */
  private RegisterClient registerClient;

  /**
   * TODO
   */
  private boolean connectToRegister;


  /**
   * Creates a new SQLWrapperService.
   * @param lifecycle Used to add shutdown hooks to the play framework.
   * @param registerClient Used for communication with the register.
   * @param provider Used to read configurations.
   */
  @Inject
  public SQLWrapperService(ApplicationLifecycle lifecycle,
                           RegisterClient registerClient,
                           ConfigProvider provider,
                           ConnectionPool connectionPool,
                           ShutdownService shutdownService,
                           D2rWrapper<?> d2rWrapper) {

    this.registerClient = registerClient;
    this.connectionPool = connectionPool;
    this.wrapper = d2rWrapper;
    this.generalConfig = provider.getGeneralWrapperConfig();

    this.connectToRegister = generalConfig.getConnectToRegister();

      try {
        startup(provider);
      }catch(ConfigException.Missing | ConfigException.WrongType e) {
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
   * Provides the D2R wrapper.
   * @return The D2R wrapper.
   */
  public D2rWrapper<?> getWrapper() {
    return wrapper;
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
   * Performs a keyword search on the underlying datasource or on the index if one is used.
   * @param keywords The keywords to search for.
   * @return A stream of rdf triples representing the success of the keyword search.
   * @throws IOException If an IO-Error occurs.
   * @throws NoValidArgumentException If 'keywords' is null.
   */
  public Stream<Triple> search(String keywords) throws IOException, NoValidArgumentException {

    if (keywords == null) {
      throw new NoValidArgumentException("keywords mustn't be null");
    }

    StringTokenizer tokenizer = new StringTokenizer(keywords, ", ", false);
    List<String> keywordList = new ArrayList<>();

    while(tokenizer.hasMoreTokens()) {
      keywordList.add(tokenizer.nextToken());
    }

    KeywordSearcher<Triple> searcher = wrapper.createKeywordSearcher();
    return searcher.searchForKeywords(keywordList);
  }

  /**
   * Does startup the sql wrapper.
   * @param provider A provider used to access the wrapper and server configurations
   * @throws D2RException If the configuration file doesn't exists or is erroneous
   * @throws IOException If an IO-Error occurs.
   * @throws SQLException If the connection to the datasource could'nt be established.
   */
  private void startup(ConfigProvider provider) throws D2RException, IOException, SQLException {

    log.info("initializing SQL Wrapper...");

    if (!generalConfig.isIndexUsed()) {
      throw new D2RException("This wrapper needs an index, but no index directory is stated in the general wrapper configuration.");
    }

    Configuration d2rConfig = provider.getD2rConfig();

    URI jdbcURI = d2rConfig.getJdbc();

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
      boolean registered = registerClient.register(generalConfig.getDatasource(), generalConfig.getRegisterURL());
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

  /**
   * TODO
   */
  private void deregister() {
    boolean success = registerClient.deRegister(generalConfig.getDatasource(), generalConfig.getRegisterURL());

    if (success)
      log.info("Successfully deregistered from the register.");
    else
      log.error("Couldn't deregister from the register.");
  }
}