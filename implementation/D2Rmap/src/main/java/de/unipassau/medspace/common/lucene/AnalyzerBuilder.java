package de.unipassau.medspace.common.lucene;

import org.apache.lucene.analysis.Analyzer;

import java.io.IOException;

/**
 * An analyzer builder is a factory for creating an {@link Analyzer}.
 */
public interface AnalyzerBuilder {

  /**
   * Builds a new {@link Analyzer}
   * @return a new {@link Analyzer}
   * @throws IOException if an IO-Error occurs.
   */
  Analyzer build() throws IOException;
}
