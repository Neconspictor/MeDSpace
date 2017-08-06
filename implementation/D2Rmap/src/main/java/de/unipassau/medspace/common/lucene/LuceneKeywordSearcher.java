package de.unipassau.medspace.common.lucene;

import de.unipassau.medspace.common.exception.NotValidArgumentException;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.DataSourceStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by David Goeth on 09.07.2017.
 */
public class LuceneKeywordSearcher implements KeywordSearcher<Document> {

  private static Logger log = LoggerFactory.getLogger(LuceneKeywordSearcher.class);

  private String[] fields;
  private IndexReaderFactory readerFactory;

  public LuceneKeywordSearcher(List<String> fields, IndexReaderFactory readerFactory) {
    this.fields = new String[fields.size()];
    fields.toArray(this.fields);
    this.readerFactory = readerFactory;
  }

  @Override
  public DataSourceStream<Document> searchForKeywords(List<String> keywords) throws IOException,
      NotValidArgumentException {

    if (keywords.size() == 0) {
      throw new NotValidArgumentException("No keywords to search for");
    }

    SearchResult result = null;
    Query query = null;
    try {
      query = constructQuery(fields, keywords);
    } catch (ParseException e) {
      throw new NotValidArgumentException("One of the keywords isn't valid", e);
    }

    try {
      result = doLuceneKeywordSearch(query);
    } catch (IOException | ParseException e) {
      throw new IOException("Exception while querying the index: ", e);
    }
    return  new DocumentStream(result);
  }

  private Query constructQuery(String[] fieldNameArray, List<String> keywords) throws ParseException {
    Analyzer analyzer = new StandardAnalyzer();
    QueryParser parser = new MultiFieldQueryParser(fieldNameArray,analyzer);

    StringBuilder keywordsConcat = new StringBuilder();
    for (String keyword : keywords) {
      keywordsConcat.append(keyword);
      keywordsConcat.append(" ");
    }

    Query query = parser.parse(keywordsConcat.toString());

    if (log.isDebugEnabled())
      log.debug("Constructed query: " + query);

    return query;
  }

  private SearchResult doLuceneKeywordSearch(Query query) throws IOException,
      ParseException {

    if (log.isDebugEnabled())
      log.debug("Constructed query: " + query);
    return new SearchResult(readerFactory.createReader(), query);
  }

  private static class DocumentStream implements DataSourceStream<Document> {

    private int index;
    private SearchResult searchResult;

    public DocumentStream(SearchResult searchResult) {
      index = 0;
      this.searchResult = searchResult;
    }

    @Override
    public void close() throws IOException {
      searchResult.close();
    }

    @Override
    public boolean hasNext() {
      return index < searchResult.getScoredLength();
    }

    @Override
    public Document next() {
      Document doc = null;
      try {
        doc = searchResult.getResult(index);
      } catch (IOException e) {
        log.error("Error while retrieving next search result", e);
      }
      ++index;
      return doc;
    }
  }
}