package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.Converter;
import de.unipassau.medspace.d2r.MappedSqlTuple;

import java.io.IOException;

/**
 * A stream that converts {@link MappedSqlTuple}s to the document type of an index.
 */
public class SqlToDocStream<DocType> implements Stream<DocType> {

  /**
   * Used as input for accessing the mapped sql tuples.
   */
  private final Stream<MappedSqlTuple> stream;

  /**
   * Used to convert the mapped sql tuples to the desired document class.
   */
  private final Converter<MappedSqlTuple, DocType> converter;

  /**
   * Creates a new SqlToDocStream.
   * @param stream Used as input for accessing the mapped sql tuples
   * @param converter Used to convert the mapped sql tuples to the desired document class.
   * @throws IOException If an IO-Error occurs.
   */
  public SqlToDocStream(Stream<MappedSqlTuple> stream,
                        Converter<MappedSqlTuple, DocType> converter) throws IOException {
    this.stream = stream;
    this.converter = converter;
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
    return converter.convert(tuple);
  }
}