package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.common.rdf.TripleCacheStream;
import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.d2r.D2rMap;

import org.apache.jena.graph.Triple;
import org.apache.lucene.document.Document;

import java.util.List;

/**
 * Created by David Goeth on 03.07.2017.
 */
public class DocToTripleStream extends TripleCacheStream<Document> {
  private final SqlResultFactory factory;

  public DocToTripleStream (DataSourceStream<Document> source, SqlResultFactory factory) {
    this.factory = factory;
    stream = source;
  }

  @Override
  protected List<Triple> createTriples(Document elem) {
    SQLResultTuple tuple = factory.create(elem);
    D2rMap map = factory.getMap(elem);
    return map.createTriples(tuple);
  }
}