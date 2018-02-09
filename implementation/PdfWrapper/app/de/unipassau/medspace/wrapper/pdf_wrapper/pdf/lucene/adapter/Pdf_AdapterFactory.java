package de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.adapter;


import de.unipassau.medspace.wrapper.pdf_wrapper.config.parsing.RootParsing;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class Pdf_AdapterFactory {

  /**
   * TODO
   */
  private final RootParsing rootParsing;

  /**
   * TODO
   * @param rootParsing
   */
  public Pdf_AdapterFactory(RootParsing rootParsing) {
    this.rootParsing = rootParsing;
  }

  /**
   * TODO
   * @return
   */
  public List<LuceneDocAdapter<?>> createAdapters() {
    List<LuceneDocAdapter<?>> adapters = new ArrayList<>();

    LuceneDocAdapter<?> adapter = new PdfFileAdapter(rootParsing.getPdfFile());
    adapters.add(adapter);

    return adapters;
  }
}