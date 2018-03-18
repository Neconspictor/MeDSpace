package de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.adapter;


import de.unipassau.medspace.wrapper.pdf_wrapper.config.mapping.RootMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class Pdf_AdapterFactory {

  /**
   * TODO
   */
  private final RootMapping rootParsing;

  /**
   * TODO
   */
  private final String downloadService;

  /**
   * TODO
   * @param rootParsing
   */
  public Pdf_AdapterFactory(RootMapping rootParsing, String downloadService) {
    this.rootParsing = rootParsing;
    this.downloadService = downloadService;
  }

  /**
   * TODO
   * @return
   */
  public List<LucenePdfFileDocAdapter<?>> createAdapters() {
    List<LucenePdfFileDocAdapter<?>> adapters = new ArrayList<>();

    LucenePdfFileDocAdapter<?> adapter = new PdfFileAdapter(rootParsing.getPdfFile(), downloadService);
    adapters.add(adapter);

    return adapters;
  }
}