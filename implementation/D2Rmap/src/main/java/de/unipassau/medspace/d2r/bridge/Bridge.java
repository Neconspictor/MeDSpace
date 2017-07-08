package de.unipassau.medspace.d2r.bridge;

import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.d2r.D2rMap;
import org.apache.jena.rdf.model.*;
import org.apache.log4j.Logger;

import java.util.List;


/**
 * Abstract class representing an D2R bridge. Extended by the subclasses ObjectPropertyBridge and DatatypePropertyBridge.
 * <BR><BR>History: 
 * <BR>07-21-2004   : Error handling changed to Log4J.
 * <BR>09-25-2003   : Changed for Jena2.
 * <BR>01-15-2003   : Initial version of this class.
 * @author Chris Bizer chris@bizer.de
 * @version V0.2
 */
abstract public class Bridge {
    protected String dataType;
    protected String pattern;
    protected String xmlLang;
    protected String property;

    protected static Logger log = Logger.getLogger(Bridge.class);

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

    public String getPattern() { return pattern; }

    public void setPattern(String pattern) {
        if (pattern == null) {
            this.pattern = null;
            return;
        }

        this.pattern = pattern.trim();
        if (this.pattern.equals("")) {
            this.pattern = null;
        }
    }

    public String getXmlLang() { return xmlLang; }

    public void setXmlLang(String xmlLang) {
        if (xmlLang == null) {
            this.xmlLang = null;
            return;
        }

        this.xmlLang = xmlLang.trim();
        if (this.xmlLang.equals("")) {
            this.xmlLang = null;
        }
    }

    public Property createProperty(QNameNormalizer normalizer) {
        String propURI = normalizer.normalize(property);
        Property prop = ResourceFactory.createProperty(propURI);
        return prop;
    }

    public String getProperty() { return property; }

    public void setProperty(String property) {
        if (property == null) this.property = null;
        else this.property = property.trim();
    }

    /**
     *
     * @param tuple
     * @return
     */
    public abstract RDFNode getValue(SQLResultTuple tuple, QNameNormalizer normalizer);

    public String getDataType() {
        return this.dataType;
    }

  public void init(List<D2rMap> maps) {

  };
}