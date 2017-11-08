package de.unipassau.medspace.common.rdf.rdf4j;

import de.unipassau.medspace.common.exception.NoValidArgumentException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParserRegistry;

import java.util.*;

/**
 * Created by David Goeth on 29.10.2017.
 */
public class RDF4JLanguageFormats {

  public static final Set<RDFFormat> formats = getFormats();

  public static final Map<String, RDFFormat> mappedFormats = getMappedFormats();

  public static final String formatsPrettyPrint = constructPrettyPrint();

  public static RDFFormat getFormatFromString(String format) throws NoValidArgumentException {
    RDFFormat result = getFormatNoException(format);

    if (result == null) {
      throw new NoValidArgumentException("Unknown language format: " + format);
    }
    return result;
  }

  public static String getFormatsPrettyPrint() {
    return formatsPrettyPrint;
  }

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