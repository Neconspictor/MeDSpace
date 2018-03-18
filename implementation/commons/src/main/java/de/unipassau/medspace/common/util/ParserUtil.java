package de.unipassau.medspace.common.util;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * TODO
 */
public class ParserUtil {

  /**
   * TODO
   * @param tokens
   * @return
   * @throws IOException
   */
  public static Date pullDateField(List<String> tokens) throws IOException {
    // we need three tokens (Day, Month, Year)
    // some ics files have more than 3 tokens on a date field (token SEQUENCE), but that is not important
    if (tokens.size() < 3)
      throw new IOException("Expected three tokens to parse a date field!");

    String days = tokens.get(0);
    String month = tokens.get(1);
    String year = tokens.get(2);

    String source = days + " " + month + " " + year;

    SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
    try {
      return sdf.parse(source);
    } catch (ParseException e) {
      throw new IOException("Couldn't parse date field; cause: ", e);
    }
  }

  /**
   * TODO
   * @param tokens
   * @param expectedToken
   * @throws IOException
   */
  public static void pullExpectedToken(List<String> tokens, String expectedToken) throws IOException {
    if (!tokens.remove(0).equals(expectedToken)) {
      throw new IOException("Expected " + expectedToken + " token");
    }
  }

  /**
   * TODO
   * @param tokens
   * @return
   * @throws IOException
   */
  public static int pullInt(List<String> tokens) throws IOException {
    // we need one token
    if (tokens.size() != 1)
      throw new IOException("Expected one token for parsing a int field!");

    try {
      return Integer.parseInt(tokens.get(0));
    }catch (NumberFormatException e) {
      throw new IOException(e);
    }
  }
}