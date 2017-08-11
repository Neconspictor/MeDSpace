package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.common.lucene.ResultFactory;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.d2r.MappedSqlTuple;

import java.io.IOException;

/**
 * Created by David Goeth on 30.06.2017.
 */
public class SqlToDocStream<DocType> implements Stream<DocType> {

  private final Stream<MappedSqlTuple> stream;
  private final ResultFactory<MappedSqlTuple, DocType> factory;

  public SqlToDocStream(Stream<MappedSqlTuple> stream,
                        ResultFactory<MappedSqlTuple, DocType> resultFactory) throws IOException {

    this.stream = stream;
    this.factory = resultFactory;
  }

  @Override
  public void close() throws IOException {
    stream.close();
  }

  @Override
  public boolean hasNext() throws IOException {
    return stream.hasNext();
  }

  @Override
  public DocType next() throws IOException {
    MappedSqlTuple tuple = stream.next();
    return factory.createDoc(tuple);
  }
}