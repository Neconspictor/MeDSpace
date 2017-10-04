package de.unipassau.medspace.common.indexing;

import de.unipassau.medspace.common.query.KeywordSearcher;

import java.io.Closeable;
import java.io.IOException;

/**
 * A IndexSearcher is used to search objects from an index.
 * Generic type DocType: The document type of the used index.
 */
public abstract class IndexSearcher<DocType> implements Closeable {

  /**
   * The index used for searching.
   */
  protected Index<DocType> index;

  /**
   * Creates a new IndexSearcher.
   * @param index The index that is used for searching.
   */
  public IndexSearcher(Index<DocType> index) {
    this.index = index;
  }


  /**
   * Creates a keyword searcher for initiating keyword searches onto the indexed data.
   * @return A keyword searcher for the wrapped index.
   * @throws IOException If an error occurs.
   */
  public abstract KeywordSearcher<DocType> createKeywordSearcher() throws IOException;


  /**
   * Provides the index of this class.
   * @return The index of this class.
   */
  public Index<DocType> getIndex() {
    return index;
  }

  @Override
  public void close() throws IOException {
    index.close();
  }
}