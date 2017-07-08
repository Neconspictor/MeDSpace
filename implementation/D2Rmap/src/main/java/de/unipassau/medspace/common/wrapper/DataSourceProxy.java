package de.unipassau.medspace.common.wrapper;

import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.DataSourceStream;
import org.apache.jena.graph.Triple;

/**
 * A class that acts as a proxy for a datasource that provides data of type E. This proxy provides methods for
 * converting the data of the datasource into rdf tripes and grants access to all of its data. Additionally it provides search
 * functionality useful for a dataspace.
 */
public interface DataSourceProxy<E> {

  /**
   * Provides a search object for initiating a keyword search onto the
   * datasource's data set.
   * @return A KeywordSearcher for searching the proxied datasource's data set
   *         based on keywords.
   */
  KeywordSearcher<Triple> createKeywordSearcher();

  /**
   * Provides a TripleConverter to convert objects of type 'E' into rdf triples.
   * @return
   */
  TripleConverter<E> getConverter();

  /**
   * Provides a stream of all the proxied datasource's data set.
   * @return A stream of all data of the proxied datasource.
   */
  DataSourceStream<E> getData();
}