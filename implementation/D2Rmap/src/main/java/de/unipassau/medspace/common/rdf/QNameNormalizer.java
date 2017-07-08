package de.unipassau.medspace.common.rdf;

import java.util.List;

/**
 * QNameNormalizer normailzes qNames to use namespace prefixes for shortening the URI of the qName.
 * For more details about qNames, see <a href="https://www.w3.org/TR/REC-xml-names/#dt-qualname">
 * https://www.w3.org/TR/REC-xml-names/#dt-qualname</a>
 */
public interface QNameNormalizer {


  /**
   * Provides a registered namespace by its prefix name
   *
   * @param prefix the prefix name to search the namespace
   * @return The matched Namespace or null if no Namespace
   *         with the specified prefix was found.
   */
  Namespace getNamespaceByPrefix(String prefix);

  /**
   * Provides a list of namespaces that are used to translate the uri of
   * a rdf triple.
   *
   * @return A list of namespaces used by this QNameNormalizer.
   */
  List<Namespace> getNamespaces();

  /**
   * Translates a qName to an URI using the namespaces registered on thsi normalizer.
   * @param qName Qualified name to be translated. See <a href="https://www.w3.org/TR/REC-xml-names/#dt-qualname">
   *              https://www.w3.org/TR/REC-xml-names/#dt-qualname</a> for a detailed description
   * @return the URI of the qualified name.
   */
  String normalize(String qName);
}
