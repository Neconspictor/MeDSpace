package de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.adapter;


import de.unipassau.medspace.wrapper.pdf_wrapper.config.mapping.RootMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter factory for PDF files.
 */
public class Pdf_AdapterFactory {


  private final RootMapping rootParsing;

  private final String downloadService;


  /**
   * Creates a new Pdf_AdapterFactory object.
   * @param rootMapping The root mapping.
   * @param downloadService the base URL of the file download service.
   */
  public Pdf_AdapterFactory(RootMapping rootMapping, String downloadService) {
    this.rootParsing = rootMapping;
    this.downloadService = downloadService;
  }

  /**
   * Creates the adapter list.
   * @return the created adapter list.
   */
  public List<PdfFileAdapter> createAdapters() {
    List<PdfFileAdapter> adapters = new ArrayList<>();

    PdfFileAdapter adapter = new PdfFileAdapter(rootParsing.getPdfFile(), downloadService);
    adapters.add(adapter);

    return adapters;
  }
}