package de.unipassau.medspace.common.stream;


import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.Triple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * An input stream from a serialized de.unipassau.medspace.common.stream.Stream of jena rdf triples.
 * This class allows to serialize a stream of jena rdf triples and provide an input stream of the
 * serialization. For the serialization this class uses a serialization language and
 * an optional PrefixMapping for prefixing rdf namespaces.
 */
public class TripleInputStream extends InputStream {

  /**
   * Used to provide the serialized rdf triples.
   */
  private final ResettableByteArrayInputStream in;

  /**
   * The stream of jena rdf triples.
   */
  private final Stream<Triple> triples;

  /**
   * An byte array output stream.
   * Used as the output target of the serialization.
   */
  private final ByteArrayOutputStreamEx out;

  /**
   * Used to serialize the triples.
   */
  private StreamRDF rdfOut;

  /**
   * Creates a new TripleInputStream from a stream of jena rdf triples, a org.apache.jena.riot.Lang
   * to use as the serialization output format and org.apache.jena.shared.PrefixMapping that defines the namespace
   * prefixes to use in the serialization process of the rdf triples.
   * @param triples The stream of jena rdf triples that should be serialized. The result of the serialization can
   *                than be read from this input stream.
   * @param format The serialization output format. Note, that not all org.apache.jena.riot.Lang classes are supported,
   *             but only which one, that can be streamed. See
   *             <a href="https://jena.apache.org/documentation/io/streaming-io.html#rdfformat-and-lang">
   *             https://jena.apache.org/documentation/io/streaming-io.html#rdfformat-and-lang</a>
   *             for a list of supported languages.
   * @param namespaces The namespace prefixes to use in the serialization process or null, if no prefixes should be used.
   */
  public TripleInputStream(Stream<Triple> triples, String format, Set<Namespace> namespaces) {
    this.triples = triples;
    in = new ResettableByteArrayInputStream();
    out = new ByteArrayOutputStreamEx();



    rdfOut = StreamRDFWriter.getWriterStream(out, Lang.TURTLE);
    rdfOut.start();
    if (namespaces != null ) {
      addMapping(namespaces);
    }
  }

  /**
   * Closes the underlying triple stream and finishes pending serialization process.
   * @throws IOException If an IO-Error occurs.
   */
  public void close() throws IOException{
      rdfOut.finish();
      triples.close();
  }


  @Override
  public int read() throws IOException {
    int elem = in.read();

    // elem == -1, i.d. in has to be filled with new data
    while (elem == -1) {
      elem = in.read();
      updateInputBuffer();

      if (!triples.hasNext()) {
        rdfOut.finish();
        updateInputBuffer();
        return in.read();
      }

      Triple source = triples.next();

      Node s = NodeFactory.createURI(source.getSubject());
      Node p = NodeFactory.createURI(source.getPredicate());
      Node o = NodeFactory.createLiteral(source.getObject());
      org.apache.jena.graph.Triple triple = org.apache.jena.graph.Triple.create(s,p,o);

      rdfOut.triple(triple);
    }

    return elem;
  }

  /**
   * Adds a map of prefix/namespaces which should be used in serialization process.
   * @param namespaces The prefix/namespace map to add.
   */
  private void addMapping(Set<Namespace> namespaces) {
    for (Namespace namespace : namespaces) {
      rdfOut.prefix(namespace.getPrefix(), namespace.getFullURI());
    }
  }

  /**
   * Updates the input buffer.
   * @throws IllegalStateException if this method is called if the used input stream has still data to read.
   */
  private void updateInputBuffer() {
    int pos = out.size();
    if (pos == 0) return;

    if (in.available() != 0) throw new IllegalStateException("Not all bytes consumed yet!");
    in.reset(out.getBufferSource(), 0, pos);
    out.reset();
  }

  /**
   * An extension to the java.io.ByteArrayOutputStream class. Allows access to the output byte buffer.
   */
  private static class ByteArrayOutputStreamEx extends ByteArrayOutputStream {

    /**
     * Provides the used output byte buffer.
     * @return The used output byte buffer.
     */
    public byte[] getBufferSource() {
      return buf;
    }
  }
}