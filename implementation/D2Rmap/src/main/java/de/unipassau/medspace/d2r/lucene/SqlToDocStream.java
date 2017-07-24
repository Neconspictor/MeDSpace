package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.stream.StreamFactory;
import de.unipassau.medspace.d2r.MappedSqlTuple;
import org.apache.lucene.document.Document;

import java.io.IOException;

/**
 * Created by David Goeth on 30.06.2017.
 */
public class SqlToDocStream implements DataSourceStream<Document> {

  private final DataSourceStream<MappedSqlTuple> stream;
  private final SqlResultFactory factory;

  public SqlToDocStream(DataSourceStream<MappedSqlTuple> stream,
                        SqlResultFactory resultFactory) throws IOException {

    this.stream = stream;
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
    MappedSqlTuple tuple = stream.next();
    D2rMap map = tuple.getMap();
    return factory.create(tuple.getSource(), map.getId());
  }
}