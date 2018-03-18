package de.unipassau.medspace.common.util;

import de.unipassau.medspace.common.rdf.*;
import de.unipassau.medspace.common.rdf.mapping.DataTypePropertyMapping;
import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * TODO
 */
public class RdfUtil {

  /**
   * TODO
   * @param value
   * @param propertyParsing
   * @return
   */
  public static RDFObject createLiteral(RDFFactory factory,
                                        QNameNormalizer normalizer,
                                        DataTypePropertyMapping propertyParsing,
                                        String value) {

    String dataType = propertyParsing.getDataType();
    String lang = propertyParsing.getLang();
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
   * TODO
   * @param propertyParsing
   * @return
   */
  public static RDFResource createProperty(RDFFactory factory,
                                           QNameNormalizer normalizer,
                                           PropertyMapping propertyParsing) {
    String propertyQName = propertyParsing.getPropertyType();
    String propURI = normalizer.normalize(propertyQName);
    return factory.createResource(propURI);
  }

  /**
   * TODO
   *
   * @param normalizer
   * @param baseURI
   * @param id
   * @return
   * @throws IllegalArgumentException
   */
  public static String createResourceId(QNameNormalizer normalizer, String baseURI, String id) {
    String subject = baseURI + "#" + id;
    return normalizer.normalize(subject);
  }

  /**
   * TODO
   * @param date
   * @return
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
   * TODO
   * @param propertyParsing
   * @param subject
   * @param value
   * @return
   */
  public static Triple triplize(RDFFactory factory,
                                QNameNormalizer normalizer,
                                PropertyMapping propertyParsing,
                                RDFResource subject,
                                String value) {

    // create property
    RDFResource property = createProperty(factory, normalizer, propertyParsing);

    // create object
    RDFObject object;
    if (propertyParsing instanceof DataTypePropertyMapping) {
      object = createLiteral(factory,
          normalizer,
          (DataTypePropertyMapping) propertyParsing,
          value);
    } else {
      object = factory.createResource(value);
    }

    return factory.createTriple(subject, property, object);
  }
}