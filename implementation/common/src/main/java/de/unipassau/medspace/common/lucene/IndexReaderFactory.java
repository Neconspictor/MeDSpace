package de.unipassau.medspace.common.lucene;

import org.apache.lucene.index.IndexReader;

import java.io.IOException;

/**
 * A factory class for creating an IndexReader for a lucene Index
 */
public interface IndexReaderFactory {

  /**
   * Creates a new IndexReader.
   * @return A reader allowing to query a lucene index
   * @throws IOException Will be thrown if an I/O Error occurs
   */
  IndexReader createReader() throws IOException;
}
