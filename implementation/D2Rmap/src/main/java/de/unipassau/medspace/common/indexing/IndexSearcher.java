package de.unipassau.medspace.common.indexing;

import de.unipassau.medspace.common.query.KeywordSearcher;

import java.io.IOException;

/**
 * Created by David Goeth on 12.08.2017.
 */
public abstract class IndexSearcher<DocType, ElemType> {
  protected Index<DocType, ElemType> index;


  /**
   * TODO
   * Creates a keyword searcher for initiating keyword searches onto the indexed data.
   * @return A keyword searcher for the wrapped index.
   * @throws IOException If an error occurs.
   */
  public abstract KeywordSearcher<DocType> createKeywordSearcher() throws IOException;


  public Index<DocType, ElemType> getIndex() {
    return index;
  }
}
