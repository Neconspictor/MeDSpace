import de.unipassau.medspace.common.SQL.DataSourceManager;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.d2r.config.ConfigurationReader;
import de.unipassau.medspace.d2r.D2rProcessor;
import de.unipassau.medspace.common.SQL.HikariDataSourceManager;
import de.unipassau.medspace.d2r.exception.D2RException;

import java.io.*;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import de.unipassau.medspace.d2r.lucene.D2rKeywordSearcher;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;


/**
 * Test Class for the D2R processor. To install and test D2R processor: 
 * <BR>1. Follow the instructions in Installation.txt
 * <BR>2. Execute this test class or run D2rProcessor from the comand line.
 *
 * <BR><BR>History: 
 * <BR>09-25-2003   : Changed for Jena2.
 * <BR>01-15-2003   : Initial version of this class.
 * @author Chris Bizer chris@bizer.de
 * @version V0.2
 */
public class TestProcessor {
    private final static String D2RMap = "./examples/medspace/medspace.d2r.xml";
    static D2rProcessor processor;

    public static void main(String[] args) {
        String prettyPrintingLang = "N3";
        DataSourceManager dataSourceManager = null;
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

            processor = new D2rProcessor(config, dataSourceManager);
            System.out.println("D2R processor created ....");
            System.out.println("D2R file read ....");

            Lang lang = Lang.TURTLE;


            processor.reindex();
            D2rKeywordSearcher searcher = new D2rKeywordSearcher(processor);
            searcher.useLucene(true);

            Instant startTime = Instant.now();
            DataSourceStream<Triple> triples = searcher.searchForKeywords(Arrays.asList("male"));

            RDFFormat format = StreamRDFWriter.defaultSerialization(lang);
            StreamRDF rdfOut = StreamRDFWriter.getWriterStream(System.out, format);
            rdfOut.start();
            for (Triple triple : triples) {
                rdfOut.triple(triple);
            }
            rdfOut.finish();

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
            if (processor != null) processor.shutdown();
            FileUtil.closeSilently(dataSourceManager);
            System.out.println("Closed streams successfully");
        }
    }
}