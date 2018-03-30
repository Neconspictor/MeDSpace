package de.unipassau.medspace.d2r.bridge;

import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.RDFObject;
import de.unipassau.medspace.common.rdf.RDFFactory;

import de.unipassau.medspace.common.rdf.RDFResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An abstract class representing a D2r Bridge. A D2r Bridge is used to create rdf statements for a specific
 * rdf resource. These statements are also called properties of a given rdf resource.
 */
abstract public class Bridge {

    /**
     * The pattern to createDoc properties from.
     */
    protected String pattern;

    /**
     * The qualified name of the propertyQName.
     */
    protected String propertyQName;

    /**
     * The RDF factory used by this class.
     */
    protected RDFFactory factory;

    /**
     * Logger instance for this class.
     */
    private static Logger log = LoggerFactory.getLogger(Bridge.class);


    /**
     * Creates a new Bridge object.
     * @param factory The RDF factory to be used.
     */
    public Bridge(RDFFactory factory) {
        this.factory = factory;
    }


    /**
     * Creates a new propertyQName and normlizes it qualified name by a give normalizer.
     * @param normalizer The normalizer to normalize the qualified name of the created propertyQName.
     * @return A new propertyQName instance of this D2r Bridge.
     */
    public RDFResource createProperty(QNameNormalizer normalizer) {
        String propURI = normalizer.normalize(propertyQName);
        return factory.createResource(propURI);
    }

    /**
     * Provides the pattern to createDoc the value of the properties.
     * @return The value pattern for the properties created by this D2r Bridge.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Provides the qualified name of the property.
     * @return the qualified name of the property.
     */
    public String getPropertyQName() { return propertyQName; }

    /**
     * Provides the propertyQName value as a rdf node from a specific sql result tuple.
     * @param tuple The sql result tuple to get the propertyQName value from.
     * @param normalizer The qualified name normalizer to normalize the resulting propertyQName value.
     * @return  The propertyQName value represented as an rdf node.
     */
    public abstract RDFObject getValue(SQLResultTuple tuple, QNameNormalizer normalizer);

    /**
     * Sets the pattern used to createDoc the propertyQName values.
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