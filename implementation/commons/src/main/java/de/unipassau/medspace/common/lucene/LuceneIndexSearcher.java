package de.unipassau.medspace.common.lucene;

import de.unipassau.medspace.common.indexing.Index;
import de.unipassau.medspace.common.indexing.IndexSearcher;
import de.unipassau.medspace.common.lucene.keyword_searcher.LuceneKeywordSearcher;
import de.unipassau.medspace.common.query.KeywordSearcher;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.List;

/**
 * An index searcher tailored to the lucene search engine.
 */
public class LuceneIndexSearcher extends IndexSearcher<Document> {

  /**
   * A list of fields, that should be searchable.
   */
  private List<String> fields;

  /**
   * A factory creating an index reader.
   */
  private IndexReaderFactory readerFactory;

  /**
   * A factory for creating an analyzer for analyzing the search query before executing it.
   */
  private AnalyzerBuilder builder;

  /**
   * Creates a new LuceneIndexSearcher.
   * @param index The index the index searcher should operate on.
   * @param fields A list of fields that are stored in the index and should be searchable by this searcher.
   * @param readerFactory A factory to create a reader for the index.
   * @param builder A factory for creating an analyzer for analyzing the search query before executing it.
   */
  public LuceneIndexSearcher(Index<Document> index, List<String> fields, IndexReaderFactory readerFactory,
                             AnalyzerBuilder builder) {
    super(index);
    this.fields = fields;
    this.readerFactory = readerFactory;
    this.builder = builder;
  }

  @Override
  public KeywordSearcher<Document> createKeywordSearcher(KeywordSearcher.Operator operator) throws IOException {
    if (operator == KeywordSearcher.Operator.AND)
      return new LuceneKeywordSearcher(fields, readerFactory, builder.build(), KeywordSearcher.Operator.AND);
    else
    return new LuceneKeywordSearcher(fields, readerFactory, builder.build(), KeywordSearcher.Operator.OR);
  }
}