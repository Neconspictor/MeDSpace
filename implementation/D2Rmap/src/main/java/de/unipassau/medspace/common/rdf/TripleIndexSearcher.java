package de.unipassau.medspace.common.rdf;

import de.unipassau.medspace.common.indexing.IndexSearcher;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.util.Converter;
import org.apache.jena.graph.Triple;

import java.io.IOException;

/**
 * Created by David Goeth on 12.08.2017.
 */
public abstract class TripleIndexSearcher<DocType, ElemType> extends IndexSearcher<DocType, ElemType> {
  protected Converter<KeywordSearcher<DocType>, KeywordSearcher<Triple>> tripleSearchConverter;


  public TripleIndexSearcher(Converter<KeywordSearcher<DocType>,
                                               KeywordSearcher<Triple>> tripleSearchConverter) {
    this.tripleSearchConverter = tripleSearchConverter;
  }

  public KeywordSearcher<Triple> createTripleKeywordSearcher() throws IOException {
    KeywordSearcher<DocType> docKeyWordSearcher = createKeywordSearcher();
    return tripleSearchConverter.convert(docKeyWordSearcher);
  }
}
