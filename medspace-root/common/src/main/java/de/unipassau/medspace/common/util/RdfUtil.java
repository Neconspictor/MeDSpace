package de.unipassau.medspace.common.util;

import de.unipassau.medspace.common.rdf.*;
import de.unipassau.medspace.common.rdf.mapping.DataTypePropertyMapping;
import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Contains utility methods useful when working with RDF.
 */
public class RdfUtil {

  /**
   * Creates a new literal.
   * @param factory Used to create the literal
   * @param normalizer The normalizer Used to normalize the literal.
   * @param value The literal value
   * @param propertyMapping Used for property mapping
   * @return The created literal.
   */
  public static RDFObject createLiteral(RDFFactory factory,
                                        QNameNormalizer normalizer,
                                        DataTypePropertyMapping propertyMapping,
                                        String value) {

    String dataType = propertyMapping.getDataType();
    String lang = propertyMapping.getLang();
    RDFObject object;

    // The lang tag specifies indirectly the dataType (rdf:lang)
    // Thus the lang tag has a higher priority than the dataType tag
    if ((value != null) && (lang != null)) {
      object = factory.createLiteral(value, lang);
    } else if ((value != null) && (dataType != null)) {
      // if no lang tag is set but the dataType tag createDoc a typed literal
      dataType = normalizer.normalize(dataType);
      object = factory.createTypedLiteral(value, dataType);
    }  else {
      // no lang tag and dataType set; assume xsd:string is the data type
      object = factory.createLiteral(value);
    }
    return object;
  }

  /**
   * Create a new property.
   * @param factory Used to create the property.
   * @param normalizer Used to normalize the property.
   * @param propertyMapping Used to create the property.
   * @return a new property.
   */
  public static RDFResource createProperty(RDFFactory factory,
                                           QNameNormalizer normalizer,
                                           PropertyMapping propertyMapping) {
    String propertyQName = propertyMapping.getPropertyType();
    String propURI = normalizer.normalize(propertyQName);
    return factory.createResource(propURI);
  }

  /**
   * Creates a new resource id.
   *
   * @param normalizer Used to normalize the resource
   * @param baseURI The base URI.
   * @param id The id for the resource.
   * @return a new resource id.
   */
  public static String createResourceId(QNameNormalizer normalizer, String baseURI, String id) {
    String subject = baseURI + "#" + id;
    return normalizer.normalize(subject);
  }

  /**
   * Creates a formatted string that represents a date.
   * @param date The date
   * @return a formatted string that represents a date.
   */
  public static String format(Date date) {
    SimpleDateFormat formater = new SimpleDateFormat("dd.MM.yyyy");
    return formater.format(date);
  }


  /**
   * Provides the local name of a qualified name.
   * The local name is the suffix when neglecting the namespace prefix.
   * A local name does only exist for qualified names that start with a namespace prefix.
   * @param qName The qualified name to get the local name from.
   * @return The local name of the qualified name or null, if no local name exists for it.
   */
  public static String getLocalName(String qName) {
    int len = qName.length();
    for (int i = 0; i < len; i++) {
      if (qName.charAt(i) == ':') {
        return qName.substring(i + 1);
      }
    }
    return null;
  }

  /**
   * Provides the namespace prefix of a given qualified name URI.
   * @param qualifiedName The qualified name ro get the namespace prefix from.
   * @return The namespace prefix or null, if the qualified name doesn't start with
   * a namespace prefix.
   */
  public static String getNamespacePrefix(String qualifiedName) {
    int len = qualifiedName.length();
    for (int i = 0; i < len; i++) {
      if (qualifiedName.charAt(i) == ':') {
        return qualifiedName.substring(0, i);
      }
    }
    return null;
  }

  /**
   * Translates a qName to an URI using a specified namespace mapping.
   * @param namespaces The namespace mapping
   * @param qName Qualified name to be translated. See <a href="https://www.w3.org/TR/REC-xml-names/#dt-qualname">
   *              https://www.w3.org/TR/REC-xml-names/#dt-qualname</a> for a detailed description
   * @return the URI of the qualified name.
   */
  @SuppressWarnings("SpellCheckingInspection")
  public static String getNormalizedURI(Map<String, Namespace> namespaces, String qName) {
    String prefix = RdfUtil.getNamespacePrefix(qName);
    Namespace namespace = namespaces.get(prefix);
    if (namespace != null) {
      String localName = RdfUtil.getLocalName(qName);
      return namespace.getFullURI() + localName;
    }
    else {
      return qName;
    }
  }


  /**
   * Creates an RDF triple for a given RDF resource.
   * @param factory Used to create the RDF triple.
   * @param normalizer Used to normalize IRIs of the triple.
   * @param propertyMapping The property mapping to use.
   * @param subject The resource to create the triple statement for.
   * @param value The value of the triple statement.
   * @return an RDF triple for a given RDF resource.
   */
  public static Triple triplize(RDFFactory factory,
                                QNameNormalizer normalizer,
                                PropertyMapping propertyMapping,
                                RDFResource subject,
                                String value) {

    // create property
    RDFResource property = createProperty(factory, normalizer, propertyMapping);

    // create object
    RDFObject object;
    if (propertyMapping instanceof DataTypePropertyMapping) {
      object = createLiteral(factory,
          normalizer,
          (DataTypePropertyMapping) propertyMapping,
          value);
    } else {
      object = factory.createResource(value);
    }

    return factory.createTriple(subject, property, object);
  }
}