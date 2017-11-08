package de.unipassau.medspace.common.stream;


import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.rdf.TripleWriter;
import de.unipassau.medspace.common.rdf.TripleWriterFactory;
import de.unipassau.medspace.common.util.FileUtil;

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
public class TestTripleInputStream extends InputStream {

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

  private TripleWriter writer;

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
  public TestTripleInputStream(Stream<Triple> triples, String format, Set<Namespace> namespaces, TripleWriterFactory factory)
      throws IOException, NoValidArgumentException {

    this.triples = triples;
    in = new ResettableByteArrayInputStream();
    out = new ByteArrayOutputStreamEx();

    this.writer = factory.create(out, format);

    if (namespaces != null ) {
      addMapping(namespaces);
    }

    while(triples.hasNext()) {
      writer.write(triples.next());
    }

    writer.close();
  }

  /**
   * Closes the underlying triple stream and finishes pending serialization process.
   * @throws IOException If an IO-Error occurs.
   */
  public void close() throws IOException {

    try {
      //triples.close();
      writer.close();
      //out.close();
      //in.close();
    } catch (IOException e) {
      FileUtil.closeSilently(triples);
      throw e;
    }
  }


  @Override
  public int read() throws IOException {
    int elem = in.read();

    // elem == -1, i.d. in has to be filled with new data
    while (elem == -1) {
      elem = in.read();
      updateInputBuffer();

      if (!triples.hasNext()) {
        updateInputBuffer();
        //writer.close();
        return in.read();
      }
    }

    return elem;
  }

  /**
   * Adds a map of prefix/namespaces which should be used in serialization process.
   * @param namespaces The prefix/namespace map to add.
   */
  private void addMapping(Set<Namespace> namespaces) throws IOException {
    for (Namespace namespace : namespaces) {
      writer.write(namespace.getPrefix(), namespace.getFullURI());
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