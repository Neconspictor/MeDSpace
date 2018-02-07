package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.IcsFileParsing;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.ImageParsing;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.Property;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.IcsFile;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.IOException;

/**
 * TODO
 */
public class IcsFileAdapter extends LuceneDocAdapter<IcsFile> {


  /**
   * TODO
   */
  private final IcsFileParsing icsFileParsing;

  private final ImageParsing imageParsing;

  /**
   * TODO
   * @param icsFileParsing
   * @param imageParsing
   */
  public IcsFileAdapter(IcsFileParsing icsFileParsing, ImageParsing imageParsing) {

    super(icsFileParsing);
    this.icsFileParsing = icsFileParsing;
    this.imageParsing = imageParsing;

    addPair(IcsFile.DATE_OF_STUDY, icsFileParsing.getDateOfStudy());
    addPair(IcsFile.PATIENT_AGE, icsFileParsing.getPatientAge());
    addPair(IcsFile.DENSITY, icsFileParsing.getDensity());
    addPair(IcsFile.DATE_DIGITIZED, icsFileParsing.getDateDigitized());
    addPair(IcsFile.DIGITIZER, icsFileParsing.getDigitizer());
    addPair(IcsFile.LEFT_CC, icsFileParsing.getLeftCc());
    addPair(IcsFile.LEFT_MLO, icsFileParsing.getLeftMlo());
    addPair(IcsFile.RIGHT_CC, icsFileParsing.getRightCc());
    addPair(IcsFile.RIGHT_MLO, icsFileParsing.getRightMlo());

  }

  @Override
  protected void addFields(IcsFile source, Document doc) throws IOException {
    doc.add(createField(IcsFile.DATE_OF_STUDY, source.getDateOfStudy()));
    doc.add(createField(IcsFile.PATIENT_AGE, source.getPatientAge()));
    doc.add(createField(IcsFile.DENSITY, source.getDensity()));
    doc.add(createField(IcsFile.DATE_DIGITIZED, source.getDateDigitized()));
    doc.add(createField(IcsFile.DIGITIZER, source.getDigitizer()));

    doc.add(createField(IcsFile.LEFT_CC, source.getLeftCC().getId()));
    doc.add(createField(IcsFile.LEFT_MLO, source.getLeftMLO().getId()));
    doc.add(createField(IcsFile.RIGHT_CC, source.getRightCC().getId()));
    doc.add(createField(IcsFile.RIGHT_MLO, source.getRightMLO().getId()));
  }

  @Override
  public String createValue(Pair<String, Property> pair,IndexableField field) {

    Property property = pair.getValue1();
    String value = field.stringValue();

    if (isImageReference(property)) {
      value = imageParsing.getRdfType() + "#" + value;
    }

    return value;
  }

  /**
   * TODO
   * @param property
   * @return
   */
  private boolean isImageReference(Property property) {
    if (property.equals(icsFileParsing.getLeftCc())) return true;
    if (property.equals(icsFileParsing.getLeftMlo())) return true;
    if (property.equals(icsFileParsing.getRightCc())) return true;
    if (property.equals(icsFileParsing.getRightMlo())) return true;

    return false;
  }
}