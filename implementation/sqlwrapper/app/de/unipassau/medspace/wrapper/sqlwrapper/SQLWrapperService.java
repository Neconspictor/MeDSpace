package de.unipassau.medspace.wrapper.sqlwrapper;

import com.typesafe.config.ConfigException;
import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.SQL.HikariConnectionPool;
import de.unipassau.medspace.common.exception.NotValidArgumentException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
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
   * The D2R configuration.
   */
  private Configuration config;

  /**
   * The D2R wrapper.
   */
  private D2rWrapper<Document> wrapper;

  /**
   * Read meta data from the datasource.
   */
  private DatabaseMetaData metaData;


  /**
   * Creates a new SQLWrapperService.
   * @param lifecycle Used to add shutdown hooks to the play framework.
   * @param playConfig Used to read play specific configurations.
   */
  @Inject
  public SQLWrapperService(ApplicationLifecycle lifecycle,
                           com.typesafe.config.Config playConfig) {
      try {
        String mappingConfigFile = playConfig.getString("MeDSpaceMappingConfig");
        startup(mappingConfigFile);
      }catch(ConfigException.Missing | ConfigException.WrongType e) {
        log.error("Couldn't read MeDSpace mapping config file: ", e);
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
  public Configuration getConfig() {
    return config;
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
   * @return A stream of rdf triples representing the result of the keyword search.
   * @throws IOException If an IO-Error occurs.
   * @throws NotValidArgumentException If 'keywords' is null.
   */
  public Stream<Triple> search(String keywords) throws IOException, NotValidArgumentException {

    if (keywords == null) {
      throw new NotValidArgumentException("keywords mustn't be null");
    }

    StringTokenizer tokenizer = new StringTokenizer(keywords, ",", false);
    List<String> keywordList = new ArrayList<>();

    while(tokenizer.hasMoreTokens()) {
      keywordList.add(tokenizer.nextToken());
    }

    KeywordSearcher<Triple> searcher = wrapper.createKeywordSearcher();
    return searcher.searchForKeywords(keywordList);
  }

  /**
   * Does startup the sql wrapper.
   * @param configFile The path to the MeDSpace D2r config file.
   * @throws D2RException If the configuration file doesn't exists or is erroneous
   * @throws IOException If an IO-Error occurs.
   * @throws SQLException If the connection to the datasource could'nt be established.
   */
  private void startup(String configFile) throws D2RException, IOException, SQLException {

    log.info("initializing SQL Wrapper...");
    try {
      config = new ConfigurationReader().readConfig(configFile);
    } catch (IOException e) {
      throw new D2RException("Error while reading the configuration file: " + configFile, e);
    }


    URI jdbcURI = null;
    try {
      jdbcURI = new URI(config.getJdbc());
    } catch (URISyntaxException e) {
      String errorMessage = "Couldn't get an URI from the jdbc uri specified in the config file" + "\n";
      errorMessage += "jdbc URI: " + jdbcURI + "\n";
      errorMessage += "config file: " + configFile + "\n";
      new D2RException(errorMessage, e);
    }

    log.info("Establish connection pool to: " + jdbcURI);

    connectionPool = new HikariConnectionPool(
        jdbcURI,
        config.getJdbcDriver(),
        config.getDatabaseUsername(),
        config.getDatabasePassword(),
        config.getMaxConnections(),
        config.getDataSourceProperties());

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

    Path indexPath = config.getIndexDirectory();

    wrapper = new D2rWrapper<Document>(connectionPool, config.getMaps(), config.getNamespaces());

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
}