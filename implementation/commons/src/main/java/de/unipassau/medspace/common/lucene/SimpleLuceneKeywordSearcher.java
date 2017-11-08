package de.unipassau.medspace.common.lucene;

import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.stream.Stream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.io.IOException;
import java.util.List;

/**
 * Created by David Goeth on 13.09.2017.
 */
public class SimpleLuceneKeywordSearcher extends LuceneKeywordSearcher {
  /**
   * Creates a new {@link LuceneKeywordSearcher}
   *
   * @param fields        Specifies the names of {@link Field} to consider for searching.
   * @param readerFactory Used to read an lucene index.
   * @param analyzer      Used to analyze the index.
   */
  public SimpleLuceneKeywordSearcher(List<String> fields, IndexReaderFactory readerFactory, Analyzer analyzer) {
    super(fields, readerFactory, analyzer);
  }

  @Override
  public Stream<Document> searchForKeywords(List<String> keywords) throws IOException,
      NoValidArgumentException {

    if (keywords.size() == 0) {
      throw new NoValidArgumentException("No keywords to search for");
    }



    return super.searchForKeywords(keywords);
  }
}