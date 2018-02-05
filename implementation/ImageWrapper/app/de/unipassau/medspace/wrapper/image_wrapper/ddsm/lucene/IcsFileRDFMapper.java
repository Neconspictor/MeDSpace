package de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene;

import de.unipassau.medspace.common.rdf.*;
import de.unipassau.medspace.common.wrapper.TripleConverter;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.*;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.IcsFile;
import de.unipassau.medspace.wrapper.image_wrapper.rdf_mapping.Util;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO
 */
public class IcsFileRDFMapper implements TripleConverter<IcsFile> {

  /**
   * TODO
   */
  private final IcsFileParsing icsFileParsing;

  /**
   * TODO
   */
  private final ImageParsing imageParsing;


  /**
   * Used to normalize the rdf triples.
   */
  private final QNameNormalizer normalizer;

  /**
   * TODO
   */
  private final RDFFactory rdfFactory;

  /**
   * TODO
   */
  private List<Pair<Property, String>> rdfProperties;

  /**
   * TODO
   */
  private Pair<Property, String> dateOfStudyPair;

  /**
   * TODO
   */
  private Pair<Property, String> patientAgePair;

  /**
   * TODO
   */
  private Pair<Property, String> densityPair;

  /**
   * TODO
   */
  private Pair<Property, String> dateDigitizedPair;

  /**
   * TODO
   */
  private Pair<Property, String> digitizerPair;

  /**
   * TODO
   */
  private Pair<Property, String> leftCCPair;

  /**
   * TODO
   */
  private Pair<Property, String> leftMLOPair;

  /**
   * TODO
   */
  private Pair<Property, String> rightCCPair;

  /**
   * TODO
   */
  private Pair<Property, String> rightMLOPair;



  /**
   * TODO
   * @param icsFileParsing
   * @param imageParsing
   * @param normalizer
   * @param rdfFactory
   */
  public IcsFileRDFMapper(IcsFileParsing icsFileParsing,
                          ImageParsing imageParsing,
                          QNameNormalizer normalizer,
                          RDFFactory rdfFactory) {
    this.icsFileParsing = icsFileParsing;
    this.imageParsing = imageParsing;
    this.normalizer = normalizer;
    this.rdfFactory = rdfFactory;

    dateOfStudyPair = new Pair(icsFileParsing.getDateOfStudy(), null);
    patientAgePair = new Pair(icsFileParsing.getPatientAge(),null);
    densityPair = new Pair(icsFileParsing.getDensity(),null);
    dateDigitizedPair = new Pair(icsFileParsing.getDateDigitized(),null);
    digitizerPair = new Pair(icsFileParsing.getDigitizer(),null);
    leftCCPair = new Pair(icsFileParsing.getLeftCc(),null);
    leftMLOPair = new Pair(icsFileParsing.getLeftMlo(),null);
    rightCCPair = new Pair(icsFileParsing.getRightCc(),null);
    rightMLOPair = new Pair(icsFileParsing.getRightMlo(),null);


    rdfProperties = Arrays.asList(
        dateOfStudyPair,
        patientAgePair,
        densityPair,
        dateDigitizedPair,
        digitizerPair,
        leftCCPair,
        leftMLOPair,
        rightCCPair,
        rightMLOPair
    );


  }

  @Override
  public List<Triple> convert(IcsFile icsFile) {

    String subjectURI = Util.createResourceId(normalizer,
        icsFileParsing.getObjectType(), icsFile.getId());

    RDFResource subject = rdfFactory.createResource(subjectURI);

    dateOfStudyPair.setAt1(Util.format(icsFile.getDateOfStudy()));
    patientAgePair.setAt1(String.valueOf(icsFile.getPatientAge()));
    densityPair.setAt1(String.valueOf(icsFile.getDensity()));
    dateDigitizedPair.setAt1(Util.format(icsFile.getDateDigitized()));
    digitizerPair.setAt1(icsFile.getDigitizer());

    leftCCPair.setAt1(createImageId(icsFile.getLeftCC().getId()));
    leftMLOPair.setAt1(createImageId(icsFile.getLeftMLO().getId()));
    rightCCPair.setAt1(createImageId(icsFile.getRightCC().getId()));
    rightMLOPair.setAt1(createImageId(icsFile.getRightMLO().getId()));

    return createTriples(rdfProperties, subject);
  }

  private String createImageId(String id) {
    return Util.createResourceId(normalizer, imageParsing.getObjectType(), id);
  }

  /**
   * TODO
   * @param content
   * @param subject
   * @return
   */
  private List<Triple> createTriples(List<Pair<Property, String>> content, RDFResource subject) {
    List<Triple> triples = new ArrayList<>();

    for (Pair<Property, String> elem : content) {

      Property property = elem.getValue0();
      String value = elem.getValue1();
      Triple triple = Util.triplize(rdfFactory,
                                    normalizer,
                                    property,
                                    subject,
                                    value);
      triples.add(triple);
    }

    return triples;
  }
}