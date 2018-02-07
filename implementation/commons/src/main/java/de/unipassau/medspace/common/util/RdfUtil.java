package de.unipassau.medspace.common.util;

import de.unipassau.medspace.common.rdf.Namespace;
import java.util.Map;

/**
 * TODO
 */
public class RdfUtil {


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
}