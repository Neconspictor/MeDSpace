package de.unipassau.medspace.common.lucene;

import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.rdf.TripleCacheStream;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.Converter;

import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.List;

/**
 * DocToTripleStream is a {@link TripleCacheStream} tailored to the needs when working with lucene {@link Document}
 */
public class DocToTripleStream extends TripleCacheStream<Document> {

  /**
   * Used to convert a {@link Document} into a list of {@link Triple}s
   */
  protected Converter<Document, List<Triple>> tripleConverter;

  /**
   * Creates a new {@link DocToTripleStream} from a {@link Stream} of documents.
   * @param source The {@link Stream} of documents to convert to triples.
   * @param tripleConverter Used to convert documents to triples.
   */
  public DocToTripleStream(Stream<Document> source, Converter<Document, List<Triple>> tripleConverter) {
    this.tripleConverter = tripleConverter;
    stream = source;
  }

  @Override
  protected List<Triple> createTriples(Document doc) {
    return tripleConverter.convert(doc);
  }

  @Override
  public void close() throws IOException {
    super.close();
  }
}