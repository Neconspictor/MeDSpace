package de.unipassau.medspace.d2r;

import de.unipassau.medspace.common.SQL.SQLResultTuple;
import org.apache.log4j.Logger;

/**
 * Some utility methods used in the mapping process.
 *
 * <BR>History: 30-05-2017   : Some refactoring; made class package-private
 * <BR>History: 09-25-2003   : Changed for Jena2.
 * <BR>History: 01-15-2003   : Initial version of this class.
 * @author Chris Bizer chris@bizer.de / David Goeth goeth@fim.uni-passau.de
 * @version V0.3.1
 */
public class D2rUtil {
  private static Logger log = Logger.getLogger(D2rUtil.class);

  public static String getNamespacePrefix(String qualifiedName) {
    int len = qualifiedName.length();
    for (int i = 0; i < len; i++) {
      if (qualifiedName.charAt(i) == ':') {
        return qualifiedName.substring(0, i);
      }
    }
    return "NoPrefixFound";
  }

  public static String getLocalName(String qName) {
    int len = qName.length();
    for (int i = 0; i < len; i++) {
      if (qName.charAt(i) == ':') {
        return qName.substring(i + 1);
      }
    }
    return "NoLocalnameFound";
  }

  public static String getColumnValue(String columnName, SQLResultTuple tuple) {
    String key = D2rUtil.getFieldNameUpperCase(columnName);
    return tuple.getValue(key);
  }

  public static String getFieldNameUpperCase(String fName) {
    int len = fName.length();
    for (int i = 0; i < len; i++) {
      if (fName.charAt(i) == '.') {
        return fName.substring(i + 1).trim().toUpperCase();
      }
    }
    return fName.trim().toUpperCase();
  }

  /**
   * Parses an D2R pattern. Translates the placeholders in an D2R pattern with values from the database.
   * @param  pattern Pattern to be translated.
   * @param  deliminator Deliminator to identifiy placeholders (Standard: @@)
   * @param  tuple Hashmap with values used for replacement.
   * @return String with placeholders replaced.
   */
  public static String parsePattern(String pattern, String deliminator,
                             SQLResultTuple tuple) {
    String result = "";
    int startPosition = 0;
    int endPosition = 0;
    try {
      if (!pattern.contains(deliminator))return pattern;

      StringBuilder resultBuilder = new StringBuilder();

      while (startPosition < pattern.length()
      && pattern.indexOf(deliminator, startPosition) != -1) {

        endPosition = startPosition;
        startPosition = pattern.indexOf(deliminator, startPosition);

        // get Text
        if (endPosition < startPosition)
          resultBuilder.append(pattern.substring(endPosition, startPosition).trim());
        startPosition = startPosition + deliminator.length();
        endPosition = pattern.indexOf(deliminator, startPosition);

        // get field
        String fieldname = pattern.substring(startPosition, endPosition);
        resultBuilder.append(getColumnValue(fieldname, tuple));
        startPosition = endPosition + deliminator.length();
      }

      result = resultBuilder.toString();
      if (endPosition + deliminator.length() < pattern.length())
        result += pattern.substring(startPosition, pattern.length()).trim();
      return result;
    }
    catch (java.lang.Throwable ex) {
      log.error("Warning: There was a problem while parsing the pattern" +
                pattern + ".", ex);
      return result;
    }
  }
}