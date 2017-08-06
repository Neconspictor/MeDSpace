package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.D2rWrapper;
import de.unipassau.medspace.d2r.MappedSqlTuple;
import org.apache.jena.graph.Triple;
import org.apache.lucene.document.Document;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David Goeth on 06.08.2017.
 */
public class LuceneD2rResultFactory extends AbstractD2rResultFactory<Document> {

  public LuceneD2rResultFactory(String mapField, D2rWrapper wrapper) {
    super(mapField, wrapper);
  }

 @Override
  public Document createDoc(MappedSqlTuple elem) {
    return create(elem.getSource(), elem.getMap().getId());
  }

  @Override
  public List<Triple> triplize(Document elem) {
    MappedSqlTuple tuple = createElem(elem);
    D2rMap map = tuple.getMap();
    return map.createTriples(tuple.getSource());
  }

  @Override
  public Class<Document> getDocType() {
    return Document.class;
  }


  @Override
  public MappedSqlTuple createElem(Document doc) {
    assert isMapped(doc);

    String mapId = getMapId(doc);
    ArrayList<Pair<String, String>> columnValuePairs = new ArrayList<>();

    D2rMap map = getMap(doc);
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

    return new MappedSqlTuple(new SQLResultTuple(columnValuePairs), map);
  }
}