package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.stream.StreamFactory;
import org.apache.lucene.document.Document;

import java.io.IOException;

/**
 * Created by David Goeth on 30.06.2017.
 */
public class SqlToDocumentStream implements DataSourceStream<Document> {

  private final DataSourceStream<SQLResultTuple> stream;
  private final String mapId;
  private final SqlResultFactory factory;

  public SqlToDocumentStream(StreamFactory<SQLResultTuple> streamFactory,
                             SqlResultFactory resultFactory,
                             D2rMap map) throws IOException {

    stream = streamFactory.create();
    mapId = map.getId();
    this.factory = resultFactory;
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
    return factory.create(tuple, mapId);
  }
}