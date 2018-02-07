package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene;

import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.RDFFactory;
import de.unipassau.medspace.common.rdf.RDFResource;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.util.Converter;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.ObjectProperty;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.Property;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter.LuceneDocAdapter;
import de.unipassau.medspace.wrapper.image_wrapper.rdf_mapping.Util;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class DocumentClassTriplizer implements Converter<Document, List<Triple>> {

  /**
   * TODO
   */
  private final List<LuceneDocAdapter<?>> adapters;

  /**
   * Used to normalize the rdf triples.
   */
  private final QNameNormalizer normalizer;

  /**
   * TODO
   */
  private final RDFFactory rdfFactory;

  /**
   * TODO
   * @param adapters
   * @param normalizer
   * @param rdfFactory
   */
  public DocumentClassTriplizer(List<LuceneDocAdapter<?>> adapters,
                                QNameNormalizer normalizer,
                                RDFFactory rdfFactory) {
    this.adapters = adapters;
    this.normalizer = normalizer;
    this.rdfFactory = rdfFactory;
  }

  @Override
  public List<Triple> convert(Document source) throws IOException {

    for (LuceneDocAdapter<?> adapter : adapters) {
      if (adapter.isConvertible(source))
        return convert(adapter, source);
    }

    throw new IOException("Document isn't convertable by any adapter: " + source);
  }

  /**
   * TODO
   * @param adapter
   * @param document
   * @return
   */
  private List<Triple> convert(LuceneDocAdapter<?> adapter, Document document) {
    List<Triple> triples = new ArrayList<>();

    String id = adapter.getObjectId(document);
    String baseURI = adapter.getClassBaseURI();
    String subjectURI = Util.createResourceId(normalizer, baseURI, id);
    RDFResource subject = rdfFactory.createResource(subjectURI);

    List<Pair<String, Property>> pairs = adapter.getFieldNamePropertyPairs();

    for (Pair<String, Property> pair : pairs) {
      IndexableField[] fields = document.getFields(pair.getValue0());

      for (IndexableField field : fields) {
        Triple triple = createTriple(field, adapter, pair, subject);
        triples.add(triple);
      }
    }

    return triples;
  }

  /**
   * TODO
   * @param field
   * @param adapter
   * @param pair
   * @param subject
   * @return
   */
  private Triple createTriple(IndexableField field,
                         LuceneDocAdapter<?> adapter,
                         Pair<String, Property> pair,
                         RDFResource subject) {

    Property property = pair.getValue1();

    String value;
    if (property instanceof ObjectProperty) {
      value = adapter.createValue(pair, field);
    } else {
      value= field.stringValue();
    }

    return Util.triplize(rdfFactory,
        normalizer,
        property,
        subject,
        value);
  }
}