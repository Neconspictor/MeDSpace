package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.common.SQL.SelectStatement;
import de.unipassau.medspace.common.lucene.ResultFactory;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.d2r.D2rUtil;
import de.unipassau.medspace.d2r.D2rWrapper;
import de.unipassau.medspace.d2r.MappedSqlTuple;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO
 */
public abstract class AbstractD2rResultFactory<DocType> implements ResultFactory<MappedSqlTuple, DocType> {

  /**
   * This constant is used as the field name for the map id
   */
  protected final String mapField;
  protected final D2rWrapper wrapper;


  public AbstractD2rResultFactory(String mapField, D2rWrapper wrapper) {
    this.mapField = mapField;
    this.wrapper = wrapper;
  }

  public static List<String> getMappedColumns(D2rMap map) {
    List<String> result = new LinkedList<>();
    String mapid = map.getId() + "_";
    SelectStatement query = map.getQuery();

    for (String column : query.getColumns()) {
      String col = D2rUtil.getFieldNameUpperCase(column);
      result.add(mapid + col);
    }

    return result;
  }

  protected Document create(SQLResultTuple tuple, String mapId) {
    Document doc = new Document();

      mapTo(doc, mapId);

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

  /**
   * Provides the map id of the specified document. It is assumed that the
   * document was previously mapped to a map id. If this isn't the case,
   * a IlegalArgumentException will be thrown.
   * @param doc The mapped document to get the map id from.
   * @return The map id of the document
   * @throws IllegalArgumentException If the document isn't mapped to a map id.
   */
  protected D2rMap getMap(Document doc) {
    String mapId = getMapId(doc);
    return wrapper.getMapById(mapId);
  }

  protected String getMapId(Document doc) {
    if (!isMapped(doc)) {
      throw new IllegalArgumentException("Specified document isn't mapped: " + doc);
    }
    return doc.getField(mapField).stringValue();
  }

  /**
   * Checks savely if a lucene Document is mapped to a map id.
   * @param doc The document to check
   * @return true, if the document is mapped to a map id
   */
  protected boolean isMapped(Document doc) {
    return doc.getField(mapField) != null;
  }

  /**
   * Assigns a given lucene document a map id.
   * @param doc The document to map
   * @param mapId The map id for the document
   */
  protected void mapTo(Document doc, String mapId) {
    doc.add(new StringField(mapField, mapId, Field.Store.YES));
  }
}