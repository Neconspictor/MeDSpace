package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene;

import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.util.Converter;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.IcsFileParsing;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.Property;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.IcsFile;
import de.unipassau.medspace.wrapper.image_wrapper.rdf_mapping.Util;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 */
public class IcsFileDocAdapter implements DocumentAdapter<IcsFile> {

  /**
   * TODO
   */
  private static final String ID_FIELD = "ID";

  /**
   * TODO
   */
  private final String classIdDescription;

  /**
   * TODO
   */
  private final String id;

  /**
   * TODO
   */
  private final Converter<IcsFile, Document> toDoc;


  /**
   * TODO
   */
  private final IcsFileParsing icsFileParsing;


  /**
   * TODO
   * @param classIdDescription
   * @param id
   * @param icsFileParsing
   */
  public IcsFileDocAdapter(String classIdDescription,
                           String id,
                           IcsFileParsing icsFileParsing) {

    this.classIdDescription = classIdDescription;
    this.id = id;
    this.icsFileParsing = icsFileParsing;

    toDoc = (source)->convert(source);
  }

  /**
   * TODO
   * @param source The object to convert
   * @return
   * @throws IOException
   */
  public Document convert(IcsFile source) throws IOException {
    Document document = new Document();

    document.add(createField(IcsFile.DATE_OF_STUDY, Util.format(source.getDateOfStudy())));
    document.add(createField(IcsFile.PATIENT_AGE, String.valueOf(source.getPatientAge())));
    document.add(createField(IcsFile.DENSITY, String.valueOf(source.getDensity())));
    document.add(createField(IcsFile.DATE_DIGITIZED, Util.format(source.getDateDigitized())));
    document.add(createField(IcsFile.DIGITIZER, source.getDigitizer()));

    document.add(createField(IcsFile.LEFT_CC, source.getLeftCC().getId()));
    document.add(createField(IcsFile.LEFT_MLO, source.getLeftMLO().getId()));
    document.add(createField(IcsFile.RIGHT_CC, source.getRightCC().getId()));
    document.add(createField(IcsFile.RIGHT_MLO, source.getRightMLO().getId()));

    //assign it the id
    document.add(new StringField(classIdDescription, id, Field.Store.YES));
    document.add(new StringField(ID_FIELD, source.getId(), Field.Store.YES));

    return document;
  }


  @Override
  public boolean isConvertable(Document document) {
    IndexableField field = document.getField(classIdDescription);
    if (field == null) return false;
    return field.stringValue().equals(id);
  }

  @Override
  public List<IndexableField> getValidFields(Document document) {

    List<IndexableField> fields = new ArrayList<>();
    List<IndexableField> source = document.getFields();
    for (IndexableField field : source) {
      if (isValid(field)) {
        fields.add(field);
      }
    }
    return fields;
  }

  @Override
  public Converter getClassToDocumentConverter() {
    return toDoc;
  }

  @Override
  public String getClassId(Document document) {
    return document.get(ID_FIELD);
  }

  @Override
  public Property getPropertyByFieldName(String fieldName) {
    switch(fieldName) {
      case IcsFile.DATE_OF_STUDY:
        return icsFileParsing.getDateOfStudy();
      case IcsFile.PATIENT_AGE:
        return icsFileParsing.getPatientAge();
      case IcsFile.DENSITY:
        return icsFileParsing.getDensity();
      case IcsFile.DATE_DIGITIZED:
        return icsFileParsing.getDateDigitized();
      case IcsFile.DIGITIZER:
        return icsFileParsing.getDigitizer();
      case IcsFile.LEFT_CC:
        return icsFileParsing.getLeftCc();
      case IcsFile.LEFT_MLO:
        return icsFileParsing.getLeftMlo();
      case IcsFile.RIGHT_CC:
        return icsFileParsing.getRightCc();
      case IcsFile.RIGHT_MLO:
        return icsFileParsing.getRightMlo();
      default: {
        throw new IllegalArgumentException("Unknown field name: " + fieldName);
      }
    }
  }

  @Override
  public String getClassBaseURI() {
    return icsFileParsing.getObjectType();
  }

  /**
   * TODO
   * @param field
   * @return
   */
  private boolean isValid(IndexableField field) {
    final String name = field.name();
    if (name.equals(classIdDescription)) return false;
    if (name.equals(ID_FIELD)) return false;
    return true;
  }

  /**
   * TODO
   * @param fieldName
   * @param value
   * @return
   */
  private IndexableField createField(String fieldName, String value) {
    return new TextField(fieldName, value, Field.Store.YES);
  }
}