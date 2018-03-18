package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.RootMapping;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class DDSM_AdapterFactory {

  /**
   * TODO
   */
  private final RootMapping rootParsing;

  private final File root;

  private final String ddsmCaseName;

  /**
   * TODO
   * @param root
   * @param rootParsing
   */
  public DDSM_AdapterFactory(File root, RootMapping rootParsing) {
    this.rootParsing = rootParsing;
    this.root = root;
    ddsmCaseName = extractCaseName(root);
  }

  /**
   * TODO
   * @return
   */
  public List<LuceneDocDdsmCaseAdapter<?>> createAdapters() {
    List<LuceneDocDdsmCaseAdapter<?>> adapters = new ArrayList<>();

    LuceneDocDdsmCaseAdapter<?> adapter = new IcsFileAdapter(rootParsing.getIcsFile(),
        rootParsing.getImage(),
        root,
        ddsmCaseName);
    adapters.add(adapter);

    adapter = new ImageAdapter(rootParsing.getImage(),
        rootParsing.getOverlay(),
        root,
        ddsmCaseName);
    adapters.add(adapter);

    adapter = new OverlayAdapter(rootParsing.getOverlay(),
        rootParsing.getAbnormality(),
        ddsmCaseName);
    adapters.add(adapter);

    adapter = new AbnormalityAdapter(rootParsing.getAbnormality(),
        rootParsing.getCalcification(),
        rootParsing.getMass(),
        ddsmCaseName);
    adapters.add(adapter);

    adapter = new CalcificationAdapter(rootParsing.getCalcification(), ddsmCaseName);
    adapters.add(adapter);

    adapter = new MassAdapter(rootParsing.getMass(), ddsmCaseName);
    adapters.add(adapter);

    return adapters;
  }

  /**
   * TODO
   * @param root
   * @return
   */
  private static String extractCaseName(File root) {
    if (!root.isDirectory()) throw new IllegalArgumentException("root is expected to be a directory!");
    return root.getName();
  }
}