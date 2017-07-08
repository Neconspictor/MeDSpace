package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.common.SQL.SelectStatement;
import de.unipassau.medspace.common.lucene.DocumentMapper;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.D2rProcessor;
import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.d2r.D2rUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by David Goeth on 06.07.2017.
 */
public class SqlMapFactory extends DocumentMapper {

  /**
   * Protected default constructor as this class is intended to be used
   * as a singleton. Only subclasses should be have access
   */
  protected SqlMapFactory() {
    super();
  }

  public static Document create(SQLResultTuple tuple, String mapId) {
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

  public static SQLResultTuple create(Document doc, D2rProcessor processor) {
    assert isMapped(doc);

    String mapId = DocumentMapper.getMap(doc);
    ArrayList<Pair<String, String>> columnValuePairs = new ArrayList<>();

    D2rMap map = processor.getMapById(mapId);
    assert map != null;

    String columnNamePrefix = mapId + "_";

    doc.forEach(indexableField -> {
      String columnName = indexableField.name();

      // Skip the MAP element
      if (columnName.equals(MAP_FIELD)) return;

      // Delete the column prefix
      columnName = columnName.substring(columnNamePrefix.length(), columnName.length());

      String value = indexableField.stringValue();
      columnValuePairs.add(new Pair<>(columnName, value));
    });

    return new SQLResultTuple(columnValuePairs);
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
}