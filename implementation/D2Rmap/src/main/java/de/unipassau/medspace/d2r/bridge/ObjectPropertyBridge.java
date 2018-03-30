package de.unipassau.medspace.d2r.bridge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.RDFObject;
import de.unipassau.medspace.common.rdf.RDFFactory;
import de.unipassau.medspace.common.rdf.RDFResource;
import de.unipassau.medspace.d2r.D2R;
import de.unipassau.medspace.d2r.D2rMap;
import de.unipassau.medspace.d2r.D2rUtil;
import de.unipassau.medspace.d2r.exception.D2RException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MeDSpace D2R Bridge for object Properties (References to other instances).
 */
public class ObjectPropertyBridge
    extends Bridge {

  /**
   * The id of the referred D2rMap. Is useful to specify the D2rMap, if it does not exist, yet.
   */
  protected String referredClassID;

  /**
   * Specifies the referred D2rMap to createDoc property values.
   * If it is null, the pattern member attribute of the Bridge class is used to createDoc property values.
   */
  protected D2rMap referredClass;

  /**
   * Specifies the columns that should be used from a sql query to createDoc property values from.
   */
  protected List<String> referredColumns;

  /**
   * Status flag to check whether this object is ready to use.
   */
  protected boolean initialized = false;

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(ObjectPropertyBridge.class);


  /**
   * Creates a new ObjectPropertyBridge class instance. If the parameter 'referredClassID' is not null,
   * This class uses it to find the D2rMap in the accompanying list 'maps', that has the same id.
   *
   * @param referredClassID Specifies the id of the referred D2rMap or null, if the pattern should be used for creating
   *                        property values instead of a D2rMap.
   * @param maps Used to get the referred D2rMap for creating property values.
   * @throws D2RException thrown if referredClassID isn't null and no D2rMap could be found, whose id matches
   * 'referredClassID'.
   */
  public ObjectPropertyBridge(RDFFactory primitiveValueFactory, String referredClassID,
                              List<D2rMap> maps) throws D2RException {
    super(primitiveValueFactory);
    referredColumns = new ArrayList<>();
    setReferredClassID(referredClassID);
    init(maps);
  }

  /**
   * Provides the D2rMap that is referred by this class. That D2rMap is used to createDoc property values.
   * @return The referred D2rMap or null, if no D2rMap is used for creating property values.
   */
  public D2rMap getReferredClass() {
    return referredClass;
  }

  /**
   * Provides an unmodifiable list of the columns of a SQL result tuple, that are used to createDoc property values from.
   * @return An unmodifiable list of the referred columns of a SQL result tuple.
   */
  public List<String> getReferredColumns() {
    return Collections.unmodifiableList(referredColumns);
  }

  /**
   * Sets the referred D2rMap this ObjectPropertyBridge should use to createDoc property values.
   * @param map The D2rMap to use for creating property values.
   */
  public void setReferredClass(D2rMap map) {
    referredClass = map;
    referredClassID = map.getId();
  }

  /**
   * Parses a string containing the column fields and adds them to the bridge. The columns are used to createDoc rdf
   * resources from the referred D2rMap class. The referred D2rMap is given by the method getReferredClass
   * @param  fields String with GroupBy fields separated by ','.
   */
  public void setReferredColumns(String fields) {
    StringTokenizer tokenizer = new StringTokenizer(fields.trim(), ",");
    while (tokenizer.hasMoreTokens()) {
      String columnName = tokenizer.nextToken().trim().toUpperCase();
      referredColumns.add(columnName);
    }
  }

  /**
   * Provides the object propertyQName as a rdf node from a specific sql result tuple.
   * @param tuple The sql result tuple to extract the object propertyQName from.
   * @param normalizer Used to normalize the resulting object propertyQName
   * @return The object propertyQName from the sql result tuple.
   */
  @Override
  public RDFObject getValue(SQLResultTuple tuple, QNameNormalizer normalizer) {

    if (!initialized) {
      throw new IllegalStateException("init must be called before executing this function.");
    }

    RDFObject referredResource = null;

    assert (referredClass != null || pattern != null);

    if (referredClass != null) {
      referredResource = getFromClass(tuple);
    }
    else if (pattern != null) {
      String value = getFromPattern(tuple);
      value = normalizer.normalize(value);
      referredResource = factory.createResource(value);
    }

    return referredResource;
  }

  /**
   * Provides the id of the referred D2rMap.
   * @return The id of the referred D2rMap. or null, id no D2rMap is referred.
   */
  protected String getReferredClassID() {
    return referredClassID;
  }

  /**
   * Initializes this ObjectPropertyBridge instance. It assigns the refferedClass member
   * on the base of the referredClassID member.
   * @param maps Used to assign  the referred class member.
   * @throws D2RException Thrown if a referredClassID was assigned to this class, but the referring D2RMap wasn't
   * found in the list 'maps'
   */
  protected void init(List<D2rMap> maps) throws D2RException {

    if (referredClassID == null) {
      initialized = true;
      return;
    }
    referredClass = null;

    // search the referred class on the base on its id
    for (D2rMap map : maps) {
      if (map.getId().equals(referredClassID)) {
        referredClass = map;
        break;
      }
    }

    // referredClass was not set
    if (referredClass == null) {
      throw new D2RException("Referred class not found in the specified D2rMap list.");
    }

    initialized = true;
  }

  /**
   * Sets the id for the referred D2rMap.
   * If a D2rMap should be referred by this ObjectPropertyBridge, this method has to be called before the method 'init'
   * is called, as that method automatically searches the referred D2rMap on the base of the 'referredClassID' member.
   * @param id
   */
  protected void setReferredClassID(String id) {

    if (id == null) {
      this.referredClassID = null;
      return;
    }

    this.referredClassID = id.trim().toUpperCase();
    if (this.referredClassID.equals(""))
      this.referredClassID = null;
  }

  /**
   * Creates a rdf resource from a given SQLResultTuple. The result will be an instance of the
   * referred D2rMap.
   * @param tuple The sql result tuple to createDoc the rdf resource from.
   * @return An rdf resource which is an instance of the referred D2rMap specified by the class member 'referredClass'.
   */
  private RDFResource getFromClass(SQLResultTuple tuple) {
    assert referredClass != null;

    // extract the resource id from the tuple
    StringBuilder resourceIDBuilder = new StringBuilder();
    for (String columnName : referredColumns) {
      String columnValue = D2rUtil.getColumnValue(columnName, tuple);
      resourceIDBuilder.append(columnValue);
    }

    // build the resource having its id
    String resourceID = resourceIDBuilder.toString();
    String uri = referredClass.urify(resourceID);
    return factory.createResource(uri);
  }

  /**
   * Creates a rdf resource from a given SQLResultTuple by using the pattern of this class.
   * @param tuple The sql result tuple to createDoc the rdf resource from.
   * @return
   */
  private String getFromPattern(SQLResultTuple tuple) {
    return D2rUtil.parsePattern(pattern,
        D2R.PATTERN_DELIMINATOR, tuple);
  }
}