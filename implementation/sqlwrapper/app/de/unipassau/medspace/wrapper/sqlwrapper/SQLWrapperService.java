package de.unipassau.medspace.wrapper.sqlwrapper;

import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.SQL.HikariConnectionPool;
import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.config.GeneralWrapperConfigReader;
import de.unipassau.medspace.common.exception.NotValidArgumentException;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.TripleIndexFactory;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.d2r.D2rWrapper;
import de.unipassau.medspace.d2r.MappedSqlTuple;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.d2r.config.ConfigurationReader;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.d2r.lucene.LuceneIndexFactory;
import org.apache.jena.graph.Triple;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.inject.ApplicationLifecycle;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
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
   * D2R specific configuration.
   */
  private Configuration d2rConfig;

  /**
   * The D2R wrapper.
   */
  private D2rWrapper<Document> wrapper;

  /**
   * Read meta data from the datasource.
   */
  private DatabaseMetaData metaData;

  private RegisterClient registerClient;


  /**
   * Creates a new SQLWrapperService.
   * @param lifecycle Used to add shutdown hooks to the play framework.
   * @param playConfig Used to read play specific configurations.
   */
  @Inject
  public SQLWrapperService(ApplicationLifecycle lifecycle,
                           com.typesafe.config.Config playConfig,
                           RegisterClient registerClient) {

    this.registerClient = registerClient;
    log.info("http.address= " + playConfig.getString("play.server.http.address"));
    log.info("http.port= " + playConfig.getString("play.server.http.port"));
      try {
        String wrapperConfigFile = playConfig.getString("MeDSpaceWrapperConfig");
        String d2rConfigFile = playConfig.getString("MeDSpaceD2rConfig");
        startup(wrapperConfigFile, d2rConfigFile);
      }catch(ConfigException.Missing | ConfigException.WrongType e) {
        log.error("Couldn't read MeDSpace mapping d2rConfig file: ", e);
        log.info("Graceful shutdown is initiated...");
        gracefulShutdown(lifecycle, -1);
      } catch(Throwable e) {
        // Catching Throwable is regarded to be a bad habit, but as we catch the Throwable only
        // for allowing the application to shutdown gracefully, it is ok to do so.
        log.error("Failed to initialize SQL Wrapper", e);
        log.info("Graceful shutdown is initiated...");
        gracefulShutdown(lifecycle, -1);
      }

      lifecycle.addStopHook(() -> {
        log.info("Shutdown is executing...");
        deregister();
        wrapper.close();
        connectionPool.close();
        return CompletableFuture.completedFuture(null);
      });

    //lifecycle.stop();
    //System.exit(-1);
  }

  /**
   * Provides the D2R configuration.
   * @return The D2R configuration.
   */
  public Configuration getD2rConfig() {
    return d2rConfig;
  }


  /**
   * Provides the D2R wrapper.
   * @return The D2R wrapper.
   */
  public D2rWrapper getWrapper() {
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
   * @throws NotValidArgumentException If 'keywords' is null.
   */
  public Stream<Triple> search(String keywords) throws IOException, NotValidArgumentException {

    if (keywords == null) {
      throw new NotValidArgumentException("keywords mustn't be null");
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
   * @param wrapperConfigFile The path to the general wrapper config file.
   * @param d2rConfigFile The path to the MeDSpace D2r config file.
   * @throws D2RException If the configuration file doesn't exists or is erroneous
   * @throws IOException If an IO-Error occurs.
   * @throws SQLException If the connection to the datasource could'nt be established.
   */
  private void startup(String wrapperConfigFile, String d2rConfigFile) throws D2RException, IOException, SQLException {

    log.info("initializing SQL Wrapper...");
    log.info("Reading general wrapper configuration...");
    try {
      generalConfig = new GeneralWrapperConfigReader().readConfig(wrapperConfigFile);
    } catch (IOException e) {
      throw new IOException("Error while reading the general wrapper configuration file: " + wrapperConfigFile, e);
    }

    if (!generalConfig.isUseIndex()) {
      throw new D2RException("This wrapper needs an index, but no index directory is stated in the general wrapper configuration.");
    }

    log.info("Reading general wrapper configuration done: ");
    log.info(generalConfig.toString());
    log.info("Reading MeDSpace D2RMap configuration...");

    try {
      d2rConfig = new ConfigurationReader().readConfig(d2rConfigFile);
    } catch (IOException e) {
      throw new D2RException("Error while reading the configuration file: " + d2rConfigFile, e);
    }

    log.info("Reading MeDSpace D2RMap configuration done.");


    URI jdbcURI = null;
    try {
      jdbcURI = new URI(d2rConfig.getJdbc());
    } catch (URISyntaxException e) {
      String errorMessage = "Couldn't get an URI from the jdbc uri specified in the d2rConfig file" + "\n";
      errorMessage += "jdbc URI: " + jdbcURI + "\n";
      errorMessage += "d2rConfig file: " + d2rConfigFile + "\n";
      new D2RException(errorMessage, e);
    }

    log.info("Establish connection pool to: " + jdbcURI);

    connectionPool = new HikariConnectionPool(
        jdbcURI,
        d2rConfig.getJdbcDriver(),
        d2rConfig.getDatabaseUsername(),
        d2rConfig.getDatabasePassword(),
        d2rConfig.getMaxConnections(),
        d2rConfig.getDataSourceProperties());

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
    boolean registered = registerClient.register(generalConfig.getDatasource(), generalConfig.getRegisterURL());
    if (registered) {
      log.info("Successfuly registered to the Register.");
    } else {
      throw new D2RException("Couldn't register to the Register!");
    }

    Path indexPath = generalConfig.getIndexDirectory();

    Map<String, Namespace> namespaces = new HashMap<>(generalConfig.getNamespaces());
    namespaces.putAll(d2rConfig.getNamespaces());

    wrapper = new D2rWrapper<Document>(connectionPool, d2rConfig.getMaps(), namespaces);

    TripleIndexFactory<Document, MappedSqlTuple> indexFactory =
        new LuceneIndexFactory(wrapper, indexPath.toString());

    wrapper.init(indexPath, indexFactory);

    boolean shouldReindex = !wrapper.existsIndex() && wrapper.isIndexUsed();

    if (shouldReindex) {
      log.info("Indexing data...");
      wrapper.reindexData();
      log.info("Indexing done.");
    }


    log.info("Initialized SQL Wrapper");
  }

  /**
   * Does a graceful shutdown.
   * @param lifecycle The application lifecycle of the play framework.
   * @param errorCode An error code passed to the operating system.
   */
  private void gracefulShutdown(ApplicationLifecycle lifecycle, int errorCode) {

    deregister();
    lifecycle.stop();

    // Stopping the application lifecycle is enough to trigger a graceful shutdown of the
    // play framework. But the play framework rises a runtime exception that the server
    // couldn't be started as the server is during a shutdown process.
    // This side effect is undesired as this function is intended
    // to do a graceful shutdown and thus shouldn't produce any misleading error messages.
    // Thus, a call of System.exit is here justified.
    System.exit(errorCode);
  }

  /**
   * Provides the used connection pool.
   * @return The used connection pool.
   */
  public ConnectionPool getConnectionPool() {
    return connectionPool;
  }

  /**
   * Provides meta data read from the datasource.
   * @return The meta data from the datasource.
   */
  public DatabaseMetaData getMetaData() {
    return metaData;
  }

  /**
   * The general wrapper configuration.
   */
  public GeneralWrapperConfig getGeneralConfig() {
    return generalConfig;
  }

  private void deregister() {
    boolean success = registerClient.deRegister(generalConfig.getDatasource(), generalConfig.getRegisterURL());

    if (success)
      log.info("Successfully deregistered from the register.");
    else
      log.error("Couldn't deregister from the register.");
  }
}