package de.unipassau.medspace.d2r.bridge;

import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An abstract class representing an D2r Bridge. A D2r Bridge is used to create rdf statements for a specific
 * rdf resource. These statements are also called properties of a given rdf resource.
 */
abstract public class Bridge {

    /**
     * Specifies the rdf data type of the properties created by this bridge
     */
    protected String dataType;

    /**
     * The pattern to create properties from.
     */
    protected String pattern;

    /**
     * An optional rdf language tag created properties should have.
     */
    protected String langTag;

    /**
     * The qualified name of the propertyQName.
     */
    protected String propertyQName;

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(Bridge.class);

    /**
     * Provides the language tag, the values of created properties should assigned to.
     * @return the language tag.
     */
    public String getLangTag() {
        return langTag;
    }

    /**
     * Creates a new propertyQName and normlizes it qualified name by a give normalizer.
     * @param normalizer The normalizer to normalize the qualified name of the created propertyQName.
     * @return A new propertyQName instance of this D2r Bridge.
     */
    public Property createProperty(QNameNormalizer normalizer) {
        String propURI = normalizer.normalize(propertyQName);
        Property prop = ResourceFactory.createProperty(propURI);
        return prop;
    }

    /**
     * Provides the pattern to create the value of the properties.
     * @return The value pattern for the properties created by this D2r Bridge.
     */
    public String getPattern() {
        return pattern;
    }

    public String getPropertyQName() { return propertyQName; }

    /**
     * Provides the propertyQName value as a rdf node from a specific sql result tuple.
     * @param tuple The sql result tuple to get the propertyQName value from.
     * @param normalizer The qualified name normalizer to normalize the resulting propertyQName value.
     *                   It is implementation dependent if a normalizer is needed. E.g. a DataTypePropertyBridge
     *                   don't need any normalizer.
     * @return  The propertyQName value represented as an rdf node.
     */
    public abstract RDFNode getValue(SQLResultTuple tuple, QNameNormalizer normalizer);

    public String getDataType() {
        return this.dataType;
    }

    /**
     * Sets the rdf data type created propertyQName values should have.
     * @param dataType The wished rdf data type.
     */
    public void setDataType(String dataType) {
        if (dataType == null) {
            this.dataType = null;
            return;
        }

        this.dataType = dataType.trim();
        if (this.dataType.equals("")) {
            this.dataType = null;
        }
    }

    /**
     * Sets the rdf language tag, created propertyQName values should assigned to.
     * @param langTag The wished language tag
     */
    public void setLangTag(String langTag) {
        if (langTag == null) {
            this.langTag = null;
            return;
        }

        this.langTag = langTag.trim();
        if (this.langTag.equals("")) {
            this.langTag = null;
        }
    }

    /**
     * Sets the pattern used to create the propertyQName values.
     * @param pattern The wished value pattern
     */
    public void setPattern(String pattern) {

        this.pattern = pattern;

        if (this.pattern == null) {
            return;
        }

        this.pattern = this.pattern.trim();
        if (this.pattern.equals("")) {
            this.pattern = null;
        }
    }

    /**
     * Sets the qualified name created properties should have.
     * @param propertyQName The qualified name for the created properties.
     * @throws IllegalArgumentException if 'propertyQName' is null, empty or consists only of whitespaces.
     */
    public void setPropertyQName(String propertyQName) {

        String original = propertyQName;

        if (propertyQName != null) {
            propertyQName = propertyQName.trim();

            if (propertyQName.equals("")) {
                propertyQName = null;
            }
        }

        if (propertyQName == null) {
            throw new IllegalArgumentException("qualified name "  + original + " not allowed.");
        }

        this.propertyQName = propertyQName;
    }
}