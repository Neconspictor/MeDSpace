package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.common.rdf.TripleCacheStream;
import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.D2rProcessor;
import de.unipassau.medspace.common.lucene.DocumentMapper;
import de.unipassau.medspace.common.lucene.SearchResult;

import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.List;

/**
 * Created by David Goeth on 03.07.2017.
 */
public class DocToTripleStream extends TripleCacheStream<Document> {

  private D2rProcessor processor;

  public DocToTripleStream (DataSourceStream<Document> source, D2rProcessor processor) {
    this.processor = processor;
    stream = source;
  }

  @Override
  protected List<Triple> createTriples(Document elem) {

    SQLResultTuple tuple = SqlMapFactory.create(elem, processor);
    D2rMap map = processor.getMapById(DocumentMapper.getMap(elem));
    return map.createTriples(tuple, processor.getNormalizer());
  }
}