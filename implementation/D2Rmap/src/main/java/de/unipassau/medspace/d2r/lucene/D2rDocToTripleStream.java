package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.common.rdf.TripleCacheStream;
import de.unipassau.medspace.common.stream.DataSourceStream;

import org.apache.jena.graph.Triple;

import java.util.List;

/**
 * Created by David Goeth on 03.07.2017.
 */
public class D2rDocToTripleStream<DocType> extends TripleCacheStream<DocType> {
  private final AbstractD2rResultFactory<DocType> factory;

  public D2rDocToTripleStream(DataSourceStream<DocType> source, AbstractD2rResultFactory<DocType> factory) {
    this.factory = factory;
    stream = source;
  }

  @Override
  protected List<Triple> createTriples(DocType elem) {
    return factory.triplize(elem);
  }
}