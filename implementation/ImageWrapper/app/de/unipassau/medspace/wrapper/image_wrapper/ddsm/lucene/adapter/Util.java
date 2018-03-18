package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * TODO
 */
public class Util {

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
   * @param root
   * @param subFile
   * @return
   * @throws IOException
   */
  public static String getRelativePath(File root, File subFile)  {

    if (!root.isDirectory())
      throw new IllegalArgumentException("root isn't a directory!");

    String canonicalRoot = makePlatformIndependentPathStructure(root);
    String canonicalSubFile = makePlatformIndependentPathStructure(subFile);

    if (!canonicalSubFile.startsWith(canonicalRoot)) {
      throw new IllegalArgumentException("File to get the relative path from isn't a sub file from root!");
    }

    int length = canonicalRoot.length();

    // we create a substring from canonicalSubFile by subtracting canonicalRoot
    // But we have to assure that the resulting string doesn't start with a path separator.
    if (!canonicalRoot.endsWith("/")) {
      ++length;
    }
    return canonicalSubFile.substring(length, canonicalSubFile.length());
  }

  public static String makePlatformIndependentPathStructure(File file) {
    String canonicalRoot = file.getAbsolutePath();

    //use only '/' as it is supported by all platforms
    return canonicalRoot.replaceAll("\\\\", "/");
  }

  /**
   * TODO
   * @param value
   * @param seperator
   * @return
   * @throws IOException
   */
  public static List<String> tokenize(String value, String seperator) throws IOException {

    List<String> result = new ArrayList<>();
    StringTokenizer tokenizer = new StringTokenizer(value, seperator);
    while(tokenizer.hasMoreTokens()) {
      result.add(tokenizer.nextToken());
    }
    return result;
  }

}