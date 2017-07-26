package de.unipassau.medspace.common.wrapper;

import de.unipassau.medspace.common.query.KeywordSearcher;
import org.apache.jena.graph.Triple;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by David Goeth on 24.07.2017.
 */
public interface Wrapper extends Closeable {

  /**
   * Provides a search object for initiating a keyword search onto the
   * datasource's data set.
   * @return A KeywordSearcher for searching the proxied datasource's data set
   *         based on keywords.
   * @throws IOException If an error occurs while trying to create a keyword searcher
   */
  KeywordSearcher<Triple> createKeywordSearcher() throws IOException;
}
