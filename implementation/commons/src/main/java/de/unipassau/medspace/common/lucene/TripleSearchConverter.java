package de.unipassau.medspace.common.lucene;

import de.unipassau.medspace.common.lucene.DocToTripleStream;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.Converter;
import org.apache.lucene.document.Document;

import java.util.List;

/**
 * Converts a keyword searcher outputing lucene documents to a converter that outputs the result to rdf triples.
 */
public class TripleSearchConverter implements Converter<KeywordSearcher<Document>,
    KeywordSearcher<Triple>> {

  /**
   * Used to convert the documents to rdf triples.
   */
  private Converter<Document, List<Triple>> triplizer;

  /**
   * Creates a new TripleSearchConverter.
   * @param triplizer The converter used to create triples from the documents.
   */
  public TripleSearchConverter(Converter<Document, List<Triple>> triplizer) {
    this.triplizer = triplizer;
  }

  @Override
  public KeywordSearcher<Triple> convert(KeywordSearcher<Document> source) {
    return keywords -> {
      Stream<Document> result =  source.searchForKeywords(keywords);
      return new DocToTripleStream(result, triplizer);
    };
  }
}
