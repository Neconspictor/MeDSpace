package de.unipassau.medspace.common.lucene.rdf.converter;

import de.unipassau.medspace.common.lucene.rdf.LuceneClassAdapter;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.RDFFactory;
import de.unipassau.medspace.common.rdf.converter.DocumentClassTriplizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

import java.util.Arrays;
import java.util.List;

/**
 * TODO
 */
public class LuceneDocClassTriplizer extends DocumentClassTriplizer<Document,IndexableField> {


  /**
   * TODO
   *
   * @param documentAdapters
   * @param normalizer
   * @param rdfFactory
   */
  public LuceneDocClassTriplizer(List<LuceneClassAdapter<?>> documentAdapters,
                                 QNameNormalizer normalizer,
                                 RDFFactory rdfFactory) {
    super(documentAdapters, normalizer, rdfFactory);

  }

  @Override
  protected List<IndexableField> getFields(Document document, String name) {
    IndexableField[] array = document.getFields(name);
     return Arrays.asList(array);
  }

  @Override
  protected String getStringValue(IndexableField field) {
    return field.stringValue();
  }
}