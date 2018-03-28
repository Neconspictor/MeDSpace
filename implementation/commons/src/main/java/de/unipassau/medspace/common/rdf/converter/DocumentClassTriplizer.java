package de.unipassau.medspace.common.rdf.converter;

import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.RDFFactory;
import de.unipassau.medspace.common.rdf.RDFResource;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.rdf.mapping.DocumentAdapter;
import de.unipassau.medspace.common.rdf.mapping.ObjectPropertyMapping;
import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.common.util.Converter;
import de.unipassau.medspace.common.util.RdfUtil;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public abstract class DocumentClassTriplizer<DocType, FieldType> implements Converter<DocType, List<Triple>> {

  /**
   * TODO
   */
  protected final List<? extends DocumentAdapter<?, DocType, FieldType>> adapters;

  /**
   * Used to normalize rdf triples.
   */
  protected final QNameNormalizer normalizer;

  /**
   * TODO
   */
  protected final RDFFactory rdfFactory;

  /**
   * TODO
   * @param adapters
   * @param normalizer
   * @param rdfFactory
   */
  public DocumentClassTriplizer(List<? extends DocumentAdapter<?, DocType, FieldType>> adapters,
                                QNameNormalizer normalizer,
                                RDFFactory rdfFactory) {
    this.adapters = adapters;
    this.normalizer = normalizer;
    this.rdfFactory = rdfFactory;
  }


  @Override
  public List<Triple> convert(DocType source) throws IOException {
    for (DocumentAdapter<?, DocType, FieldType> adapter : adapters) {
      if (adapter.isConvertible(source))
        return convert(adapter, source);
    }

    throw new IOException("Document isn't convertable by any adapter: " + source);
  }

  /**
   * TODO
   * @param adapter TODO
   * @param document TODO
   * @return
   */
  protected List<Triple> convert(DocumentAdapter<?, DocType, FieldType> adapter, DocType document) {
    List<Triple> triples = new ArrayList<>();

    String id = adapter.getObjectId(document);
    String baseURI = adapter.getClassBaseURI();
    String subjectURI = RdfUtil.createResourceId(normalizer, baseURI, id);
    RDFResource subject = rdfFactory.createResource(subjectURI);

    List<Pair<String, PropertyMapping>> pairs = adapter.getFieldNamePropertyPairs();

    for (Pair<String, PropertyMapping> pair : pairs) {
      List<FieldType> fields = getFields(document, pair.getValue0());

      for (FieldType field : fields) {
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
  protected Triple createTriple(FieldType field,
                              DocumentAdapter<?, DocType, FieldType> adapter,
                              Pair<String, PropertyMapping> pair,
                              RDFResource subject) {

    PropertyMapping property = pair.getValue1();

    String value = adapter.createValue(pair, field);

    return RdfUtil.triplize(rdfFactory,
        normalizer,
        property,
        subject,
        value);
  }

  /**
   * TODO
   * @param document
   * @return
   */
  protected abstract List<FieldType> getFields(DocType document, String name);

  /**
   * TODO
   * @param field
   * @return
   */
  protected abstract String getStringValue(FieldType field);
}