package de.unipassau.medspace.common.indexing.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;

import java.io.Closeable;
import java.io.IOException;

public class SearchResult implements Closeable {

  private IndexReader reader;
  private IndexSearcher searcher;
  private TopDocs topDocs;

  public SearchResult(IndexReader reader, Query query) throws IOException {
    this(reader, query, false, 0);
  }

  public SearchResult(IndexReader reader, Query query, int totalHitCount) throws IOException {
    this(reader, query, true, totalHitCount);
  }

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

  @Override
  public void close() throws IOException {
    if (reader != null)
      reader.close();
  }

  public Document getResult(int i) throws IOException {
    ScoreDoc[] hits = topDocs.scoreDocs;
    int docId = hits[i].doc;
    return searcher.doc(docId);
  }

  public int getScoredLength() {
    return topDocs.scoreDocs.length;
  }

  public TopDocs getTopDocs() {
    return topDocs;
  }

  public int getTotalLength() {
    return topDocs.totalHits;
  }
}