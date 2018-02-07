package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.RootParsing;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class DDSM_AdapterFactory {

  /**
   * TODO
   */
  private final RootParsing rootParsing;

  /**
   * TODO
   * @param rootParsing
   */
  public DDSM_AdapterFactory(RootParsing rootParsing) {
    this.rootParsing = rootParsing;
  }

  /**
   * TODO
   * @return
   */
  public List<LuceneDocAdapter<?>> createAdapters() {
    List<LuceneDocAdapter<?>> adapters = new ArrayList<>();

    LuceneDocAdapter<?> adapter = new IcsFileAdapter(rootParsing.getIcsFile(), rootParsing.getImage());
    adapters.add(adapter);

    adapter = new ImageAdapter(rootParsing.getImage(), rootParsing.getOverlay());
    adapters.add(adapter);

    adapter = new OverlayAdapter(rootParsing.getOverlay(), rootParsing.getAbnormality());
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