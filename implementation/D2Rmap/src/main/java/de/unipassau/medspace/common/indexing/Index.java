package de.unipassau.medspace.common.indexing;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by David Goeth on 13.06.2017.
 */
public interface Index extends Closeable {

  void clearIndex() throws IOException;

  IndexReader createReader() throws IOException;

  void index(Iterable<Document> data) throws IOException;

  boolean isOpen();

  void open() throws IOException;

  /**
   * Clears the index and indexes the sql data.
   */
  void reindex(Iterable<Document> data) throws IOException;

}