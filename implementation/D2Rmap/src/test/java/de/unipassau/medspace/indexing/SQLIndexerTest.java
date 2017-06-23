package de.unipassau.medspace.indexing;

import de.fuberlin.wiwiss.d2r.exception.D2RException;
import de.unipassau.medsapce.indexing.SQLIndexer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by David Goeth on 13.06.2017.
 */
public class SQLIndexerTest {

  @Test
  public void indexSQLDatasourceTest() throws IOException, ParseException, D2RException {

    for (int i = 0; i < 1; ++i) {
      System.out.println(i);
      testIndex();
    }
  }

  private void testIndex() throws IOException, ParseException, D2RException {
    SQLIndexer indexer = SQLIndexer.create("./_work/index");
    ArrayList<Document> docs = new ArrayList<>();
    for (int i = 0; i < 100; ++i) {
      addDoc(docs, "The Art of Computer Science", "9900333X");
      addDoc(docs, "Managing Gigabytes", "55063554A");
      addDoc(docs, "Lucene in Action cool", "193398817");
      addDoc(docs, "Lucene in Action", "193398817");
      addDoc(docs, "Lucene cool for Dummies", "55320055Z");
    }

    indexer.open();
    indexer.reindex(docs);

    Instant startTime = Instant.now();
    //SearchResult result = doKeywordSearchWithoutPreCounting(new String[]{"lucene", "Computer"}, indexer, 40);
    SearchResult result = doKeywordSearchWithPreCounting(new String[]{"\"lucene cool\" OR lucene OR cool"}, indexer);
    Instant endTime = Instant.now();
    System.out.println("Time needed for query: " + Duration.between(startTime, endTime));


    System.out.println("Found " + result.getScoredLength() + " hits.");
    System.out.println("Total hit count: " + result.getTotalLength());
    for(int i=0;i<result.getScoredLength();++i) {
      Document d = result.getResult(i);
      System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("title"));
    }

    result.close();
    indexer.close();
  }

  private SearchResult doKeywordSearchWithPreCounting(String[] keywords, SQLIndexer indexer) throws ParseException, IOException {
    StandardAnalyzer analyzer = new StandardAnalyzer();
    StringBuilder querystr = new StringBuilder();
    String and = " AND ";
    for (String keyword : keywords) {
      querystr.append(keyword + and);
    }
    querystr = querystr.delete(querystr.length() - and.length(), querystr.length());
    System.out.println("query: " + querystr.toString());

    Query q = new QueryParser("title", analyzer).parse(querystr.toString());
    return new SearchResult(indexer.createReader(), q);
  }

  private SearchResult doKeywordSearchWithoutPreCounting(String[] keywords, SQLIndexer indexer, int totalHitCount) throws ParseException, IOException {
    StandardAnalyzer analyzer = new StandardAnalyzer();
    StringBuilder querystr = new StringBuilder();
    String and = " AND ";
    for (String keyword : keywords) {
      querystr.append("\"" + keyword + "\"" + and);
    }
    querystr = querystr.delete(querystr.length() - and.length(), querystr.length());
    System.out.println("query: " + querystr.toString());
    Query q = new QueryParser("field", analyzer).parse(querystr.toString());
    //MatchAllDocsQuery test = new MatchAllDocsQuery();
    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    //builder.add(test, BooleanClause.Occur.MUST);
    builder.add(q, BooleanClause.Occur.MUST);
    BooleanQuery bool = builder.build();
    return new SearchResult(indexer.createReader(), bool, totalHitCount);
  }

  private static void addDoc(Collection<Document> coll, String title, String isbn) throws IOException {
    Document doc = new Document();
    doc.add(new TextField("title", title, Field.Store.YES));
    doc.add(new StringField("isbn", isbn, Field.Store.YES));
    coll.add(doc);
  }

  private static class SearchResult implements Closeable {

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
}