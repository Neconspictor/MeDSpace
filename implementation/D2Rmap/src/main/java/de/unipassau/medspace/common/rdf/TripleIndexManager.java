package de.unipassau.medspace.common.rdf;

import de.unipassau.medspace.common.indexing.IndexManager;
import de.unipassau.medspace.common.indexing.IndexSearcher;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.util.Converter;
import org.apache.jena.graph.Triple;

import java.io.IOException;

/**
 * Created by David Goeth on 12.08.2017.
 */
public class TripleIndexManager<DocType, ElemType> extends IndexManager<DocType, ElemType> {
  protected Converter<KeywordSearcher<DocType>, KeywordSearcher<Triple>> tripleSearchConverter;

  public TripleIndexManager(IndexSearcher<DocType> searcher,
                            Converter<ElemType, DocType> converterToDoc,
                            Converter<DocType, ElemType> converterToElem,
                            Converter<KeywordSearcher<DocType>, KeywordSearcher<Triple>> tripleSearchConverter) {
    super(searcher, converterToDoc, converterToElem);
    this.tripleSearchConverter = tripleSearchConverter;
  }

  public KeywordSearcher<Triple> createTripleKeywordSearcher() throws IOException {
    KeywordSearcher<DocType> docKeyWordSearcher = searcher.createKeywordSearcher();
    return tripleSearchConverter.convert(docKeyWordSearcher);
  }
}
