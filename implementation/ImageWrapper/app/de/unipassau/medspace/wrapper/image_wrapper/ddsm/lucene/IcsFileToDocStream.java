package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene;

import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.Abnormality;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.Image;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.IcsFile;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.OverlayMetaData;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.Calcification;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.LesionType;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lesion.Mass;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter.*;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A stream that converts an ICS file to a document stream.
 */
public class IcsFileToDocStream implements Stream<Document> {

  private final IcsFile icsFile;

  private final IcsFileAdapter icsFileAdapter;

  private final OverlayAdapter overlayAdapter;

  private final AbnormalityAdapter abnormalityAdapter;

  private final ImageAdapter imageAdapter;

  private final CalcificationAdapter calcificationAdapter;

  private final MassAdapter massAdapter;

  private List<Document> documents;

  /**
   * Creates a new IcsFileToDocStream object.
   *
   * @param icsFile The ICS file.
   * @param icsFileAdapter An ICS file adapter.
   * @param overlayAdapter An overlay adapter.
   * @param abnormalityAdapter An overlay adapter.
   * @param imageAdapter An image adapter.
   * @param calcificationAdapter A calcification adapter.
   * @param massAdapter A mass adapter.
   * @throws IOException If an IO error occurs.
   */
  public IcsFileToDocStream(IcsFile icsFile,
                     IcsFileAdapter icsFileAdapter,
                     OverlayAdapter overlayAdapter,
                     AbnormalityAdapter abnormalityAdapter,
                     ImageAdapter imageAdapter,
                     CalcificationAdapter calcificationAdapter,
                     MassAdapter massAdapter) throws IOException {

    this.icsFile = icsFile;
    this.icsFileAdapter = icsFileAdapter;
    this.overlayAdapter = overlayAdapter;
    this.abnormalityAdapter = abnormalityAdapter;
    this.imageAdapter = imageAdapter;
    this.calcificationAdapter = calcificationAdapter;
    this.massAdapter = massAdapter;
    this.documents = new ArrayList<>();
    createDocs(icsFile);
  }

  private void createDocs(IcsFile icsFile) throws IOException {

    //icsFile
    Document doc = icsFileAdapter.convert(icsFile);
    documents.add(doc);

    //images
    createDocs(icsFile.getLeftCC());
    createDocs(icsFile.getLeftMLO());
    createDocs(icsFile.getRightCC());
    createDocs(icsFile.getRightMLO());
  }

  private void createDocs(Image image) throws IOException {
    Document doc = imageAdapter.convert(image);
    documents.add(doc);

    OverlayMetaData overlay = image.getOverlay();
    if (overlay!= null) {
      createDocs(overlay);
    }
  }

  private void createDocs(OverlayMetaData overlay) throws IOException {
    Document doc = overlayAdapter.convert(overlay);
    documents.add(doc);

    for (Abnormality abnormality : overlay.getAbnormalities()) {
      createDocs(abnormality);
    }
  }

  private void createDocs(Abnormality abnormality) throws IOException {
    Document doc = abnormalityAdapter.convert(abnormality);
    documents.add(doc);

    for (LesionType lesionType : abnormality.getLesionTypes()) {
      createDocs(lesionType);
    }
  }

  private void createDocs(LesionType lesionType) throws IOException {
    Document doc;
    if (lesionType instanceof Calcification) {
      doc = calcificationAdapter.convert((Calcification) lesionType);
    } else {
      doc = massAdapter.convert((Mass) lesionType);
    }

    documents.add(doc);
  }

  @Override
  public Document next() throws IOException {
    if (!hasNext()) throw new IOException("No next element available!");
    return documents.remove(0);
  }

  @Override
  public boolean hasNext() throws IOException {
    return documents.size() > 0;
  }

  @Override
  public void close() throws IOException {
    // Nothing to do
  }
}