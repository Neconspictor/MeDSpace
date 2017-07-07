package de.unipassau.medspace.common.indexing;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;

/**
 * Holds utility methods for assigning lucene index document a D2rMap id.
 */
public class DocumentMapper {

  /**
   * This constant is used as the field name for the map id
   */
  public static final String MAP_FIELD = "MAP";

  /**
   * Protected default constructor as this class is intended to be used
   * as a singleton. Only subclasses should be have access
   */
  protected DocumentMapper() {

  }

  /**
   * Assigns a given lucene document a map id.
   * @param doc The document to map
   * @param mapId The map id for the document
   */
  public static void mapTo(Document doc, String mapId) {
    doc.add(new StringField(MAP_FIELD, mapId, Field.Store.YES));
  }

  /**
   * Checks savely if a lucene Document is mapped to a map id.
   * @param doc The document to check
   * @return true, if the document is mapped to a map id
   */
  public static boolean isMapped(Document doc) {
    return doc.getField(MAP_FIELD) != null;
  }

  /**
   * Provides the map id of the specified document. It is assumed that the
   * document was previously mapped to a map id. If this isn't the case,
   * a IlegalArgumentException will be thrown.
   * @param doc The mapped document to get the map id from.
   * @return The map id of the document
   * @throws IllegalArgumentException If the document isn't mapped to a map id.
   */
  public static String getMap(Document doc) {
    if (!isMapped(doc)) {
      throw new IllegalArgumentException("Specified document isn't mapped: " + doc);
    }
    return doc.getField(MAP_FIELD).stringValue();
  }
}