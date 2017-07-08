package de.unipassau.medspace.common.query;

import de.unipassau.medspace.common.stream.DataSourceStream;

import java.io.IOException;
import java.util.List;

/**
 * A Keyword searcher is a concept for searching a set of data base on a list of keywords.
 * This interface provides a general view for implmenting such a functionality.
 */
public interface KeywordSearcher<E> {

  /**
   * Does a keyword search onto an underlying data set based on a given list of keywords.
   * @param keywords The keywords to search for
   * @return The keyword search result as stream of data.
   * @throws IOException If an error occurs while the search is processed.
   */
  DataSourceStream<E> searchForKeywords(List<String> keywords) throws IOException;
}