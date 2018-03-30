package de.unipassau.medspace.common.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Utility methods useful when working with strings.
 */
public final class StringUtil {


    /**
     * Checks if a given string starts with an expected token.
     * @param line The string.
     * @param expectedToken The expected token.
     * @return true if the string starts with the token, otherwise false.
     */
    public static boolean beginsWithToken(String line, String expectedToken) {
      List<String> tokens = StringUtil.tokenize(line, " \t");
      if (tokens.size() > 0 && tokens.get(0).equals(expectedToken)) {
        return true;
      }
      return false;
    }

    /**
     * Provides a hexadecimal string representation of an byte array.
     * @param array The byte array.
     * @return a hexadecimal string representation of the byte array.
     */
  public static String bytesToHex(byte[] array) {
    StringBuffer hexString = new StringBuffer();
    for (int i = 0; i < array.length; i++) {
      String hex = Integer.toHexString(0xff & array[i]);
      if(hex.length() == 1) hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }

  /**
   * Concates a list of strings and separates the elements with a given separator token.
   * @param list The list of strings
   * @param seperator The separator token
   * @return A concazenated string.
   */
  public static String concat(List<String> list, String seperator) {
    StringBuilder builder = new StringBuilder();

    for (String elem : list) {
      builder.append(elem);
      builder.append(seperator);
    }

    if (builder.length() > 0)
      builder.delete(builder.length() - seperator.length(), builder.length());

    return builder.toString();
  }

  /**
   * Encodes a string with SHA-256.
   * @param source The string to encode.
   * @return An encoded string.
   */
  public static String encodeString(String source) {

    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Couldn't instantiate SHA-256 algorithm", e);
    }

    byte[] encodedHash = digest.digest(
        source.getBytes(StandardCharsets.UTF_8));

    return bytesToHex(encodedHash);
  }

  /**
   * tokenizes a string with a given separator.
   * @param value The string to tokenize.
   * @param seperator The separator token to use.
   * @return The tokenized string.
   */
  public static List<String> tokenize(String value, String seperator) {

    List<String> result = new ArrayList<>();
    StringTokenizer tokenizer = new StringTokenizer(value, seperator);
    while(tokenizer.hasMoreTokens()) {
      result.add(tokenizer.nextToken());
    }
    return result;
  }
}