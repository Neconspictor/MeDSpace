package de.unipassau.medspace.common.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;

import java.io.IOException;

/**
 * TODO
 */
public class LuceneUtil {

  /**
   * Creates an {@link Analyzer} used for indexing and searching.
   * @return An {@link Analyzer} used for indexing and searching.
   * @throws IOException If an IO-Error occurs.
   */
  public static Analyzer buildAnalyzer() throws IOException {
    return CustomAnalyzer.builder()
        .withTokenizer("whitespace")
        .addTokenFilter("lowercase")
        .addTokenFilter("standard")
        .build();
  }
}