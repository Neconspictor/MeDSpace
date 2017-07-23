package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.common.SQL.SelectStatement;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.D2rProxy;
import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.d2r.D2rUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by David Goeth on 06.07.2017.
 */
public class SqlResultFactory {

  /**
   * This constant is used as the field name for the map id
   */
  private final String mapField;
  private final D2rProxy proxy;


  public SqlResultFactory(String mapField, D2rProxy proxy) {
    this.mapField = mapField;
    this.proxy = proxy;
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

  public Document create(SQLResultTuple tuple, String mapId) {
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

  public SQLResultTuple create(Document doc) {
    assert isMapped(doc);

    String mapId = getMapId(doc);
    ArrayList<Pair<String, String>> columnValuePairs = new ArrayList<>();

    D2rMap map = proxy.getMapById(mapId);
    assert map != null;

    String columnNamePrefix = mapId + "_";

    doc.forEach(indexableField -> {
      String columnName = indexableField.name();

      // Skip the MAP element
      if (columnName.equals(mapField)) return;

      // Delete the column prefix
      columnName = columnName.substring(columnNamePrefix.length(), columnName.length());

      String value = indexableField.stringValue();
      columnValuePairs.add(new Pair<>(columnName, value));
    });

    return new SQLResultTuple(columnValuePairs);
  }

  /**
   * Provides the map id of the specified document. It is assumed that the
   * document was previously mapped to a map id. If this isn't the case,
   * a IlegalArgumentException will be thrown.
   * @param doc The mapped document to get the map id from.
   * @return The map id of the document
   * @throws IllegalArgumentException If the document isn't mapped to a map id.
   */
  public D2rMap getMap(Document doc) {
    String mapId = getMapId(doc);
    return proxy.getMapById(mapId);
  }

  public String getMapId(Document doc) {
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
  public boolean isMapped(Document doc) {
    return doc.getField(mapField) != null;
  }

  /**
   * Assigns a given lucene document a map id.
   * @param doc The document to map
   * @param mapId The map id for the document
   */
  public void mapTo(Document doc, String mapId) {
    doc.add(new StringField(mapField, mapId, Field.Store.YES));
  }
}