package de.fuberlin.wiwiss.d2r;

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
    static final String D2RNS = "http://www.medspace.com/D2Rmap";
    static final String XMLNS = "http://www.w3.org/XML/1998/namespace";
    static final String RDFNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    static final String RDFNS_PREFIX = "rdf";
    static final String DELIMINATOR = "@@";
    static final String STANDARD_OUTPUT_FORMAT = "RDF/XML";

    // root element Map
    static final String ROOT_ELEMENT = "Map";

    static final String CLASS_MAP_ELEMENT = "ClassMap";
    static final String CLASS_MAP_SQL_ATTRIBUTE = "sql";
    static final String CLASS_MAP_RESOURCE_ID_COLUMNS_ATTRIBUTE = "resourceIdColumns";
    static final String CLASS_MAP_ID_ATTRIBUTE = "id";
    static final String CLASS_MAP_TYPE_ATTRIBUTE = "type";
    static final String CLASS_MAP_URI_PATTERN_ATTRIBUTE = "uriPattern";

    // complex type DataTypePropertyBridge
    static final String DATA_TYPE_PROPERTY_BRIDGE_ELEMENT = "DataTypePropertyBridge";
    static final String DATA_TYPE_PROPERTY_BRIDGE_PROPERTY_ATTRIBUTE = "property";
    static final String DATA_TYPE_PROPERTY_BRIDGE_PATTERN_ATTRIBUTE = "pattern";
    static final String DATA_TYPE_PROPERTY_BRIDGE_TRANSLATE_ATTRIBUTE = "translate";
    static final String DATA_TYPE_PROPERTY_BRIDGE_DATA_TYPE_ATTRIBUTE = "dataType";
    static final String DATA_TYPE_PROPERTY_BRIDGE_LANG_ATTRIBUTE = "lang";

    // complex type DBAuthentification
    static final String DBAUTHENTIFICATION_ELEMENT = "DBAuthentification";
    static final String DBAUTHENTIFICATION_USERNAME_ATTRIBUTE = "username";
    static final String DBAUTHENTIFICATION_PASSWORD_ATTRIBUTE = "password";

    // complex type DBConnection
    static final String DBCONNECTION_ELEMENT = "DBConnection";
    static final String DBCONNECTION_JDBC_DSN_ATTRIBUTE = "jdbcDSN";
    static final String DBCONNECTION_JDBC_DRIVER_ATTRIBUTE = "jdbcDriver";

    // complex type Namesapce
    static final String NAMESPACE_ELEMENT = "Namespace";
    static final String NAMESPACE_PREFIX_ATTRIBUTE = "prefix";
    static final String NAMESPACE_NAMESPACE_ATTRIBUTE = "namespace";

    // complex type ObjectPropertyBridge
    static final String OBJECT_PROPERTY_BRIDGE_ELEMENT = "ObjectPropertyBridge";
    static final String OBJECT_PROPERTY_BRIDGE_PROPERTY_ATTRIBUTE = "property";
    static final String OBJECT_PROPERTY_BRIDGE_PATTERN_ATTRIBUTE = "pattern";
    static final String OBJECT_PROPERTY_BRIDGE_TRANSLATE_ATTRIBUTE = "translate";
    static final String OBJECT_PROPERTY_BRIDGE_REFERRED_CLASS_ATTRIBUTE = "referredClass";
    static final String OBJECT_PROPERTY_BRIDGE_REFERRED_GROUPBY_ATTRIBUTE = "referredColumns";

    // element OutputFormat
    static final String OUTPUT_FORMAT_ELEMENT = "OutputFormat";

    // complex type Translation
    static final String TRANSLATION_ELEMENT = "Translation";
    static final String TRANSLATION_KEY_ATTRIBUTE = "key";
    static final String TRANSLATION_VALUE_ATTRIBUTE = "value";

    // complex type TranslationTable
    static final String TRANSLATION_TABLE_ELEMENT = "TranslationTable";
    static final String TRANSLATION_TABLE_ID_ATTRIBUTE = "id";
}