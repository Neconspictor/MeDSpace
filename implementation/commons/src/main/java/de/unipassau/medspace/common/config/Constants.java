package de.unipassau.medspace.common.config;

/**
 * Created by David Goeth on 03.10.2017.
 */
public final class Constants {

  /**
   * The schema, that defines the structure and content of a valid general wrapper config file
   */
  public static final String WRAPPER_VALIDATION_SCHEMA = "/medspace-wrapper-config-specification.xsd";


  /**
   * Wrapper namespace
   */
  public static final String WRAPPER_NS = "http://www.medspace.com/wrapper-config-specification";

  /**
   * The rdf namespace
   */
  public static final String RDFNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  /**
   * The standard prefix of the rdf namespace
   */
  public static final String RDFNS_PREFIX = "rdf";

  /**
   * Holds information about the root element of a general wrapper configuration.
   */
  public static final class Root {
    /**
     * Name of the root element
     */
    public static final String NAME = "Config";
  }

  /**
   * Holds information about the complex type 'Index'
   */
  public static final class Index {

    /**
     * Name of the element 'Index'
     */
    public static final String NAME = "Index";

    /**
     * Name of the attribute 'directory'
     */
    public static final String DIRECTORY_ATTRIBUTE = "directory";
  }

  /**
   * Holds information about the complex type 'Namespace'
   */
  public static final class Namespace {

    /**
     * Name of the element 'Namespace'
     */
    public static final String NAME = "Namespace";

    /**
     * Name of the attribute 'prefix'
     */
    public static final String PREFIX_ATTRIBUTE = "prefix";

    /**
     * Name of the attribute 'namespace'
     */
    public static final String NAMESPACE_ATTRIBUTE = "namespace";
  }

  /**
   * Holds information about the element type 'OutputFormat'
   */
  public static final class OutputFormat {

    /**
     * Name of the element 'OutputFormat'
     */
    public static final String NAME = "OutputFormat";

    /**
     * Standard output format - should be used if no output format element is stated.
     */
    public static final String STANDARD_OUTPUT_FORMAT = "NTRIPLES";
  }

  /**
   * Holds information about the element type 'Register'
   */
  public static final class Register {

    /**
     * Name of the element 'Register'
     */
    public static final String NAME = "Register";

    /**
     * Name of the 'url' attribute
     */
    public static final String URL_ATTRIBUTE = "url";
  }
}