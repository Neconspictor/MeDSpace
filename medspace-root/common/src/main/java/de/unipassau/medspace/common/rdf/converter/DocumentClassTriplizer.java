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
 * Used to create RDF triples from documents that are mapped to a specific RDF class.
 */
public abstract class DocumentClassTriplizer<DocType, FieldType> implements Converter<DocType, List<Triple>> {

  /**
   * The adapters used to create RDF triples.
   */
  protected final List<? extends DocumentAdapter<?, DocType, FieldType>> adapters;

  /**
   * Used to normalize rdf triples.
   */
  protected final QNameNormalizer normalizer;

  /**
   * Used to create RDF triples.
   */
  protected final RDFFactory rdfFactory;

  /**
   * Creates a new DocumentClassTriplizer.
   *
   * @param adapters The adapters to use.
   * @param normalizer Used to normalize RDF triples.
   * @param rdfFactory USed to create RDF triples.
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
   * Creates RDF triples from a document a given adapter.
   * @param adapter The adapter.
   * @param document The document.
   * @return RDF triples from a document a given adapter.
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
   * Creates an RDF triple statement for an RDF resource.
   * @param field The field to create a triple from.
   * @param adapter The adapter used for conversion.
   * @param pair The pair of field name and property mapping.
   * @param subject The resource to create the statement for.
   * @return an RDF triple statement for an RDF resource.
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
   * Provides the list of fields for a given field name from a document.
   * @param document The document to get the list from.
   * @param name the field name
   * @return The list of fields.
   */
  protected abstract List<FieldType> getFields(DocType document, String name);

  /**
   * Provides the value of a field.
   * @param field The field.
   * @return The string value of the field.
   */
  protected abstract String getStringValue(FieldType field);
}