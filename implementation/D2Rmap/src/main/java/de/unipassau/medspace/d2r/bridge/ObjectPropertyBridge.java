package de.unipassau.medspace.d2r.bridge;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.d2r.D2R;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.D2rUtil;
import de.unipassau.medspace.d2r.exception.D2RException;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private List<String> referredGroupBy;
  private static Logger log = LoggerFactory.getLogger(ObjectPropertyBridge.class);


  public ObjectPropertyBridge(String referredClassID, List<D2rMap> maps) throws D2RException {
    referredGroupBy = new ArrayList<>();
    setReferredClassID(referredClassID);
    init(maps);
  }

  public String getReferredClassID() {
    return referredClassID;
  }

  public void setReferredClassID(String id) {

    if (id == null) {
      this.referredClassID = null;
      return;
    }

    this.referredClassID = id.trim().toUpperCase();
    if (this.referredClassID.equals(""))
      this.referredClassID = null;
  }

  public void setReferredClass(D2rMap map) {
    referredClass = map;
  }


  public List<String> getReferredGroupBy() {
    return referredGroupBy;
  }

  /**
   * Parses a string containing the GroupBy fields and adds them to the bridge.
   * @param  fields String with GroupBy fields separated by ','.
   */
  public void setReferredGroupBy(String fields) {
    StringTokenizer tokenizer = new StringTokenizer(fields.trim(), ",");
    while (tokenizer.hasMoreTokens()) {
      String columnName = tokenizer.nextToken().trim().toUpperCase();
      referredGroupBy.add(columnName);
    }
  }

  @Override
  public RDFNode getValue(SQLResultTuple tuple, QNameNormalizer normalizer) {
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

  protected void init(List<D2rMap> maps) throws D2RException {

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
      throw new D2RException("Referred class not found in the specified D2rMap list.");
    }
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