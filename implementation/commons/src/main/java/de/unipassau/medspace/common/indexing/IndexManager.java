package de.unipassau.medspace.common.indexing;

import de.unipassau.medspace.common.util.Converter;

import java.io.Closeable;
import java.io.IOException;

/**
 * A IndexManager ia an utility for working with an index and searching on it.<br><br>
 * A IndexManager is created on the base on two generic object types: The document type (DocType)
 * and the object type (ObjectType). <br><br>
 * 1. DocType: Specifies the document type of the index to manage.<br>
 * 2. ObjectType: Specifies a class type instances of the document type should be able to be converted.
 * <br><br>
 * The ObjectType is intended to be used to map the content of a document (of type DocType) to a class that is more
 * convenient to work with.<br>
 * To get instances of the ObjectType, the IndexManager provides a converter, the user can use.<br>
 * Additionally, the IndexManager provides a converter to convert objects of type ObjectType to the index's document
 * type. Thus, a user can easily create documents from objects of type ObjectType<br><br>
 *
 * Example:<br>
 * If we have data of persons (e.g. names and addresses). First we will store the data in objects of type
 * 'Person'. Than we will create a IndexManager and specify for the ObjectType our Person class. We retrieve a converter
 * object from the IndexManager to create documents from our Person objects and store them in the index.
 * Through a IndexSearcher retrieved from the IndexManager we can search for documents. To get person objects from the
 * search result we can again use a converter from the IndexManager.
 */
public class IndexManager<DocType, ObjectType> implements Closeable {

  /**
   * Wraps the index and allows the user to search the index.
   */
  protected IndexSearcher<DocType> searcher;

  /**
   * Converter to build documents from objects.
   */
  protected Converter<ObjectType, DocType> converterToDoc;

  /**
   * Converter to build objects from documents.
   */
  protected Converter<DocType, ObjectType> converterToObject;

  /**
   * Creates a new IndexManager.
   * @param searcher A searcher of an index.
   * @param converterToDoc converts objects to documents of the index
   * @param converterToObject converts documents to objects.
   */
  public IndexManager(IndexSearcher<DocType> searcher,
                      Converter<ObjectType, DocType> converterToDoc,
                      Converter<DocType, ObjectType> converterToObject) {
    this.searcher = searcher;
    this.converterToDoc = converterToDoc;
    this.converterToObject = converterToObject;
  }

  /**
   * Provides a converter that converts objects to documents for the index.
   * @return
   */
  public Converter<ObjectType, DocType> getConverterToDoc() {
    return converterToDoc;
  }

  /**
   * Provides a converter that converts documents of the index to a given object type.
   * @return
   */
  public Converter<DocType, ObjectType> getConverterToObject() {
    return converterToObject;
  }

  /**
   * Provides the index of this class.
   * @return The index of this class.
   */
  public Index<DocType> getIndex() {
    return searcher.getIndex();
  }

  /**
   * Provides a searcher of the index.
   * @return A searcher of the index.
   */
  public IndexSearcher<DocType> getSearcher() {
    return searcher;
  }

  @Override
  public void close() throws IOException {
    searcher.close();
  }
}