package de.unipassau.medspace.common.rdf;

import de.unipassau.medspace.common.indexing.IndexManager;
import de.unipassau.medspace.common.indexing.Index;
import de.unipassau.medspace.common.indexing.IndexSearcher;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.util.Converter;
import org.apache.jena.graph.Triple;

import java.io.IOException;

/**
 * Specifies an {@link IndexManager} that additionally allows the creation of triples from documents and the creation
 * of a keyword searcher that returns triples instead of documents.
 */
public class TripleIndexManager<DocType, ElemType> extends IndexManager<DocType, ElemType> {

  /**
   * Used to convert documents to triples.
   */
  protected Converter<KeywordSearcher<DocType>, KeywordSearcher<Triple>> tripleSearchConverter;

  /**
   * Creates a new TripleIndexManager.
   * @param searcher Used for searching an {@link Index}.
   * @param converterToDoc Used to convert element objects to documents.
   * @param converterToElem Used to convert documents to element objects.
   * @param tripleSearchConverter Used to convert documents to triples.
   */
  public TripleIndexManager(IndexSearcher<DocType> searcher,
                            Converter<ElemType, DocType> converterToDoc,
                            Converter<DocType, ElemType> converterToElem,
                            Converter<KeywordSearcher<DocType>, KeywordSearcher<Triple>> tripleSearchConverter) {
    super(searcher, converterToDoc, converterToElem);
    this.tripleSearchConverter = tripleSearchConverter;
  }

  /**
   * Creates a new keyword searcher that searches the index from this class and returns the result represented as
   * {@link Triple}s
   * @return A keyword searcher that returns triples.
   * @throws IOException If an IO-Error occurs.
   */
  public KeywordSearcher<Triple> createTripleKeywordSearcher() throws IOException {
    KeywordSearcher<DocType> docKeyWordSearcher = searcher.createKeywordSearcher();
    return tripleSearchConverter.convert(docKeyWordSearcher);
  }
}