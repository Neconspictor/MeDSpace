package de.unipassau.medspace;

import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.SQL.HikariConnectionPool;
import de.unipassau.medspace.common.rdf.*;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.rdf.rdf4j.RDF4JTripleWriterFactory;
import de.unipassau.medspace.common.rdf.rdf4j.RDF4J_RDFProvider;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.stream.TripleInputStream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.d2r.D2rWrapper;
import de.unipassau.medspace.d2r.MappedSqlTuple;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.d2r.config.ConfigurationReader;
import de.unipassau.medspace.d2r.exception.D2RException;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;


import de.unipassau.medspace.d2r.lucene.LuceneIndexFactory;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestProcessor {
  private final static String D2RMap = "./medspace/medspace-d2r-mapping.xml";
  private final static Logger log = LoggerFactory.getLogger(TestProcessor.class);

  public static void main(String[] args) throws IOException {
    ConnectionPool connectionPool = null;
    D2rWrapper<Document> wrapper = null;

    RDFProvider provider = new RDF4J_RDFProvider();
    TripleWriterFactory tripleWriterFactory = provider.getWriterFactory();

    try {
      log.info("D2R test started ....");

      Configuration config = new ConfigurationReader(provider).readConfig(D2RMap);
      URI jdbcURI = new URI(config.getJdbc());

      connectionPool = new HikariConnectionPool(
          jdbcURI,
          config.getJdbcDriver(),
          config.getDatabaseUsername(),
          config.getDatabasePassword(),
          config.getMaxConnections(),
          config.getDataSourceProperties());

      log.info("D2R proxy created ....");
      log.info("D2R file read ....");

      String format = "Turtle";



      wrapper = new D2rWrapper<>(connectionPool, config.getMaps(), config.getNamespaces());



      TripleIndexFactory<Document, MappedSqlTuple> indexFactory = null;

      if (config.isIndexUsed()) {
        indexFactory =  new LuceneIndexFactory(wrapper, config.getIndexDirectory().toString());
      }

      wrapper.init(config.getIndexDirectory(), indexFactory);

      boolean shouldReindex = !wrapper.existsIndex() && wrapper.isIndexUsed();

      if (shouldReindex) {
        log.info("Indexing data...");
        wrapper.reindexData();
        log.info("Indexing done.");
      }

      KeywordSearcher<Triple> searcher = wrapper.createKeywordSearcher();

      Instant startTime = Instant.now();

      Stream<Triple> triples = searcher.searchForKeywords(Arrays.asList("male", "female"));
      Set<Namespace> namespaces = wrapper.getNamespaces();

      boolean hasTriples = triples.hasNext();

      if (hasTriples) {
        InputStream tripleStream = new TripleInputStream(triples,
            config.getOutputFormat(),
            namespaces,
            tripleWriterFactory);

        BufferedReader reader = new BufferedReader(new InputStreamReader(tripleStream, StandardCharsets.UTF_8));
        String value = reader.readLine();
        while (value != null) {
          System.out.println(value);
          value = reader.readLine();
        }

      } else {
        log.info("No results found!");
      }

      triples.close();


      Instant endTime = Instant.now();

      log.info("RDF data exported ....");
      log.info("Time elapsed: " + Duration.between(startTime, endTime));
    } catch (D2RException d2rex) {
      log.error("D2RException caught:  " + d2rex.getMessage());
      log.debug("Error stacktrace", d2rex);
    } catch (IOException ioex) {
      log.error("IOException caught:  ", ioex);
      log.debug("Error stacktrace", ioex);
    } catch (java.lang.Throwable ex) {
      log.error("General exception caught:  ", ex);
      log.debug("Error stacktrace", ex);
    } finally {
      FileUtil.closeSilently(wrapper);
      FileUtil.closeSilently(connectionPool);
      log.info("streams successfully closed");
    }
  }
}