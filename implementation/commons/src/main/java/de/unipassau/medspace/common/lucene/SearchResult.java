package de.unipassau.medspace.common.lucene;

import de.unipassau.medspace.common.util.FileUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;

import java.io.Closeable;
import java.io.IOException;

/**
 * Specifies the result of a {@link Query}
 */
public class SearchResult implements Closeable {

  /**
   * Used to read documents from the lucene index.
   */
  private IndexReader reader;

  /**
   * Used to search documents from the index using a query.
   */
  private IndexSearcher searcher;

  /**
   * The lucene search result.
   */
  private TopDocs topDocs;

  /**
   * Executes a given query and constructs a search result from it.
   * @param reader Used to read the lucene index.
   * @param query The query to get a search result from.
   * @throws IOException If an IO-Error occurs during collecting the search result.
   */
  public SearchResult(IndexReader reader, Query query) throws IOException {
    this(reader, query, false, 0);
  }

  /**
   * Executes a given query and constructs a search result from it.
   * @param reader Used to read the lucene index.
   * @param query The query to get a search result from.
   * @param totalHitCount Sets a maximum of the number of documents that can be included to the search result.
   *                      E.g. if it is set to 100, maximal 100 documents can be retrieved.
   * @throws IOException If an IO-Error occurs during collecting the search result.
   */
  public SearchResult(IndexReader reader, Query query, int totalHitCount) throws IOException {
    this(reader, query, true, totalHitCount);
  }

  /**
   * Executes a given query and constructs a search result from it.
   * @param reader Used to read the lucene index.
   * @param query The query to get a search result from.
   * @param useTotalHitCount Specifies if a limit for the retrieved documents should be used. If set to false, no limit
   *                         is used and thus all documents will be searched that match the query.
   * @param totalHitCount Sets a maximum of the number of documents that can be included to the search result.
   *                      E.g. if it is set to 100, maximal 100 documents can be retrieved.
   *                      This number is only considered if useTotalHitCount is set to true.
   * @throws IOException If an IO-Error occurs during collecting the search result.
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

  @Override
  public void close() throws IOException {
    FileUtil.closeSilently(reader, true);
  }

  /**
   * Provides a document from the search result by its index score.
   * E.g. the first document has index 0, the second document index 1 and so on.
   * @param i The score index of the wished document.
   * @return The document having the specified score index.
   * @throws IOException If an IO-Error occurs.
   */
  public Document getResult(int i) throws IOException {
    ScoreDoc[] hits = topDocs.scoreDocs;
    int docId = hits[i].doc;
    return searcher.doc(docId);
  }

  /**
   * Provides the number of documents that can be retrieved from this search result.
   * @return the number of documents that can be retrieved from this search result.
   */
  public int getSize() {
    return topDocs.scoreDocs.length;
  }

  /**
   * Provides access to the scored top documents.
   * @return The scored top documents found by this search result.
   */
  public TopDocs getTopDocs() {
    return topDocs;
  }

  /**
   * Provides the number of total documents of the index that match the query.
   * NOTE: This number doesn't specifiy the number of documents that can be retrieved from this SearchResult.
   * If used a totalHitCount while constructing the SearchResult, this number can be larger than the documents inside
   * this SearchResult. To get the number of included documents, use {@link #getSize}
   * @return The number of total documents of the index that match the query.
   */
  public int getTotalLength() {
    return topDocs.totalHits;
  }
}