package de.unipassau.medspace.common.rdf;

/**
 * QNameNormalizer normalizes qNames to use namespace prefixes for shortening the URI of the qName.
 * For more details about qNames, see <a href="https://www.w3.org/TR/REC-xml-names/#dt-qualname">
 * https://www.w3.org/TR/REC-xml-names/#dt-qualname</a>
 */
public interface QNameNormalizer {

  /**
   * Translates a qName to an URI using the namespaces registered on thsi normalizer.
   * @param qName Qualified name to be translated. See <a href="https://www.w3.org/TR/REC-xml-names/#dt-qualname">
   *              https://www.w3.org/TR/REC-xml-names/#dt-qualname</a> for a detailed description
   * @return the URI of the qualified name.
   */
  String normalize(String qName);
}
