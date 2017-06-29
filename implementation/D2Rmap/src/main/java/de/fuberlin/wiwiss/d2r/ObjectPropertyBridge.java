package de.fuberlin.wiwiss.d2r;

import java.util.List;
import java.util.Vector;
import java.util.StringTokenizer;

import de.unipassau.medspace.SQL.SQLResultTuple;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
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
  private String referredClassID;
  private D2rMap referredClass;
  private Vector<String> referredGroupBy;
  private static Logger log = Logger.getLogger(ObjectPropertyBridge.class);


  protected ObjectPropertyBridge() {
    referredGroupBy = new Vector<>();
    referredClassID = null;
  }

  protected String getReferredClassID() {
    return referredClassID;
  }

  protected void setReferredClassID(String referredClass) {
    this.referredClassID = referredClass.trim();
    if (this.referredClassID.equals(""))
      this.referredClassID = null;
  }

  public void setReferredClass(D2rMap map) {
    referredClass = map;
  }

  public void init(List<D2rMap> maps) {

    if (referredClassID == null) return;
    referredClass = null;
    for (D2rMap map : maps) {
      if (map.getId().equals(referredClassID)) {
        setReferredClass(map);
        break;
      }
    }

    // referredClass was not set
    if (referredClass == null) {
      throw new IllegalStateException("Referred class not found in the specified D2rMap list.");
    }
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
    while (tokenizer.hasMoreTokens()) {
      String columnName = tokenizer.nextToken().trim().toUpperCase();
      referredGroupBy.add(columnName);
    }
  }

  @Override
  protected RDFNode getValue(SQLResultTuple tuple, URINormalizer normalizer) {
    Resource referredResource = null;

    if (getReferredClassID() != null) {
      referredResource = getFromClass(tuple);
    }
    else if (getPattern() != null) {
      String value = getFromPattern(tuple);
      value = normalizer.normalize(value);
      referredResource = ResourceFactory.createResource(value);
    }

    return referredResource;
  }

  private Resource getFromClass(SQLResultTuple tuple) {
    assert referredClass != null;

    // get referred instance
    StringBuilder resourceIDBuilder = new StringBuilder();
    for (String columnName : getReferredGroupBy()) {
      String columnValue = D2rUtil.getColumnValue(columnName, tuple);
      resourceIDBuilder.append(columnValue);
    }
    String resourceID = resourceIDBuilder.toString();
    String uri = referredClass.urify(resourceID);
    return ResourceFactory.createResource(uri);
  }

  private String getFromPattern(SQLResultTuple tuple) {
    return D2rUtil.parsePattern(getPattern(),
        D2R.DELIMINATOR, tuple);
  }
}