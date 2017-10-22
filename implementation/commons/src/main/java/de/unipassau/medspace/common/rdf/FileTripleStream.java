package de.unipassau.medspace.common.rdf;

import de.unipassau.medspace.common.stream.Stream;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.iri.IRI;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.library.leviathan.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by David Goeth on 22.10.2017.
 */
public class FileTripleStream implements Stream<Triple> {
  private PipedRDFIterator<Triple> triples;
  private PrefixMapping prefixMapping;

  public FileTripleStream(File in, String contentType) throws URISyntaxException, FileNotFoundException {
    //Model model = ModelFactory.createDefaultModel();
    //RDFDataMgr.read(model, in, Lang.TURTLE);

    TypedInputStream typedInput = new TypedInputStream(new FileInputStream(in.getPath()), contentType);
    PipedRDFIterator<Triple> itTest = new PipedRDFIterator<>();
    PipedTriplesStream out = new PipedTriplesStream(itTest);
    RDFParser.create()
        .source(typedInput)
        .base(typedInput.getBaseURI())
        .lang(Lang.NTRIPLES)
        .context(null)
        .parse(out);

    Sink<Triple> sink = new Sink<Triple>() {
      @Override
      public void send(Triple item) {
        System.out.println("Got triple: " + item);
      }

      @Override
      public void flush() {

      }

      @Override
      public void close() {
        System.out.println("Closed sink!");
      }
    };
    //StreamRDFLib;
    StreamRDF destintation = StreamRDFLib.sinkTriples(sink);

    RDFDataMgr.parse(destintation, in.getAbsolutePath(), Lang.NTRIPLES) ;

    triples = itTest;

    Map<String, String> prefixes = itTest.getPrefixes().getMappingCopyStr();

    prefixMapping= PrefixMapping.Factory
                        .create()
                        .setNsPrefixes(prefixes);
  }

  @Override
  public Triple next() throws IOException {
    return triples.next();
  }

  @Override
  public boolean hasNext() throws IOException {
    return triples.hasNext();
  }

  @Override
  public void close() throws IOException {
    triples.close();
  }

  public PrefixMapping getPrefixMapping() {
    return prefixMapping;
  }
}