package de.unipassau.medspace.common.lucene;

import org.apache.lucene.analysis.Analyzer;

import java.io.IOException;

/**
 * TODO
 */
public interface AnalyzerBuilder {

  Analyzer build() throws IOException;
}
