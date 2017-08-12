package de.unipassau.medspace.common.indexing;

import de.unipassau.medspace.common.util.Converter;

/**
 * Created by David Goeth on 12.08.2017.
 */
public class IndexManager<DocType, ElemType> {
  protected IndexSearcher<DocType, ElemType> searcher;
  protected Converter<ElemType, DocType> converterToDoc;
  protected Converter<DocType, ElemType> converterToElem;

  public IndexManager(IndexSearcher<DocType, ElemType> searcher,
                      Converter<ElemType, DocType> converterToDoc,
                      Converter<DocType, ElemType> converterToElem) {
    this.searcher = searcher;
    this.converterToDoc = converterToDoc;
    this.converterToElem = converterToElem;
  }

  public Converter<ElemType, DocType> getConverterToDoc() {
    return converterToDoc;
  }

  public Converter<DocType, ElemType> getConverterToElem() {
    return converterToElem;
  }

  public Index<DocType, ElemType> getIndex() {
    return searcher.getIndex();
  }

  public IndexSearcher<DocType, ElemType> getSearcher() {
    return searcher;
  }
}