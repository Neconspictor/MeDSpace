package de.unipassau.medspace.common.indexing;

import de.unipassau.medspace.common.indexing.SQLIndex;
import de.unipassau.medspace.common.indexing.SearchResult;
import de.unipassau.medspace.d2r.exception.D2RException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by David Goeth on 13.06.2017.
 */
public class SQLIndexTest {

  @Test
  public void indexSQLDatasourceTest() throws IOException, ParseException, D2RException {

    for (int i = 0; i < 1; ++i) {
      System.out.println(i);
      testIndex();
    }
  }

  private void testIndex() throws IOException, ParseException, D2RException {
    SQLIndex indexer = SQLIndex.create("./_work/index");
    ArrayList<Document> docs = new ArrayList<>();
    for (int i = 0; i < 1; ++i) {
      addDoc(docs, "The Art of Computer Science", "9900333X");
      addDoc(docs, "Managing Gigabytes", "55063554A");
      addDoc(docs, "Lucene in Action cool", "193398817");
      addDoc(docs, "Lucene in Action", "193398817");
      addDoc(docs, "Lucene cool for Dummies", "55320055Z");
    }

    indexer.open();
    indexer.reindex(docs);

    Instant startTime = Instant.now();
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

  private SearchResult doKeywordSearchWithPreCounting(String[] keywords, SQLIndex indexer) throws ParseException, IOException {
    StandardAnalyzer analyzer = new StandardAnalyzer();
    StringBuilder querystr = new StringBuilder();
    String and = " AND ";
    for (String keyword : keywords) {
      querystr.append(keyword + and);
    }
    querystr = querystr.delete(querystr.length() - and.length(), querystr.length());
    System.out.println("query: " + querystr.toString());

    IndexReader reader = indexer.createReader();
    List<IndexableField> fields = reader.document(0).getFields();
    ArrayList<String> fieldNames = new ArrayList<>();
    for (IndexableField field : fields) {
      fieldNames.add(field.name());
    }
    String[] fieldNameArray = new String[fieldNames.size()];
    Query q = new MultiFieldQueryParser(fieldNames.toArray(fieldNameArray),analyzer).parse(querystr.toString());
    return new SearchResult(indexer.createReader(), q);
  }

  private static void addDoc(Collection<Document> coll, String title, String isbn) throws IOException {
    Document doc = new Document();
    doc.add(new TextField("title", title, Field.Store.YES));
    doc.add(new TextField("isbn", isbn, Field.Store.YES));
    coll.add(doc);
  }
}