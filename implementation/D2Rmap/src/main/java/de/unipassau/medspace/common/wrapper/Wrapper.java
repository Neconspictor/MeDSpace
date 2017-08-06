package de.unipassau.medspace.common.wrapper;

import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.DataSourceStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;

import java.io.Closeable;
import java.io.IOException;

/**
 * TODO
 */
public interface Wrapper extends Closeable {

  /**
   * Provides a search object for initiating a keyword search onto the
   * datasource's data set.
   * @return A KeywordSearcher for searching the proxied datasource's data set
   *         based on keywords.
   * @throws IOException If an error occurs while trying to createDoc a keyword searcher
   */
  KeywordSearcher<Triple> createKeywordSearcher() throws IOException;

  /**
   * TODO
   * @return
   */
  PrefixMapping getNamespacePrefixMapper();

  /**
   * TODO
   * @throws IOException
   */
  void reindexData() throws IOException;

  /**
   * TODO
   * @return
   */
  boolean existsIndex();

  /**
   * TODO
   * @return
   */
  boolean isIndexUsed();

  /**
   * TODO Test stuff
   * @return
   */
  DataSourceStream<Triple> getAllData() throws IOException;
}
