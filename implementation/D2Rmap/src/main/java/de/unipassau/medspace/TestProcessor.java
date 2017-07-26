package de.unipassau.medspace;

import de.unipassau.medspace.common.SQL.DataSourceManager;
import de.unipassau.medspace.common.indexing.DataSourceIndex;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.common.util.TempFile;
import de.unipassau.medspace.d2r.D2rProxy;
import de.unipassau.medspace.d2r.D2rWrapper;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.d2r.config.ConfigurationReader;
import de.unipassau.medspace.common.SQL.HikariDataSourceManager;
import de.unipassau.medspace.d2r.exception.D2RException;

import java.io.*;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;


import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.shared.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestProcessor {
  private final static String D2RMap = "./examples/medspace/medspace.d2r.xml";
  private final static Logger log = LoggerFactory.getLogger(TestProcessor.class);

  public static void main(String[] args) throws IOException {
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
    } catch (D2RException d2rex) {
      log.error("D2RException caught:  " + d2rex.getMessage());
      log.debug("Error stacktrace", d2rex);
    } catch (IOException ioex) {
      log.error("IOException caught:  " + ioex.getMessage());
      log.debug("Error stacktrace", ioex);
    } catch (java.lang.Throwable ex) {
      log.error("General exception caught:  " + ex.getMessage());
      log.debug("Error stacktrace", ex);
    } finally {
      FileUtil.closeSilently(wrapper);
      FileUtil.closeSilently(dataSourceManager);
      log.info("streams successfully closed");
    }
  }
}