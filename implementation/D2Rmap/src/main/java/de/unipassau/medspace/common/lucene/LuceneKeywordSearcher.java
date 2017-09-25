package de.unipassau.medspace.common.lucene;

import de.unipassau.medspace.common.exception.NotValidArgumentException;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.Stream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * A keyword searcher for the apache lucene search engine.
 */
public class LuceneKeywordSearcher implements KeywordSearcher<Document> {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(LuceneKeywordSearcher.class);

  /**
   * An array of names specifying lucene {@link org.apache.lucene.document.Field}s that should be included in the search.
   * Fields that aren't included into the array will be ignored.
   */
  protected String[] fields;

  /**
   * A factory for creating an {@link IndexReader}, which is used to read a lucene index.
   */
  protected IndexReaderFactory readerFactory;

  /**
   * Used to analyze the indexed data.
   */
  protected Analyzer analyzer;

  /**
   * Creates a new {@link LuceneKeywordSearcher}
   * @param fields Specifies the names of {@link org.apache.lucene.document.Field} to consider for searching.
   * @param readerFactory Used to read an lucene index.
   * @param analyzer Used to analyze the index.
   */
  public LuceneKeywordSearcher(List<String> fields, IndexReaderFactory readerFactory, Analyzer analyzer) {
    this.fields = new String[fields.size()];
    fields.toArray(this.fields);
    this.readerFactory = readerFactory;
    this.analyzer = analyzer;
  }

  @Override
  public Stream<Document> searchForKeywords(List<String> keywords) throws IOException,
      NotValidArgumentException {

    if (keywords.size() == 0) {
      throw new NotValidArgumentException("No keywords to search for");
    }

    // escape  keywords
    for (int i = 0; i < keywords.size(); ++i) {
      String escaped = escape(keywords.get(i));
      keywords.set(i, escaped);
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
    } catch (IOException e) {
      throw new IOException("Exception while querying the index: ", e);
    }

    return  new DocumentStream(result);
  }

  /**
   * Escape special characters and parer tokens from a lucene search query
   * @param query The query to process.
   * @return An escaped query
   */
  protected String escape(String query) {

    // Eleminate the boolean operators OR, AND, NOT as they are only considered in upper case.
    query = query.toLowerCase();

    // Escape special characters used in the lucene query parser: + - && || ! ( ) { } [ ] ^ " ~ \ * ? : /
    query = query.replaceAll("\\+|-|&&|\\|\\||!|\\(|\\)|\\{|\\}|\\[|\\]|\\^|\"|~|\\\\|\\*|\\?|:|/",
        "\\\\$0");

    return query;
  }

  /**
   * Constructs a new keyword query for the lucene index.
   * @param fieldNameArray The names of a list of {@link org.apache.lucene.document.Field} to consider for searching.
   * @param keywords The keywords to search for in the specified {@link org.apache.lucene.document.Field}s.
   * @return A new query that searches for the specified keywords.
   * @throws ParseException If the query couldn't be constructed.
   */
  protected Query constructQuery(String[] fieldNameArray, List<String> keywords) throws ParseException {
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

  /**
   * Executes a query.
   * @param query The query to execute.
   * @return The result of the executed query.
   * @throws IOException If an IO-Error occurs while retrieving the search result.
   */
  protected SearchResult doLuceneKeywordSearch(Query query) throws IOException {

    if (log.isDebugEnabled())
      log.debug("Constructed query: " + query);
    return new SearchResult(readerFactory.createReader(), query);
  }

  /**
   * A stream of lucene {@link Document}s
   */
  protected static class DocumentStream implements Stream<Document> {

    /**
     * A cursor for getting the next document from the search result.
     */
    private int index;

    /**
     * The search result to retrieve the documents from.
     */
    private SearchResult searchResult;

    /**
     * Creates a new DocumentStream from a given search result.
     * @param searchResult Used as document input stream.
     */
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
      return index < searchResult.getSize();
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