package de.fuberlin.wiwiss.d2r;

import de.unipassau.medsapce.SQL.SQLResultTuple;
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
    protected String translation;
    protected String dataType;
    protected String pattern;
    protected String xmlLang;
    protected String property;

    protected static Logger log = Logger.getLogger(Bridge.class);

    protected String getTranslation() { return translation; }

    protected void setTranslation(String translation) {
        if (translation == null) {
            this.translation = null;
            return;
        }

        this.translation = translation.trim();
        if (this.translation.equals("")) {
            this.translation = null;
        }
    }

    protected void setDataType(String dataType) {
        if (dataType == null) {
            this.dataType = null;
            return;
        }

        this.dataType = dataType.trim();
        if (this.dataType.equals("")) {
            this.dataType = null;
        }
    }

    protected String getPattern() { return pattern; }

    protected void setPattern(String pattern) {
        if (pattern == null) {
            this.pattern = null;
            return;
        }

        this.pattern = pattern.trim();
        if (this.pattern.equals("")) {
            this.pattern = null;
        }
    }

    protected String getXmlLang() { return xmlLang; }

    protected void setXmlLang(String xmlLang) {
        if (xmlLang == null) {
            this.xmlLang = null;
            return;
        }

        this.xmlLang = xmlLang.trim();
        if (this.xmlLang.equals("")) {
            this.xmlLang = null;
        }
    }

    protected Property createProperty(D2rProcessor processor) {
        Property prop = null;
        try {
            String propURI = processor.getNormalizedURI(getProperty());
            prop = ResourceFactory.createProperty(propURI);
            //prop = processor.getModel().getProperty(propURI);
        } catch (java.lang.Throwable ex) {
          log.warn("Warning: (getProperty) Property object for property " +
                   this.getProperty() + " not found in model.", ex);
        }
        return prop;
    }

    protected String getProperty() { return property; }

    protected void setProperty(String property) {
        if (property == null) this.property = null;
        else this.property = property.trim();
    }

    /**
     *
     * @param tuple
     * @return
     */
    protected abstract RDFNode getValue(SQLResultTuple tuple, URINormalizer normalizer);

    public String getDataType() {
        return this.dataType;
    }

  public void init(List<D2RMap> maps) {

  };
}