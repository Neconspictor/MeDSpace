package de.unipassau.medspace.indexing;

import de.fuberlin.wiwiss.d2r.D2rMap;
import de.unipassau.medspace.SQL.SQLResultTuple;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.stream.StreamFactory;
import de.unipassau.medspace.mapping.SqlMapFactory;
import org.apache.lucene.document.Document;

import java.io.IOException;

/**
 * Created by David Goeth on 30.06.2017.
 */
public class SqlToDocumentStream implements DataSourceStream<Document> {

  private DataSourceStream<SQLResultTuple> stream;
  private String mapId;

  public SqlToDocumentStream(StreamFactory<SQLResultTuple> factory, D2rMap mapper) throws IOException {
    stream = factory.create();
    mapId = mapper.getId();
  }

  @Override
  public void close() throws IOException {
    stream.close();
  }

  @Override
  public boolean hasNext() {
    return stream.hasNext();
  }

  @Override
  public Document next() {
    SQLResultTuple tuple = stream.next();
    return SqlMapFactory.create(tuple, mapId);
  }
}