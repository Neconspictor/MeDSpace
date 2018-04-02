package de.unipassau.medspace.common.util;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Utilities for parsing file content.
 */
public class ParserUtil {

  /**
   * Pulls the first element of a token list and converts it to a date object.
   * @param tokens The token list
   * @return The created date object
   * @throws IOException If no date object could be created.
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
   * Pulls the first element of a token list and compares it with a given token. If the token doesn't match the
   * expected token an io error is thrown.
   * @param tokens The token list
   * @param expectedToken The expected token.
   * @throws IOException If the token doesn't match the expected token
   */
  public static void pullExpectedToken(List<String> tokens, String expectedToken) throws IOException {
    if (!tokens.remove(0).equals(expectedToken)) {
      throw new IOException("Expected " + expectedToken + " token");
    }
  }

  /**
   * Pulls the first element of a token list and converts it to an integer.
   * @param tokens The token list
   * @return The parsed integer.
   * @throws IOException If the token cannot be converted to an integer.
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