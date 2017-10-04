package de.unipassau.medspace.common.wrapper;

import de.unipassau.medspace.common.query.KeywordSearcher;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;

import java.io.Closeable;
import java.io.IOException;

/**
 * Defines a wrapper for a dataspace.
 * A wrapper is used to handle queries meant for a specific datasource or sub-dataspace.
 * It reformulates queries of the dataspace so that the datasource can answer it and converts the query result of the
 * datasource into rdf triples.
 * The minimal functionality a wrapper has to implement is, that a keyword search can be executed on a datasource even
 * if the datasource doesn't support any keyword search functionality.
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
   * Provides a mapping of prefix and namespace URIs used to prefix triples returned by this wrapper.
   * @return a mapping of prefix and namespace URIs used to prefix triples returned by this wrapper.
   */
  PrefixMapping getNamespacePrefixMapper();

  /**
   * Clears the index and reindexes again all data of the proxied datasource.
   * @throws IOException If any IO-Error occurs.
   */
  void reindexData() throws IOException;

  /**
   * Checks if this index has indexed data.
   * @return true, if the index of the wrapper has data.
   */
  boolean existsIndex();

  /**
   * Checks, if the wrapper uses an index to answer keyword searches instead consulting the datasource directly.
   * @return true, if an index is used.
   */
  boolean isIndexUsed();
}