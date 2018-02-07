package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene;

import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.IcsFile;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter.*;
import org.apache.lucene.document.Document;

import java.io.IOException;

/**
 * TODO
 */
public class IcsFileStreamToDocStream implements Stream<Stream<Document>> {

  private final Stream<IcsFile> source;

  private final IcsFileAdapter icsFileAdapter;

  private final OverlayAdapter overlayAdapter;

  private final AbnormalityAdapter abnormalityAdapter;

  private final ImageAdapter imageAdapter;

  private final CalcificationAdapter calcificationAdapter;

  private final MassAdapter massAdapter;

  public IcsFileStreamToDocStream(Stream<IcsFile> source,
                                  IcsFileAdapter icsFileAdapter,
                                  OverlayAdapter overlayAdapter,
                                  AbnormalityAdapter abnormalityAdapter,
                                  ImageAdapter imageAdapter,
                                  CalcificationAdapter calcificationAdapter,
                                  MassAdapter massAdapter) {
    this.source = source;
    this.icsFileAdapter = icsFileAdapter;
    this.overlayAdapter = overlayAdapter;
    this.abnormalityAdapter = abnormalityAdapter;
    this.imageAdapter = imageAdapter;
    this.calcificationAdapter = calcificationAdapter;
    this.massAdapter = massAdapter;
  }

  @Override
  public Stream<Document> next() throws IOException {
    IcsFile icsFile = source.next();
    return new IcsFileToDocStream(icsFile,
        icsFileAdapter,
        overlayAdapter,
        abnormalityAdapter,
        imageAdapter,
        calcificationAdapter,
        massAdapter);
  }

  @Override
  public boolean hasNext() throws IOException {
    return source.hasNext();
  }

  @Override
  public void close() throws IOException {
    source.close();
  }
}