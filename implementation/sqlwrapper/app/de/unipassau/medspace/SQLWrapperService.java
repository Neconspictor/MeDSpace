package de.unipassau.medspace;

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
 * Created by David Goeth on 24.07.2017.
 */
@Singleton
public class SQLWrapperService {

  private static Logger log = LoggerFactory.getLogger(SQLWrapperService.class);
  private ConnectionPool connectionPool;
  private Configuration config;
  private D2rWrapper<Document> wrapper;
  private DatabaseMetaData metaData;
  private  com.typesafe.config.Config playConfig;

  @Inject
  public SQLWrapperService(ApplicationLifecycle lifecycle,
                           com.typesafe.config.Config playConfig) {

      this.playConfig = playConfig;

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

  public Configuration getConfig() {
    return config;
  }


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
      throw new D2RException("Couldn't establish connection to the datasource", e);
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

  public ConnectionPool getConnectionPool() {
    return connectionPool;
  }

  public DatabaseMetaData getMetaData() {
    return metaData;
  }

}