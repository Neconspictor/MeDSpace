package de.fuberlin.wiwiss.d2r;

import java.util.HashMap;
import java.util.Vector;
import java.util.StringTokenizer;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.log4j.Logger;

/**
 * D2R bridge for ObjectProperties (References to other instances).
 * <BR>History: 01-15-2003   : Initial version of this class.
 * <BR>History: 09-25-2003   : Changed for Jena2.
 * @author Chris Bizer chris@bizer.de
 * @version V0.2
 */
public class ObjectPropertyBridge
    extends Bridge {
  private String referredClass;
  private Vector<String> referredGroupBy;
  private static Logger log = Logger.getLogger(ObjectPropertyBridge.class);

  protected ObjectPropertyBridge() {
    referredGroupBy = new Vector<>();
  }

  protected String getReferredClass() {
    return referredClass;
  }

  protected void setReferredClass(String referredClass) {
    this.referredClass = referredClass.trim();
  }

  protected Vector<String> getReferredGroupBy() {
    return referredGroupBy;
  }

  /**
   * Parses a string containing the GroupBy fields and adds them to the bridge.
   * @param  fields String with GroupBy fields separated by ','.
   */
  protected void setReferredGroupBy(String fields) {
    StringTokenizer tokenizer = new StringTokenizer(fields.trim(), ",");
    while (tokenizer.hasMoreTokens())
      this.referredGroupBy.add(tokenizer.nextToken().trim());
  }

  @Override
  protected RDFNode getValue(D2rProcessor processor, ResultResource tuple) {
    Model model = processor.getModel();
    Resource referredResource = null;

    if (getReferredClass() != null) {
      referredResource = getFromClass(processor, tuple);
    }
    else if (getPattern() != null) {
      String value = getFromPattern(tuple);
      value = processor.getNormalizedURI(value);
      referredResource = model.getResource(value);
    }
    else if (getColumn() != null) {
      String value = getFromColumn(processor, tuple);
      value = processor.getNormalizedURI(value);
      referredResource = model.getResource(value);
    }
    else if (getValue() != null) {
      String value = getValue();
      value = processor.getNormalizedURI(value);
      referredResource = model.getResource(value);
    }

    return referredResource;
  }

  private Resource getFromClass(D2rProcessor processor, ResultResource tuple) {
    D2RMap referredMap = processor.getMapById(getReferredClass());
    if (referredMap == null) {
      log.warn("Warning: (CreateProperties) Couldn't find referred " +
          "map " + getReferredClass());
      return null;
    }
    // get referred instance
    StringBuilder resourceIDBuilder = new StringBuilder();
    for (String s : getReferredGroupBy()) {
      resourceIDBuilder.append(tuple.getValueByColmnName(s));
    }
    String resourceID = resourceIDBuilder.toString();
    Resource referredResource = referredMap.getResourceById(
        resourceID);
    if (referredResource == null) {
      log.warn("Warning: (CreateProperties) Reference to resource of class " +
          getReferredClass() + " with resource id " + resourceID + " not found");
    }
    return referredResource;
  }

  private String getFromColumn(D2rProcessor processor, ResultResource tuple) {
    String value = tuple.getValueByColmnName(getColumn());
    if (getTranslation() != null) {
      HashMap<String, TranslationTable> tables = processor.getTranslationTables();
      TranslationTable table = tables.get(getTranslation());
      if (table != null) {
        String translation = table.get(value);
        // if not found in table and there is a pattern -> use pattern
        if (translation == null && getPattern() != null) {
          // alternative pattern
          translation = D2rUtil.parsePattern(getPattern(),
              D2R.DELIMINATOR, tuple);
        }
        value = translation;
      } else {
        log.warn("Warning: (CreateProperties) " +
            "Couldn't find translation table " +
            getTranslation());
      }
    }
    return value;
  }

  private String getFromPattern(ResultResource tuple) {
    return D2rUtil.parsePattern(getPattern(),
        D2R.DELIMINATOR, tuple);
  }
}