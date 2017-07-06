package de.unipassau.medspace.rdf;

import de.fuberlin.wiwiss.d2r.D2rMap;
import de.fuberlin.wiwiss.d2r.D2rProcessor;
import de.unipassau.medspace.SQL.SQLResultTuple;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.mapping.LuceneDocMapper;
import de.unipassau.medspace.indexing.SearchResult;

import de.unipassau.medspace.mapping.SqlMapFactory;
import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by David Goeth on 03.07.2017.
 */
public class DocToTripleStream extends TripleCacheStream<Document> {

  private static Logger log = Logger.getLogger(DocToTripleStream.class);
  private D2rProcessor processor;

  public DocToTripleStream (SearchResult result, D2rProcessor processor) {
    this.processor = processor;
    stream = new DocStream(result);
  }

  @Override
  protected List<Triple> createTriples(Document elem) {

    SQLResultTuple tuple = SqlMapFactory.create(elem, processor);
    D2rMap map = processor.getMapById(LuceneDocMapper.getMap(elem));
    return map.createTriples(tuple, processor.getNormalizer());
  }

  private static class DocStream implements DataSourceStream<Document> {

    private int index;
    private SearchResult result;

    public DocStream(SearchResult result) {
      index = 0;
      this.result = result;
    }

    @Override
    public void close() throws IOException {
      result.close();
    }

    @Override
    public boolean hasNext() {
      return index < result.getScoredLength();
    }

    @Override
    public Document next() {
      Document doc = null;
      try {
        doc = result.getResult(index);
      } catch (IOException e) {
        log.error("IOException retrived while trying to access a lucene search result document: ", e);
      }

      ++index;
      return doc;
    }
  }
}