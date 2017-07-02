package de.unipassau.medspace.indexing;

import de.fuberlin.wiwiss.d2r.D2rMapper;
import de.unipassau.medspace.SQL.SQLResultTuple;
import de.unipassau.medspace.SQL.SqlStream;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.stream.StreamFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.io.IOException;

/**
 * Created by David Goeth on 30.06.2017.
 */
public class SqlToDocumentStream implements DataSourceStream<Document> {

  private DataSourceStream<SQLResultTuple> stream;
  private String mapId;

  public SqlToDocumentStream(StreamFactory<SQLResultTuple> factory, D2rMapper mapper) throws IOException {
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
    Document doc = new Document();

    doc.add(new StringField("MAP", mapId, Field.Store.YES));

    for (int i = 0; i < tuple.getColumnCount(); ++i) {
      String tupleColumnName = tuple.getColumnName(i);
      String columnValue = tuple.getValue(i);

      assert tupleColumnName != null;
      assert columnValue != null;

      String columnName = mapId + "_" + tupleColumnName;

      Field field = new TextField(columnName, tuple.getValue(i), Field.Store.YES);

      doc.add(field);
    }

    return doc;
  }
}
