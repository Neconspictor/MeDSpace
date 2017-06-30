package de.unipassau.medspace.indexing;

import de.unipassau.medspace.SQL.SQLResultTuple;
import de.unipassau.medspace.SQL.SqlStream;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.stream.StreamFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import java.io.IOException;

/**
 * Created by David Goeth on 30.06.2017.
 */
public class SqlToDocumentStream implements DataSourceStream<Document> {

  private DataSourceStream<SQLResultTuple> stream;

  public SqlToDocumentStream(StreamFactory<SQLResultTuple> factory) throws IOException {
    stream = factory.create();
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
    Document doc = new Document();

    for (int i = 0; i < tuple.getColumnCount(); ++i) {
      String columnName = tuple.getColumnName(i);
      String columnValue = tuple.getValue(i);

      assert columnName != null;
      assert columnValue != null;

      Field field = new TextField(tuple.getColumnName(i), tuple.getValue(i), Field.Store.YES);

      doc.add(field);
    }

    return doc;
  }
}
