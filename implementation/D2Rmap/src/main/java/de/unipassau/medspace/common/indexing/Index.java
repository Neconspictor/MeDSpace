package de.unipassau.medspace.common.indexing;

import de.unipassau.medspace.common.stream.Stream;
import java.io.Closeable;
import java.io.IOException;

/**
 * An index is a concept for storing data suitable for later searching it.
 * A full-text search index is intended to be used for full-text and keyword searches.
 * This interface is intended to be used as a wrapper for a full-text search engine implementation
 * like apache lucene.
 */
public interface Index<DocumentType> extends Closeable {

  /**
   * Deletes all the indexed data.
   * @throws IOException If an error occurs.
   */
  void clearIndex() throws IOException;

  /**
   * Checks, if this index is open and has indexed data.
   * @return true if the index is open and has indexed data.
   */
  default boolean exists() {
    try {
      return hasIndexedData();
    } catch (IOException e) {
      return false;
    }
  };

  /**
   * Checks if this index has indexed data.
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
   * Clears the index and indexes than new data.
   * @param data A stream of documents that should be indexed.
   * @throws IOException If any IO-Error occurs.
   */
  default void reindex(Stream<DocumentType> data) throws IOException {
    clearIndex();
    index(data);
  }
}