package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter;

import de.unipassau.medspace.common.lucene.rdf.LuceneDocFileAdapter;
import de.unipassau.medspace.common.rdf.mapping.IdentifiableFile;
import de.unipassau.medspace.common.rdf.mapping.PropertyMapping;
import de.unipassau.medspace.common.register.Service;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.IcsFileMapping;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.ImageMapping;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.IcsFile;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.javatuples.Pair;

import java.io.File;
import java.io.IOException;

/**
 * A DDSM adapter for a ICS file.
 */
public class IcsFileAdapter extends DDSM_CaseAdapter<IcsFile> {

  private static final String ICSFILE_FOLDER_STRUCTURE_METADATA = "ICSFILE_FOLDER_STRUCTURE_METADATA";


  private final IcsFileMapping icsFileParsing;

  private final ImageMapping imageParsing;

  /**
   * Creates a new IcsFileAdapter object.
   *
   * @param icsFileMapping The mapping for an ICS file.
   * @param imageMapping The mapping for an image.
   * @param root The DDSM root folder.
   */
  public IcsFileAdapter(IcsFileMapping icsFileMapping,
                        ImageMapping imageMapping,
                        File root,
                        String downloadService) {

    super(icsFileMapping,
        new LuceneDocFileAdapter<IdentifiableFile>(icsFileMapping,
            root,
            downloadService,
            null));

    this.icsFileParsing = icsFileMapping;
    this.imageParsing = imageMapping;

    addPair(IcsFile.DATE_OF_STUDY, icsFileMapping.getDateOfStudy());
    addPair(IcsFile.PATIENT_AGE, icsFileMapping.getPatientAge());
    addPair(IcsFile.DENSITY, icsFileMapping.getDensity());
    addPair(IcsFile.DATE_DIGITIZED, icsFileMapping.getDateDigitized());
    addPair(IcsFile.DIGITIZER, icsFileMapping.getDigitizer());
    addPair(IcsFile.LEFT_CC, icsFileMapping.getLeftCc());
    addPair(IcsFile.LEFT_MLO, icsFileMapping.getLeftMlo());
    addPair(IcsFile.RIGHT_CC, icsFileMapping.getRightCc());
    addPair(IcsFile.RIGHT_MLO, icsFileMapping.getRightMlo());

    this.metaDataFields.add(ICSFILE_FOLDER_STRUCTURE_METADATA);
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
  protected String getValue(Pair<String, PropertyMapping> pair, IndexableField field) {
    PropertyMapping property = pair.getValue1();

    if (isImageReference(property)) {
     return imageParsing.getRdfType() + "#" + field.stringValue();
    }

    return null;
  }

  private boolean isImageReference(PropertyMapping property) {
    if (property.equals(icsFileParsing.getLeftCc())) return true;
    if (property.equals(icsFileParsing.getLeftMlo())) return true;
    if (property.equals(icsFileParsing.getRightCc())) return true;
    if (property.equals(icsFileParsing.getRightMlo())) return true;

    return false;
  }
}