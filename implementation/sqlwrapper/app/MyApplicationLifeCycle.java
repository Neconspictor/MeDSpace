
import de.unipassau.medspace.TestProcessor;
import de.unipassau.medspace.common.SQL.DataSourceManager;
import de.unipassau.medspace.common.SQL.HikariDataSourceManager;
import de.unipassau.medspace.common.indexing.DataSourceIndex;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.d2r.D2rProxy;
import de.unipassau.medspace.d2r.D2rWrapper;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.d2r.config.ConfigurationReader;
import de.unipassau.medspace.d2r.exception.D2RException;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.shared.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.inject.ApplicationLifecycle;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by David Goeth on 24.07.2017.
 */
@Singleton
public class MyApplicationLifeCycle {

  private static Logger log = LoggerFactory.getLogger(MyApplicationLifeCycle.class);
  private final static String D2RMap = "./examples/medspace/medspace.d2r.xml";

  @Inject
  public MyApplicationLifeCycle(ApplicationLifecycle lifecycle) {
      log.warn("Bootstrapping...");
      lifecycle.addStopHook(() -> {
        log.warn("gracefulShutdown hook hello world!");
        return  	CompletableFuture.completedFuture(null);
      });

      try {
        startup();
      } catch(Throwable e) {
        // Catching Throwable is regarded to be a bad habit, but as we catch the Throwable only
        // for allowing the application to shutdown gracefully, it is ok to do so.
        log.error("Error on startup", e);
        gracefulShutdown(lifecycle, -1);
      }

    //lifecycle.stop();
    //System.exit(-1);
  }

  private void startup() throws D2RException, IOException, URISyntaxException {
    String prettyPrintingLang = "N3";
    DataSourceManager dataSourceManager = null;
    D2rWrapper wrapper = null;

    try {
      log.info("D2R test started ....");
      Configuration config = new ConfigurationReader().readConfig(D2RMap);
      URI jdbcURI = new URI(config.getJdbc());

      dataSourceManager = new HikariDataSourceManager(
          jdbcURI,
          config.getJdbcDriver(),
          config.getDatabaseUsername(),
          config.getDatabasePassword(),
          config.getMaxConnections(),
          config.getDataSourceProperties());

      D2rProxy proxy = new D2rProxy(config, dataSourceManager);
      log.info("D2R proxy created ....");
      log.info("D2R file read ....");

      Lang lang = Lang.TURTLE;

      if (config.isIndexUsed()) {
        //index = new SqlIndex(config.getIndexDirectory(), proxy);
      }

      wrapper = new D2rWrapper(proxy, config.getIndexDirectory());

      DataSourceIndex index = wrapper.getIndex();

      boolean exists = index.exists();

      if (!exists) {
        log.info("Indexing data...");
        index.reindex();
        log.info("Indexing done.");
      }

      KeywordSearcher<Triple> searcher = wrapper.createKeywordSearcher();

      Instant startTime = Instant.now();
      DataSourceStream<Triple> triples = searcher.searchForKeywords(Arrays.asList("male"));

      boolean hasTriples = triples.hasNext();

      if (hasTriples) {
        RDFFormat format = StreamRDFWriter.defaultSerialization(lang);
        StreamRDF rdfOut = StreamRDFWriter.getWriterStream(System.out, format);
        PrefixMapping mapping = proxy.getNamespacePrefixMapper();
        for (Map.Entry<String, String> map : mapping.getNsPrefixMap().entrySet()) {
          rdfOut.prefix(map.getKey(), map.getValue());
        }
        rdfOut.start();
        for (Triple triple : triples) {
          rdfOut.triple(triple);
        }
        rdfOut.finish();
      } else {
        log.info("No results found!");
      }

      triples.close();


      Instant endTime = Instant.now();
      //Lang lang = config.getOutputFormat();
      Lang prettyLang = RDFLanguages.shortnameToLang(prettyPrintingLang);

      log.info("RDF data exported ....");
      log.info("Time elapsed: " + Duration.between(startTime, endTime));
    } finally {
      FileUtil.closeSilently(wrapper);
      FileUtil.closeSilently(dataSourceManager);
      log.info("streams successfully closed");
    }
  }

  private void gracefulShutdown(ApplicationLifecycle lifecycle, int errorCode) {

    lifecycle.stop();

    // Stopping the application lifecycle is enough to trigger a graceful shutdown of the
    // play framework. But the play framework prints an error message that the server
    // couldn't be started as the server is during a shutdown process.
    // This side effect is undesired as this function is intended
    // to do a graceful shutdown and thus shouldn't produce any error messages.
    // Thus, a call of System.exit is here justified.
    System.exit(errorCode);
  }
}