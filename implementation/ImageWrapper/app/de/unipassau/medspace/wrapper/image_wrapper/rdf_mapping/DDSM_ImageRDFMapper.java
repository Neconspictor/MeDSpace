package de.unipassau.medspace.wrapper.image_wrapper.rdf_mapping;

import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.RDFFactory;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.wrapper.TripleConverter;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.ImageParsing;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.DDSM_Image;

import java.util.List;

/**
 * TODO
 */
public class DDSM_ImageRDFMapper implements TripleConverter<DDSM_Image> {


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
   * @param imageParsing
   * @param normalizer
   * @param rdfFactory
   */
  public DDSM_ImageRDFMapper(ImageParsing imageParsing,
                             QNameNormalizer normalizer,
                             RDFFactory rdfFactory) {
    this.imageParsing = imageParsing;
    this.normalizer = normalizer;
    this.rdfFactory = rdfFactory;
  }


  @Override
  public List<Triple> convert(DDSM_Image elem) {
    return null;
  }
}