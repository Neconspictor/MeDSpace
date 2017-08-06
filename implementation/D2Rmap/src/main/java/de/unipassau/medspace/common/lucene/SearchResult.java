package de.unipassau.medspace.common.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;

import java.io.Closeable;
import java.io.IOException;

/**
 * TODO
 */
public class SearchResult implements Closeable {

  /**
   * TODO
   */
  private IndexReader reader;

  /**
   * TODO
   */
  private IndexSearcher searcher;

  /**
   * TODO
   */
  private TopDocs topDocs;

  /**
   * TODO
   */
  public SearchResult(IndexReader reader, Query query) throws IOException {
    this(reader, query, false, 0);
  }

  /**
   * TODO
   */
  public SearchResult(IndexReader reader, Query query, int totalHitCount) throws IOException {
    this(reader, query, true, totalHitCount);
  }

  /**
   * TODO
   */
  private SearchResult(IndexReader reader, Query query, boolean useTotalHitCount, int totalHitCount) throws IOException {
    this.reader = reader;
    searcher = new IndexSearcher(reader);
    if (!useTotalHitCount) {
      TotalHitCountCollector hitCollector = new TotalHitCountCollector();
      searcher.search(query, hitCollector);
      totalHitCount = hitCollector.getTotalHits();
    }

    if (totalHitCount <= 0)
      totalHitCount = 1;

    topDocs = searcher.search(query, totalHitCount);
  }

  /**
   * TODO
   */
  @Override
  public void close() throws IOException {
    if (reader != null)
      reader.close();
  }

  /**
   * TODO
   */
  public Document getResult(int i) throws IOException {
    ScoreDoc[] hits = topDocs.scoreDocs;
    int docId = hits[i].doc;
    return searcher.doc(docId);
  }

  /**
   * TODO
   */
  public int getScoredLength() {
    return topDocs.scoreDocs.length;
  }

  /**
   * TODO
   */
  public TopDocs getTopDocs() {
    return topDocs;
  }

  /**
   * TODO
   */
  public int getTotalLength() {
    return topDocs.totalHits;
  }
}