package de.unipassau.medspace.common.config;

/**
 * Contains constants that are used for the general wrapper configuration.
 */
public final class Constants {

  /**
   * The schema that defines the structure and content of a valid general wrapper config file
   */
  public static final String WRAPPER_VALIDATION_SCHEMA = "/medspace-wrapper-config-specification.xsd";

  /**
   * The schema that defines basic XML types that can be used for defining RDF mappings.
   */
  public static final String RDF_MAPPING_SCHEMA = "/medspace-rdf-mapping-specification.xsd";

    /**
     * Standard output format - should be used if no output format element is stated.
     */
    public static final String STANDARD_OUTPUT_FORMAT = "N-TRIPLES";
}