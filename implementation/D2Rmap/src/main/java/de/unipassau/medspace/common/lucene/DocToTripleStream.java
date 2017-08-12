package de.unipassau.medspace.common.lucene;

import de.unipassau.medspace.common.rdf.TripleCacheStream;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.Converter;
import org.apache.jena.graph.Triple;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.List;

/**
 * Created by David Goeth on 06.08.2017.
 */
public class DocToTripleStream extends TripleCacheStream<Document> {

  protected Converter<Document, List<Triple>> tripleConverter;

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