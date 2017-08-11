package de.unipassau.medspace.common.indexing;

import de.unipassau.medspace.common.lucene.ResultFactory;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.Stream;
import org.apache.jena.graph.Triple;

import java.io.Closeable;
import java.io.IOException;

/**
 * A index is a concept for storing data suitable for later searching it.
 * A full-text search index is intended to be used for full-text and keyword searches.
 * This interface is intended to be used as a wrapper for a full-text search engine implementation.
 */
public interface DataSourceIndex<DocumentType, ElemType> extends Closeable {

  /**
   * Deletes all the indexed data.
   * @throws IOException If an error occurs.
   */
  void clearIndex() throws IOException;

  /**
   * TODO
   * Creates a keyword searcher for initiating keyword searches onto the indexed data.
   * @return A keyword searcher for the wrapped index.
   * @throws IOException If an error occurs.
   */
  default KeywordSearcher<Triple> createKeywordSearcher() throws IOException {
    return convert(createDocKeywordSearcher());
  };

  /**
   * TODO
   * Creates a keyword searcher for initiating keyword searches onto the indexed data.
   * @return A keyword searcher for the wrapped index.
   * @throws IOException If an error occurs.
   */
  KeywordSearcher<DocumentType> createDocKeywordSearcher() throws IOException;


  KeywordSearcher<Triple> convert(KeywordSearcher<DocumentType> source) throws IOException;


  /**
   * TODO
   * Checks, if the materialized index was created once before.
   * @return true if the index was created once before.
   */
  default boolean exists() {
    try {
      return hasIndexedData();
    } catch (IOException e) {
      return false;
    }
  };

  /**
   * Checks if this index as indexed data.
   *
   * @return true if this index has at least one indexed document.
   * @throws IOException if the index isn't opened or an error occurs.
   */
  boolean hasIndexedData() throws IOException;

  /**
   * Indexes a list of data.
   * @param data The data to index.
   * @throws IOException if an error occurs.
   */
  void index(Stream<DocumentType> data) throws IOException;

  /**
   * Checks if the wrapped index is open.
   * @return true, if the index is open.
   */
  boolean isOpen();

  /**
   * Opens the index.
   * @throws IOException if the index couldn't be opened.
   */
  void open() throws IOException;

  /**
   * Clears the index and indexes the sql data.
   * @param data TODO
   * @throws IOException TODO
   */
  default void reindex(Stream<DocumentType> data) throws IOException {
    clearIndex();
    index(data);
  };

  ResultFactory<ElemType, DocumentType> getResultFactory();
}