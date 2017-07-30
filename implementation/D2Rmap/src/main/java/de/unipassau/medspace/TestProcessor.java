package de.unipassau.medspace;

import de.unipassau.medspace.common.SQL.DataSourceManager;
import de.unipassau.medspace.common.indexing.DataSourceIndex;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.common.util.TempFile;
import de.unipassau.medspace.d2r.D2rProxy;
import de.unipassau.medspace.d2r.D2rWrapper;
import de.unipassau.medspace.d2r.MappedSqlTuple;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.d2r.config.ConfigurationReader;
import de.unipassau.medspace.common.SQL.HikariDataSourceManager;
import de.unipassau.medspace.d2r.exception.D2RException;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;


import de.unipassau.medspace.d2r.stream.SqlToTripleStream;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.base.Sys;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.riot.writer.WriterStreamRDFBase;
import org.apache.jena.riot.writer.WriterStreamRDFBlocks;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Quad;
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

      //ObjectOutputStream out = new ObjectOutputStream(System.out);
      PrefixMapping mapping = proxy.getNamespacePrefixMapper();



      /*for (Triple triple : triples) {
        //triple.
        System.out.println(triple.toString(mapping));
      }*/

      //out.close();

      boolean hasTriples = triples.hasNext();

      if (hasTriples) {
        //TypedInputStream inputStream =  TypedInputStream.wrap(null);
        //RDFParser.create().source(inputStream).parse((StreamRDF)null);
        RDFFormat format = StreamRDFWriter.defaultSerialization(lang);
        //final StreamRDF rdfOut = StreamRDFWriter.getWriterStream(System.out, format);

        ResettableByteArrayInputStream in = new ResettableByteArrayInputStream();

        final TripleWriter writer = new TripleWriter(1024);

        final StreamRDF rdfOut = new WriterStreamRDFBlocks(new IndentedWriterEx(writer));

        InputStream test = new JenaRDFInputStream(triples, mapping, false);

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
}