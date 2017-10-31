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
   * The rdf namespace
   */
  public static final String RDFNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  /**
   * The standard prefix of the rdf namespace
   */
  public static final String RDFNS_PREFIX = "rdf";

  /**
   * Holds information about the root element of a D2RMap.
   */
  public static final class Root {
    /**
     * Name of the root element
     */
    public static final String NAME = "Map";

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
      public static final String STANDARD_OUTPUT_FORMAT = "N-TRIPLES";
    }
  }

  /**
   * Holds information about the complex type 'ClassMap'
   */
  public static final class ClassMap {

    /**
     * Name of the element 'ClassMap'
     */
    public static final String NAME = "ClassMap";
    /**
     * sql attribute from the ClassMap element
     */
    public static final String SQL_ATTRIBUTE = "sql";
    /**
     * resourceIdColumns attribute from the ClassMap element
     */
    public static final String RESOURCE_ID_COLUMNS_ATTRIBUTE = "resourceIdColumns";
    /**
     * id attribute from the ClassMap element
     */
    public static final String ID_ATTRIBUTE = "id";
    /**
     * type attribute from the ClassMap element
     */
    public static final String TYPE_ATTRIBUTE = "type";
    /**
     * baseURI attribute from the ClassMap element
     */
    public static final String BASE_URI_ATTRIBUTE = "baseURI";
  }

  /**
   * Holds information about the complex type 'DataSourceProperty'
   */
  public static final class DataSourceProperty {

    /**
     * Name of the DataSourceProperty element
     */
    public static final String NAME = "DataSourceProperty";

    /**
     * Name of the 'name' attribute
     */
    public static final String NAME_ATTRIBUTE = "name";

    /**
     * Name of the 'value' attribute
     */
    public static final String VALUE_ATTRIBUTE = "value";
  }

  /**
   * Holds information about the complex type 'DataTypePropertyBridge'
   */
  public static final class DataTypePropertyBridge {

    /**
     * The name of the DataTypePropertyBridge element
     */
    public static final String NAME = "DataTypePropertyBridge";

    /**
     * The name of the attribute 'property'
     */
    public static final String PROPERTY_ATTRIBUTE = "property";

    /**
     * The name of the attribute 'pattern'
     */
    public static final String PATTERN_ATTRIBUTE = "pattern";

    /**
     * The name of the attribute 'dataType'
     */
    public static final String DATA_TYPE_ATTRIBUTE = "dataType";

    /**
     * The name of the attribute 'lang'
     */
    public static final String LANG_ATTRIBUTE = "lang";
  }

  /**
   * Holds information about the complex type 'DBAuthentification'
   */
  public static final class DBAuthentification {

    /**
     * Name of the element 'DBAuthentification'
     */
    public static final String NAME = "DBAuthentification";

    /**
     * Name of the attribute 'username'
     */
    public static final String USERNAME_ATTRIBUTE = "username";

    /**
     * Name of the attribute 'password'
     */
    public static final String PASSWORD_ATTRIBUTE = "password";
  }

  /**
   * Holds information about the complex type 'DBConnection'
   */
  public static final class DBConnection {

    /**
     * Name of the element 'DBConnection'
     */
    public static final String NAME = "DBConnection";

    /**
     * Name of the attribute 'jdbcDSN'
     */
    public static final String JDBC_DSN_ATTRIBUTE = "jdbcDSN";

    /**
     * Name of the attribute 'jdbcDriver'
     */
    public static final String JDBC_DRIVER_ATTRIBUTE = "jdbcDriver";

    /**
     * Name of the attribute 'maxConnections'
     */
    public static final String MAX_CONNECTIONS_ATTRIBUTE = "maxConnections";
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
   * Holds information about the complex type 'ObjectPropertyBridge'
   */
  public static final class ObjectPropertyBridge {

    /**
     * Name of the element 'ObjectPropertyBridge'
     */
    public static final String NAME = "ObjectPropertyBridge";

    /**
     * Name of the attribute 'property'
     */
    public static final String PROPERTY_ATTRIBUTE = "property";

    /**
     * Name of the attribute 'pattern'
     */
    public static final String PATTERN_ATTRIBUTE = "pattern";

    /**
     * Name of the attribute 'referredClass'
     */
    public static final String REFERRED_CLASS_ATTRIBUTE = "referredClass";

    /**
     * Name of the attribute 'referredColumns'
     */
    public static final String REFERRED_GROUPBY_ATTRIBUTE = "referredColumns";
  }
}