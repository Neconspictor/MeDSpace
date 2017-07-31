package de.unipassau.medspace;

import de.unipassau.medspace.common.SQL.DataSourceManager;
import de.unipassau.medspace.common.indexing.DataSourceIndex;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.stream.JenaRDFInputStream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.d2r.D2rProxy;
import de.unipassau.medspace.d2r.D2rWrapper;
import de.unipassau.medspace.d2r.MappedSqlTuple;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.d2r.config.ConfigurationReader;
import de.unipassau.medspace.common.SQL.HikariDataSourceManager;
import de.unipassau.medspace.d2r.exception.D2RException;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;


import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.riot.writer.WriterStreamRDFBlocks;
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
      //DataSourceStream<Triple> triples = searcher.searchForKeywords(Arrays.asList("male"));

      DataSourceStream<MappedSqlTuple> stream = wrapper.getProxy().getAllData();
      DataSourceStream<Triple> triples = new TripleData(stream);

      //DataSourceStream<Triple> triples = new TripleTestStream();
      PrefixMapping mapping = proxy.getNamespacePrefixMapper();

      boolean hasTriples = triples.hasNext();

      if (hasTriples) {
        RDFFormat format = StreamRDFWriter.defaultSerialization(lang);

        ResettableByteArrayInputStream in = new ResettableByteArrayInputStream();

        final TripleWriter writer = new TripleWriter(1024);

        final StreamRDF rdfOut = new WriterStreamRDFBlocks(new IndentedWriterEx(writer));

        InputStream test = new JenaRDFInputStream(triples, mapping);

        BufferedReader reader = new BufferedReader(new InputStreamReader(test, StandardCharsets.UTF_8));
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
      //Lang lang = config.getOutputFormat();
      Lang prettyLang = RDFLanguages.shortnameToLang(prettyPrintingLang);

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
      FileUtil.closeSilently(dataSourceManager);
      log.info("streams successfully closed");
    }
  }

  private static class TripleData implements DataSourceStream<Triple> {


    private DataSourceStream<MappedSqlTuple> stream;
    private List<Triple> cache = new ArrayList<>();

    public TripleData(DataSourceStream<MappedSqlTuple> stream) {
      this.stream = stream;
    }

    @Override
    public void close() throws IOException {
      stream.close();
    }

    @Override
    public boolean hasNext() {
      return stream.hasNext() || !cache.isEmpty();
    }

    @Override
    public Triple next() {
      if (cache.isEmpty()) {
        MappedSqlTuple tuple = stream.next();
        cache.addAll(tuple.getMap().createTriples(tuple.getSource()));
      }
      return cache.remove(0);
    }
  }

  private static class TripleTestStream implements DataSourceStream<Triple> {

    private List<Triple> data;

    public TripleTestStream() {
      data = new ArrayList<>();
      data.add(create("data1", "rdf:type", "data"));
      data.add(create("data2", "rdf:type", "data"));
    }

    @Override
    public void close() throws IOException {
      data.clear();
    }

    @Override
    public boolean hasNext() {
      return !data.isEmpty();
    }

    @Override
    public Triple next() {
      return data.remove(0);
    }

    private static Triple create(String subjectStr, String predicateStr, String objectStr) {
      Resource subject = ResourceFactory.createResource(subjectStr);
      Property predicate = ResourceFactory.createProperty(predicateStr);
      Resource object = ResourceFactory.createResource(objectStr);
      return Triple.create(subject.asNode(), predicate.asNode(), object.asNode());
    }
  }
}