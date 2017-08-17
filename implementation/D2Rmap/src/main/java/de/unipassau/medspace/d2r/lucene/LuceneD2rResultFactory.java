package de.unipassau.medspace.d2r.lucene;

import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.SQL.SelectStatement;
import de.unipassau.medspace.common.util.Converter;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.D2rUtil;
import de.unipassau.medspace.d2r.D2rWrapper;
import de.unipassau.medspace.d2r.MappedSqlTuple;
import org.apache.jena.graph.Triple;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides access to converters for converting {@link Document} to {@link MappedSqlTuple} and vice versa.
 * Additionally it provides access to a converter that converts {@link Document} to a {@link Triple} list.
 */
public class LuceneD2rResultFactory  {

  /**
   * Converts sql tuples to documents
   */
  private Converter<MappedSqlTuple, Document> toDoc;

  /**
   * Converts documents to mapped sql tuples.
   */
  private Converter<Document, MappedSqlTuple> toElem;

  /**
   * Converts documents to rdf triples.
   */
  private Converter<Document, List<Triple>> triplizer;

  /**
   * This constant is used as the field name for the map id
   */
  protected final String mapField;

  /**
   * Used to get access to the D2rMaps.
   */
  protected final D2rWrapper wrapper;


  /**
   * Creates a new LuceneD2rResultFactory
   * @param mapField Will be stored in the created documents, to assign documents a D2rMap.
   * @param wrapper Used to get access to the D2rMaps.
   */
  public LuceneD2rResultFactory(String mapField, D2rWrapper wrapper) {
    this.mapField = mapField;
    this.wrapper = wrapper;
    toDoc = (MappedSqlTuple elem) ->
       create(elem.getSource(), elem.getMap().getId());
    toElem = (Document doc) -> createElem(doc);
    triplizer = (Document doc) -> triplize(doc);
  }

  /**
   * Provides a converter for converting mapped sql tuples to documents.
   * @return A converter that converts mapped sql tuples to documents.
   */
  public Converter<MappedSqlTuple, Document> getToDoc() {
    return toDoc;
  }

  /**
   * Provides a converter for converting documents to mapped sql tuples.
   * @return A converter that converts documents to mapped sql tuples.
   */
  public Converter<Document, MappedSqlTuple> getToElem() {
    return toElem;
  }

  /**
   * Provides a converter for converting documents to rdf triples.
   * @return A converter that converts documents to rdf triples.
   */
  public Converter<Document, List<Triple>> getTriplizer() {
    return triplizer;
  }

  /**
   * This methods fetches the names of all columns from the given D2rMap and adds the id of the D2rMap
   * followed by an underscore as a prefix to all column names.
   * Each column name is thus substituted by the following rule: 'mapid'_'columnName'
   *
   * This method is useful when working with lucene when it is wished to store the columns of a D2rMap separated from
   * columns of other D2rMaps, which could have the same name, but potentially not the same meaning.
   *
   * @param map The map to get the prefixed column name list from.
   * @return A list of column names that are mapped to the specified D2rMap.
   */
  protected static List<String> getMappedColumns(D2rMap map) {
    List<String> result = new LinkedList<>();
    String mapid = map.getId() + "_";
    SelectStatement query = map.getQuery();

    for (String column : query.getColumns()) {
      String col = D2rUtil.getColumnNameUpperCase(column);
      result.add(mapid + col);
    }

    return result;
  }


  /**
   * Creates a document from a sql tuple and a given D2rMap id.
   * @param tuple The sql tuple to store into the new document.
   * @param mapId The D2rMap id to assign to the document.
   * @return a Document that represents the sql tuple and hasthe specified a map id field that allows to easily
   * get the D2rMap from the document in order to create triples from it.
   */
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
   * Creates a mapped sql tuple from a document.
   * Note: The document has to have a valid map id field in order to work properly.
   * @param doc The document to create a mapped sql tuple from.
   * @return A mapped sql tuple.
   * @throws IllegalArgumentException if the document hasn't got a map id field.
   */
  protected MappedSqlTuple createElem(Document doc) {
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

  /**
   *  Provides the map id of a given document that has been assigned previously a map field.
   * @param doc The document to get the map id from.
   * @return The id of the D2rMap the document is assigned to.
   * @throws IllegalArgumentException If the document hasn't got a map id field.
   */
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

  /**
   * Creates rdf triples from a document.
   * @param doc The document to create triples from.
   * @return A list of rdf triples that represent the document.
   */
  protected List<Triple> triplize(Document doc) {
    MappedSqlTuple tuple = createElem(doc);
    D2rMap map = tuple.getMap();
    return map.createTriples(tuple.getSource());
  }
}