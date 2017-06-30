import de.fuberlin.wiwiss.d2r.*;
import de.fuberlin.wiwiss.d2r.exception.D2RException;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import de.unipassau.medspace.common.stream.StreamCollection;
import de.unipassau.medspace.util.FileUtil;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
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
    private final static String Outputfile = "./examples/medspace/output/output.rdf.xml";
    static D2rProcessor processor;

    public static void main(String[] args) {
        FileOutputStream out = null;
        FileInputStream in = null;
        FileInputStream in2 = null;
        String prettyPrintingLang = "N3";
        try {
            System.out.println("D2R test started ....");
            Configuration config = new ConfigurationReader().readConfig(D2RMap);
            DataSourceManager dataSourceManager = new DataSourceManager(config);
            processor = new D2rProcessor(config, dataSourceManager);
            System.out.println("D2R processor created ....");
            System.out.println("D2R file read ....");

            Lang lang = Lang.TURTLE;


            processor.reindex();

            Instant startTime = Instant.now();
            processor.doLuceneKeywordSearch(Arrays.asList("Male"));

            /*try (StreamCollection<Triple> tripleStream = processor.doKeywordSearch(Arrays.asList(""))) {
                tripleStream.start();
                RDFFormat format = StreamRDFWriter.defaultSerialization(lang);
                StreamRDF rdfOut = StreamRDFWriter.getWriterStream(System.out, format);
                rdfOut.start();
                for (Triple triple : tripleStream) {
                    rdfOut.triple(triple);
                }
                rdfOut.finish();
            }*/

            Instant endTime = Instant.now();
            //Lang lang = config.getOutputFormat();
            Lang prettyLang = RDFLanguages.shortnameToLang(prettyPrintingLang);


            //Model output = processor.doKeywordSearch(Arrays.asList("English", "Male"));
           // Model output = processor.generateAllInstancesAsModel();
            //output.write(System.out, prettyLang.getLabel());
            Model newModel = de.fuberlin.wiwiss.d2r.factory.ModelFactory.getInstance().createDefaultModel();
            out = new FileOutputStream("./modelout.d2rtmp");

            System.out.println("Lang: " + lang);
            //RDFDataMgr.write(out, output, lang);

            in = new FileInputStream("./modelout.d2rtmp");
            System.out.println("Start streaming the model...");
            RDFDataMgr.read(newModel, in, lang);
            in.close();

            out.close();
            out = new FileOutputStream("./myContent.d2rtmp");
            RDFDataMgr.write(out, newModel, prettyLang);

            //FileUtil.write(in, "./myContent.txt");

            //in2 = new FileInputStream("./myContent.txt");

            //newModel.write(System.out, "N3");
            //newModel.write(System.out, "N3");
            //RDFDataMgr.write(System.out, output, lang);
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
            FileUtil.closeSilently(out);
            FileUtil.closeSilently(in);
            FileUtil.closeSilently(in2);
            System.out.println("Closed streams successfully");
        }
    }
}