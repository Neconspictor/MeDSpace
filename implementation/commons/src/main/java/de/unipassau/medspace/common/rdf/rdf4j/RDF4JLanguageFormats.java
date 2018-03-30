package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.exception.NoValidArgumentException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParserRegistry;

import java.util.*;

/**
 * Holds language formats supported by RDF4J.
 */
public class RDF4JLanguageFormats {

  /**
   * The supported language formats.
   */
  public static final Set<RDFFormat> formats = getFormats();

  /**
   * Map of names to supported RDF formats.
   */
  public static final Map<String, RDFFormat> mappedFormats = getMappedFormats();

  /**
   * A formatted string that lists all supported language formats that can be used for printing.
   */
  public static final String formatsPrettyPrint = constructPrettyPrint();

  /**
   * Provides a language format from a string.
   * @param format A string that is a key for the language format.
   * @return The language format.
   * @throws NoValidArgumentException If the language format couldn't be found.
   */
  public static RDFFormat getFormatFromString(String format) throws NoValidArgumentException {
    RDFFormat result = getFormatNoException(format);

    if (result == null) {
      throw new NoValidArgumentException("Unknown language format: " + format);
    }
    return result;
  }

  /**
   * Provides a formatted string that lists all supported language formats that can be used for printing.
   * @return a formatted string that lists all supported language formats that can be used for printing.
   */
  public static String getFormatsPrettyPrint() {
    return formatsPrettyPrint;
  }

  /**
   * Checks if a given format is supported.
   * @param format The format to check.
   * @return true if the format is supported.
   */
  public static boolean isValidFormat(String format) {
    return getFormatNoException(format) != null;
  }

  private static String constructPrettyPrint() {
    StringBuilder builder = new StringBuilder();
    String nextToken = ",\n";

    for (RDFFormat format : formats) {
      builder.append(format.getName());
      builder.append(nextToken);
    }

    if (builder.length() > 0)
      builder.delete(builder.length() - nextToken.length(), builder.length() );

    return builder.toString();
  }


  private static Set<RDFFormat> getFormats() {
    Set<RDFFormat> formats =  RDFParserRegistry.getInstance().getKeys();
    return Collections.unmodifiableSet(formats);
  }

  private static Map<String, RDFFormat> getMappedFormats() {
    Set<RDFFormat> formats  = RDFParserRegistry.getInstance().getKeys();
    HashMap<String, RDFFormat> map = new HashMap<>();
    for (RDFFormat format : formats) {
      String name = format.getName().toUpperCase();
      map.put(name, format);
    }

    return Collections.unmodifiableMap(map);
  }

  private static RDFFormat getFormatNoException(String format) {
    final String uppercase = format.toUpperCase();
    return mappedFormats.get(uppercase);
  }
}