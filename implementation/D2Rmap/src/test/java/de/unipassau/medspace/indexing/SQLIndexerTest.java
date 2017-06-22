package de.unipassau.medspace.indexing;

import de.fuberlin.wiwiss.d2r.exception.D2RException;
import de.unipassau.medsapce.indexing.SQLIndexer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
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
    addDoc(docs, "Lucene in Action", "193398817");
    addDoc(docs, "Lucene for Dummies", "55320055Z");
    addDoc(docs, "Managing Gigabytes", "55063554A");
    addDoc(docs, "The Art of Computer Science", "9900333X");

    indexer.open();
    indexer.reindex(docs);

    SearchResult result = doKeywordSearch(new String[]{"lucene"}, indexer, 1);

    System.out.println("Found " + result.getScoredLength() + " hits.");
    System.out.println("Total hit count: " + result.getTotalLength());
    for(int i=0;i<result.getScoredLength();++i) {
      Document d = result.getResult(i);
      System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("title"));
    }

    result.close();
    indexer.close();
  }

  private SearchResult doKeywordSearch(String[] keywords, SQLIndexer indexer, int topScore) throws ParseException, IOException {
    StandardAnalyzer analyzer = new StandardAnalyzer();
    String querystr = "lucene";
    Query q = new QueryParser("title", analyzer).parse(querystr);
    return new SearchResult(indexer.createReader(), q, topScore);
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

    public SearchResult(IndexReader reader, Query query, int hitsPerPage) throws IOException {
      this.reader = reader;
      searcher = new IndexSearcher(reader);
      topDocs = searcher.search(query, hitsPerPage);
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