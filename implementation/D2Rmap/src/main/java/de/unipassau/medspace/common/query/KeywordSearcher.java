package de.unipassau.medspace.common.query;

import de.unipassau.medspace.common.stream.DataSourceStream;

import java.io.IOException;
import java.util.List;

/**
 * A Keyword searcher is a concept for searching a set of search fields on a list of keywords.
 * This interface provides a general view for implmenting such a functionality.
 */
public interface KeywordSearcher<E> {

  /**
   * Does a keyword search onto a list of search fields based on a given list of keywords.
   * @param keywords The keywords for searching data in the fields
   * @return The keyword search result as stream of data.
   * @throws IOException If an error occurs while the search is processed.
   */
  DataSourceStream<E> searchForKeywords(List<String> keywords) throws IOException;
}