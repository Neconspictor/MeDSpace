package de.unipassau.medspace.wrapper.pdf_wrapper.pdf;

import java.io.*;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * TODO
 */
public class Util {

  /**
   * TODO
   * @param line
   * @return
   */
  public static List<String> tokenize(String line) {
    StringTokenizer tokenizer = new StringTokenizer(line, " \t"); //tokens are separated by white-spaces
    List<String> tokens = new LinkedList<>();
    while(tokenizer.hasMoreTokens()) {
      tokens.add(tokenizer.nextToken().toUpperCase());
    }

    return tokens;
  }

  /**
   * TODO
   * @param file
   * @return
   * @throws IOException
   */
  public static List<String> getLineContent(File file) throws IOException {
    List<String> result = new LinkedList<>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    try {
      String line = reader.readLine();
      while(line != null) {
        result.add(line);
        line = reader.readLine();
      }
    } finally {
      reader.close();
    }

    return result;
  }

  /**
   * TODO
   * @param tokens
   * @return
   * @throws IOException
   */
  public static Date parseDateField(List<String> tokens) throws IOException {
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
   * @return
   * @throws IOException
   */
  public static int parseInt(List<String> tokens) throws IOException {
    // we need one token
    if (tokens.size() != 1)
      throw new IOException("Expected one token for parsing a int field!");

    try {
      return Integer.parseInt(tokens.get(0));
    }catch (NumberFormatException e) {
      throw new IOException(e);
    }
  }

  /**
   * TODO
   * @param tokens
   * @return
   */
  public static String parseText(List<String> tokens) {
    StringBuilder builder = new StringBuilder();
    for (String token : tokens) {
      builder.append(token + " ");
    }

    // delete last " " character
    if (tokens.size() > 0) {
      builder.delete(builder.length() - 1, builder.length());
    }

    return builder.toString();
  }

  /**
   * TODO
   * @param tokens
   * @param expectedToken
   * @throws IOException
   */
  public static void parseExpectedToken(List<String> tokens, String expectedToken) throws IOException {
    if (!tokens.remove(0).equals(expectedToken)) {
      throw new IOException("Expected " + expectedToken + " token");
    }
  }

  /**
   * TODO
   * @param line
   * @param expectedToken
   * @return
   */
  public static boolean beginsWithToken(String line, String expectedToken) {
    List<String> tokens = tokenize(line);
    if (tokens.size() > 0 && tokens.get(0).equals(expectedToken)) {
      return true;
    }
    return false;
  }

  /**
   * TODO
   * @param source
   * @param destination
   * @return
   */
  public static String createRelativePath(File source, File destination) {
    Path relative = source.toPath().relativize(destination.toPath());
    String result = relative.toString();
    return result.replaceAll("\\\\", "/");
  }

  /**
   * TODO
   * @param collection
   * @param clazz
   * @param <T>
   * @return
   */
  public static <T> T getByClass(Collection<?> collection, Class<T> clazz) {
    for (Object elem : collection) {
      if (clazz.isInstance(elem)) return (T) elem;
    }
    return null;
  }
}