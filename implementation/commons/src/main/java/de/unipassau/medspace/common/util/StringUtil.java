package de.unipassau.medspace.common.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * TODO
 */
public final class StringUtil {


    /**
     * TODO
     * @param line
     * @param expectedToken
     * @return
     */
    public static boolean beginsWithToken(String line, String expectedToken) {
      List<String> tokens = StringUtil.tokenize(line, " \t");
      if (tokens.size() > 0 && tokens.get(0).equals(expectedToken)) {
        return true;
      }
      return false;
    }

    /**
     * TODO
     * @param hash
     * @return
     */
  public static String bytesToHex(byte[] hash) {
    StringBuffer hexString = new StringBuffer();
    for (int i = 0; i < hash.length; i++) {
      String hex = Integer.toHexString(0xff & hash[i]);
      if(hex.length() == 1) hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }

  /**
   * TODO
   * @param list
   * @param seperator
   * @return
       * @throws IOException
   */
  public static String concat(List<String> list, String seperator) throws IOException {
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
   * TODO
   * @param source
   * @return
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
   * TODO
   * @param value
   * @param seperator
   * @return
   * @throws IOException
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