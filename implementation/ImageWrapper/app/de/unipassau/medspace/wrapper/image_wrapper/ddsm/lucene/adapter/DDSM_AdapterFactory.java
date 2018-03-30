package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.common.lucene.rdf.LuceneClassAdapter;
import de.unipassau.medspace.common.register.Service;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.RootMapping;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A factory for DDSM adapters.
 */
public class DDSM_AdapterFactory {


  private final RootMapping rootParsing;

  private final File root;

  private final String fileDownloadService;

  /**
   * Creates a new DDSM_AdapterFactory object.
   * @param root The root directory of the DDSM cases.
   * @param rootMapping The mapping for the root configuration.
   * @param fileDownloadService The file donwload service base URL
   */
  public DDSM_AdapterFactory(File root, RootMapping rootMapping, String fileDownloadService) {
    this.rootParsing = rootMapping;
    this.root = root;
    this.fileDownloadService = fileDownloadService;
  }

  /**
   * Creates the list of DDSM adapters.
   * @return the list of DDSM adapters.
   */
  public List<LuceneClassAdapter<?>> createAdapters() {
    List<LuceneClassAdapter<?>> adapters = new ArrayList<>();

    DDSM_CaseAdapter<?> adapter = new IcsFileAdapter(rootParsing.getIcsFile(),
        rootParsing.getImage(),
        root,
        fileDownloadService);
    adapters.add(adapter);

    adapter = new ImageAdapter(rootParsing.getImage(),
        rootParsing.getOverlay(),
        root,
        fileDownloadService);
    adapters.add(adapter);

    adapter = new OverlayAdapter(rootParsing.getOverlay(),
        rootParsing.getAbnormality());
    adapters.add(adapter);

    adapter = new AbnormalityAdapter(rootParsing.getAbnormality(),
        rootParsing.getCalcification(),
        rootParsing.getMass());
    adapters.add(adapter);

    adapter = new CalcificationAdapter(rootParsing.getCalcification());
    adapters.add(adapter);

    adapter = new MassAdapter(rootParsing.getMass());
    adapters.add(adapter);

    return adapters;
  }
}