import de.unipassau.medspace.common.SQL.DataSourceManager;
import de.unipassau.medspace.common.indexing.DataSourceIndex;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.util.FileUtil;
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


/**
 * Test Class for the D2R proxy. To install and test D2R proxy:
 * <BR>1. Follow the instructions in Installation.txt
 * <BR>2. Execute this test class or run D2rProxy from the comand line.
 *
 * <BR><BR>History: 
 * <BR>09-25-2003   : Changed for Jena2.
 * <BR>01-15-2003   : Initial version of this class.
 * @author Chris Bizer chris@bizer.de
 * @version V0.2
 */
public class TestProcessor {
    private final static String D2RMap = "./examples/medspace/medspace.d2r.xml";

    public static void main(String[] args) {
      String prettyPrintingLang = "N3";
      DataSourceManager dataSourceManager = null;
      D2rWrapper wrapper = null;

      try {
          System.out.println("D2R test started ....");
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
          System.out.println("D2R proxy created ....");
          System.out.println("D2R file read ....");

          Lang lang = Lang.TURTLE;

          if (config.isIndexUsed()) {
            //index = new SqlIndex(config.getIndexDirectory(), proxy);
          }

          wrapper = new D2rWrapper(proxy, config.getIndexDirectory());

          DataSourceIndex index = wrapper.getIndex();

          boolean exists = index.exists();

          if (!exists) {
            System.out.println("Indexing data...");
            index.reindex();
            System.out.println("Indexing done.");
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
            System.out.println("No results found!");
          }

          triples.close();


          Instant endTime = Instant.now();
          //Lang lang = config.getOutputFormat();
          Lang prettyLang = RDFLanguages.shortnameToLang(prettyPrintingLang);

          System.out.println("RDF data exported ....");
          System.out.println("Time elapsed: " + Duration.between(startTime, endTime));
      } catch (D2RException d2rex) {
          System.out.println("\n*** D2R Exception caught ***\n");
          System.out.println("Message:  " + d2rex.getMessage());
          System.out.println("");
          d2rex.printStackTrace();
      }
      catch (IOException ioex) {
          System.out.println("\n*** IO Exception caught ***\n");
          System.out.println("Message:  " + ioex.getMessage());
          System.out.println("");
          ioex.printStackTrace();
      }
      catch (java.lang.Throwable ex) {
          System.out.println("\n*** General Exception caught ***\n");
          System.out.println("Message:  " + ex.getMessage());
          System.out.println("");
          ex.printStackTrace();
      } finally {
          FileUtil.closeSilently(wrapper);
          FileUtil.closeSilently(dataSourceManager);
          System.out.println("Closed streams successfully");
      }
    }
}