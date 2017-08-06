package de.unipassau.medspace.d2r;

/**
 * D2R constants and default configuration. 
 * <BR>History: 
 * <BR>01-15-2003   : Initial version of this class.
 * <BR>09-25-2003   : Changed for Jena2.
 * <BR>06-21-2004   : RDFNS renamed
 * @author Chris Bizer chris@bizer.de
 * @version V0.2
 */
public class D2R {
    public static final String D2RNS = "http://www.medspace.com/D2Rmap";
    public static final String XMLNS = "http://www.w3.org/XML/1998/namespace";
    public static final String RDFNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RDFNS_PREFIX = "rdf";
    public static final String DELIMINATOR = "@@";
    public static final String STANDARD_OUTPUT_FORMAT = "NTRIPLES";

    // root element Map
    public static final String ROOT_ELEMENT = "Map";

    public static final String CLASS_MAP_ELEMENT = "ClassMap";
    public static final String CLASS_MAP_SQL_ATTRIBUTE = "sql";
    public static final String CLASS_MAP_RESOURCE_ID_COLUMNS_ATTRIBUTE = "resourceIdColumns";
    public static final String CLASS_MAP_ID_ATTRIBUTE = "id";
    public static final String CLASS_MAP_TYPE_ATTRIBUTE = "type";
    public static final String CLASS_MAP_BASE_URI_ATTRIBUTE = "baseURI";

    // complex type DataTypePropertyBridge
    public static final String DATA_TYPE_PROPERTY_BRIDGE_ELEMENT = "DataTypePropertyBridge";
    public static final String DATA_TYPE_PROPERTY_BRIDGE_PROPERTY_ATTRIBUTE = "property";
    public static final String DATA_TYPE_PROPERTY_BRIDGE_PATTERN_ATTRIBUTE = "pattern";
    public static final String DATA_TYPE_PROPERTY_BRIDGE_TRANSLATE_ATTRIBUTE = "translate";
    public static final String DATA_TYPE_PROPERTY_BRIDGE_DATA_TYPE_ATTRIBUTE = "dataType";
    public static final String DATA_TYPE_PROPERTY_BRIDGE_LANG_ATTRIBUTE = "lang";

    // complex type DBAuthentification
    public static final String DBAUTHENTIFICATION_ELEMENT = "DBAuthentification";
    public static final String DBAUTHENTIFICATION_USERNAME_ATTRIBUTE = "username";
    public static final String DBAUTHENTIFICATION_PASSWORD_ATTRIBUTE = "password";

    // complex type DBConnection
    public static final String DBCONNECTION_ELEMENT = "DBConnection";
    public static final String DBCONNECTION_JDBC_DSN_ATTRIBUTE = "jdbcDSN";
    public static final String DBCONNECTION_JDBC_DRIVER_ATTRIBUTE = "jdbcDriver";
    public static final String DBCONNECTION_MAX_CONNECTIONS_ATTRIBUTE = "maxConnections";

    // complex type Namesapce
    public static final String NAMESPACE_ELEMENT = "Namespace";
    public static final String NAMESPACE_PREFIX_ATTRIBUTE = "prefix";
    public static final String NAMESPACE_NAMESPACE_ATTRIBUTE = "namespace";

    // complex type ObjectPropertyBridge
    public static final String OBJECT_PROPERTY_BRIDGE_ELEMENT = "ObjectPropertyBridge";
    public static final String OBJECT_PROPERTY_BRIDGE_PROPERTY_ATTRIBUTE = "property";
    public static final String OBJECT_PROPERTY_BRIDGE_PATTERN_ATTRIBUTE = "pattern";
    public static final String OBJECT_PROPERTY_BRIDGE_TRANSLATE_ATTRIBUTE = "translate";
    public static final String OBJECT_PROPERTY_BRIDGE_REFERRED_CLASS_ATTRIBUTE = "referredClass";
    public static final String OBJECT_PROPERTY_BRIDGE_REFERRED_GROUPBY_ATTRIBUTE = "referredColumns";

    // element OutputFormat
    public static final String OUTPUT_FORMAT_ELEMENT = "OutputFormat";

    // elemt DataSourceIndex
    public static final String INDEX_ELEMENT = "Index";
    public static final String INDEX_DIRECTORY_ATTRIBUTE = "directory";

    // complex type DataSourceProperty
    public static final String DATA_SOURCE_PROPERTY_ELEMENT = "DataSourceProperty";
    public static final String DATA_SOURCE_PROPERTY_NAME_ATTRIBUTE = "name";
    public static final String DATA_SOURCE_PROPERTY_VALUE_ATTRIBUTE = "value";

    public static final String MEDSPACE_VALIDATION_SCHEMA = "/Medspace_D2Rmap.xsd";


    /**
     * This constant is used as the field name for the map id
     */
    public static final String MAP_FIELD = "MAP";
}