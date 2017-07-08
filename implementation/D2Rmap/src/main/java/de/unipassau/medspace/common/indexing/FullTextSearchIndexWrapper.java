package de.unipassau.medspace.common.indexing;

import de.unipassau.medspace.common.query.KeywordSearcher;

import java.io.Closeable;
import java.io.IOException;

/**
 * A index is a concept for storing data suitable for later searching it.
 * A full-text search index is intended to be used for full-text and keyword searches.
 * This interface is intended to be used as a wrapper for a full-text search engine implementation.
 */
public interface FullTextSearchIndexWrapper<DocumentType> extends Closeable {

  /**
   * Deletes all the indexed data.
   * @throws IOException If an error occurs.
   */
  void clearIndex() throws IOException;

  /**
   * Creates a keyword searcher for initiating keywor searches onto the indexed data.
   * @return A keyword searcher for the wrapped index.
   * @throws IOException If an error occurs.
   */
  KeywordSearcher<DocumentType> createKeywordSearcher() throws IOException;

  /**
   * Indexes a list of data.
   * @param data The data to index.
   * @throws IOException if an error occurs.
   */
  void index(Iterable<DocumentType> data) throws IOException;

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
   */
  default void reindex(Iterable<DocumentType> data) throws IOException {
    clearIndex();
    index(data);
  };
}