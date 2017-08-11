package de.unipassau.medspace.common.lucene;

import de.unipassau.medspace.common.rdf.TripleCacheStream;
import de.unipassau.medspace.common.stream.Stream;
import org.apache.jena.graph.Triple;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.List;

/**
 * Created by David Goeth on 06.08.2017.
 */
public class DocToTripleStream<ElemType> extends TripleCacheStream<Document> {

  protected ResultFactory<ElemType, Document> resultFactory;

  public DocToTripleStream(Stream<Document> source, ResultFactory<ElemType, Document> resultFactory) {
    this.resultFactory = resultFactory;
    stream = source;
  }

  @Override
  protected List<Triple> createTriples(Document elem) {
    return resultFactory.triplize(elem);
  }

  @Override
  public void close() throws IOException {
    super.close();
  }
}