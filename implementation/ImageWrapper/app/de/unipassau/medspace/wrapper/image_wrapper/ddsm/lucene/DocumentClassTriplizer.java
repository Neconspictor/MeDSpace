package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene;

import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.RDFFactory;
import de.unipassau.medspace.common.rdf.RDFResource;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.util.Converter;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.Property;
import de.unipassau.medspace.wrapper.image_wrapper.rdf_mapping.Util;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

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
  private final List<DocumentAdapter<?>> adapters;

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
  public DocumentClassTriplizer(List<DocumentAdapter<?>> adapters,
                                QNameNormalizer normalizer,
                                RDFFactory rdfFactory) {
    this.adapters = adapters;
    this.normalizer = normalizer;
    this.rdfFactory = rdfFactory;
  }

  @Override
  public List<Triple> convert(Document source) throws IOException {

    for (DocumentAdapter<?> adapter : adapters) {
      if (adapter.isConvertable(source))
        return convert(adapter, source);
    }

    throw new IOException("Document isn't convertable by any adapter: " + source);
  }

  private List<Triple> convert(DocumentAdapter<?> adapter, Document document) {
    List<Triple> triples = new ArrayList<>();
    List<IndexableField> fields = adapter.getValidFields(document);

    String id = adapter.getClassId(document);
    String baseURI = adapter.getClassBaseURI();
    String subjectURI = Util.createResourceId(normalizer, baseURI, id);
    RDFResource subject = rdfFactory.createResource(subjectURI);

    for (IndexableField field : fields) {
      Property property = adapter.getPropertyByFieldName(field.name());
      String value = field.stringValue();

      Triple triple = Util.triplize(rdfFactory,
          normalizer,
          property,
          subject,
          value);

      triples.add(triple);

    }

    return triples;
  }
}