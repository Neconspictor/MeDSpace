package de.unipassau.medspace.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by David Goeth on 14.11.2017.
 */
public final class StringUtil {

  public static String bytesToHex(byte[] hash) {
    StringBuffer hexString = new StringBuffer();
    for (int i = 0; i < hash.length; i++) {
      String hex = Integer.toHexString(0xff & hash[i]);
      if(hex.length() == 1) hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }

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
}