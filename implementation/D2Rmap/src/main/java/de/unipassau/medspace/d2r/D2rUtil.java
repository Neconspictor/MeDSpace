package de.unipassau.medspace.d2r;

import de.unipassau.medspace.common.SQL.SQLResultTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some utility methods used in the mapping process.
 */
public class D2rUtil {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(D2rUtil.class);

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
   * Provides the value of a column from a given sql tuple.
   * @param columnName The column to get the value from.
   * @param tuple The sql tuple.
   * @return The value of the column or null, if the sql tuple doesn't have the specified column.
   */
  public static String getColumnValue(String columnName, SQLResultTuple tuple) {
    String key = D2rUtil.getColumnNameUpperCase(columnName);
    return tuple.getValue(key);
  }

  /**
   * Provides the column name in upper case characters without schema specifiers from a sql column field.
   * @param field The field of a sql query, that describes a column
   * @return The column name without schema specifiers and in upper case.
   */
  public static String getColumnNameUpperCase(String field) {
    int len = field.length();
    for (int i = 0; i < len; i++) {
      if (field.charAt(i) == '.') {
        return field.substring(i + 1).trim().toUpperCase();
      }
    }
    return field.trim().toUpperCase();
  }

  /**
   * Parses an D2R pattern. Translates the placeholders in an D2R pattern with values from the database.
   * @param  pattern Pattern to be translated.
   * @param  deliminator Deliminator to identify placeholders (Standard: @@)
   * @param  tuple  The sql tuple to get data needed for placeholder replacement.
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