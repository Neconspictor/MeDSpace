package de.unipassau.medspace.d2r;

/**
 * Contains the constants and the names of xml elements used in a MeDSpace D2rMap config file.
 */
public final class D2R {

  /**
   * D2rMap namespace
   */
  public static final String D2RNS = "http://www.medspace.com/D2Rmap";

  /**
   * The pattern deliminator is used to sign D2R patterns. With D2r Patterns the user can
   * refer columns of a sql query or a ClassMap by its id.
   */
  public static final String PATTERN_DELIMINATOR = "@@";

  /**
   * This constant is used as the field name for a map id. Used to assign index documents a specific D2r Map
   */
  public static final String MAP_FIELD = "MAP";

  /**
   * The MeDSpace schema, that defines the structure and content of a valid MeDSpace D2RMap config file
   */
  public static final String MEDSPACE_VALIDATION_SCHEMA = "/Medspace_D2Rmap.xsd";

  /**
   * This constant is used as the field name for meta data tags.
   */
  public static final String D2RMAP_META_DATA_TAGS = "D2RMAP_META_DATA_TAGS";

  /**
   * The rdf namespace
   */
  public static final String RDFNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  /**
   * The standard prefix of the rdf namespace
   */
  public static final String RDFNS_PREFIX = "rdf";

  /**
   * Standard output format - should be used if no output format element is stated.
   */
  public static final String STANDARD_OUTPUT_FORMAT = "N-TRIPLES";
}