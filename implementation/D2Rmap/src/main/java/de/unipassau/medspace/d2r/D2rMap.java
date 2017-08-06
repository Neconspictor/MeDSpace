package de.unipassau.medspace.d2r;

import java.util.*;
import java.sql.*;

import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.d2r.bridge.Bridge;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.common.SQL.SelectStatement;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * D2rMap Class. A D2rMap class is created for every d2r:ClassMap element in the mapping file.
 * The D2rMap class contains a Vector with all Bridges and an HashMap with all resources.
 * <BR><BR>History:
 * <BR>18-05-2017   : Updated for Java 8; removed unsafe operations
 * <BR>07-21-2004   : Error handling changed to Log4J.
 * <BR>09-25-2003   : Changed for Jena2.
 * <BR>01-15-2003   : Initial version of this class.
 *
 * @author Chris Bizer chris@bizer.de / David Goeth goeth@fim.uni-passau.de
 * @version V0.3
 */
public class D2rMap {
  private Vector<Bridge> bridges;
  private String baseURI;
  private String sql;
  private SelectStatement statement;
  private String id;
  private List<String> resourceIdColumns;
  private QNameNormalizer normalizer;

  /** log4j logger used for this class */
  private static Logger log = LoggerFactory.getLogger(D2rMap.class);

  public D2rMap() {
    bridges = new Vector<>();
    resourceIdColumns = new ArrayList<>();
    normalizer = qName -> qName;
  }


  public void addBridge(Bridge bridge) {
    this.bridges.add(bridge);
  }

  /**
   * Adds GroupBy fields to the map.
   * @param  fields String containing all GroupBy fields separated be ','.
   */
  public void addResourceIdColumns(String fields) {
    StringTokenizer tokenizer = new StringTokenizer(fields, ",");
    while (tokenizer.hasMoreTokens()) {
      String columnName = tokenizer.nextToken().toUpperCase().trim();
      resourceIdColumns.add(columnName);
    }
  }

  public List<Triple> createTriples(SQLResultTuple tuple) {
    List<Triple> triples = new ArrayList<>();
    Resource resource;

    // set instance id
    StringBuilder resourceIDBuilder = new StringBuilder();

    for (String columnName : getResourceIdColumns()) {
      String columnValue = D2rUtil.getColumnValue(columnName, tuple);
      resourceIDBuilder.append(columnValue);
    }
    String resourceID = resourceIDBuilder.toString();

    // define URI and generate instance
    String uri = getBaseURI() + resourceID;
    uri = normalizer.normalize(uri);
    resource = ResourceFactory.createResource(uri);

    if (resource == null || resourceID.equals("")) {
      log.warn("Warning: Couldn't createDoc resource " + resourceID + " in map " + getId() +
          ".");
      return null;
    }

    for (Bridge bridge : getBridges()) {
      // generate propertyQName
      Property prop = bridge.createProperty(normalizer);
      RDFNode value = bridge.getValue(tuple, normalizer);
      if (prop != null && value != null) {
        Triple triple = Triple.create(resource.asNode(), prop.asNode(), value.asNode());
        triples.add(triple);
      }
    }

    return triples;
  }

  public String getBaseURI() {
    return baseURI;
  }

  public List<Bridge> getBridges() {
    return Collections.unmodifiableList(bridges);
  }

  /**
   * Provides the id of this D2rMap.
   * Warranty: The result isn't null
   * @return
   */
  public String getId() {
    assert id != null;
    return id;
  }

  public QNameNormalizer getNormalizer() {
    return normalizer;
  }

  public SelectStatement getQuery() {
    return statement;
  }

  public List<String> getResourceIdColumns() {
    return Collections.unmodifiableList(resourceIdColumns);
  }

  public void init(DataSource dataSource) throws D2RException {
    try {
      statement = new SelectStatement(this.sql, dataSource);
    } catch (SQLException e) {
      throw new D2RException("Couldn't init D2rMap: ", e);
    }
  }

  public void setBaseURI(String baseURI) {
    this.baseURI = baseURI;
  }

  /**
   * Creates an URI for an rdf resource which is an instance of this D2rMap.
   *
   * @param resourceID An distinct id referring to an rdf resource of this D2rMap.
   * @return The URI for the provided resource id
   */
  public String urify(String resourceID) {
    return normalizer.normalize(baseURI + resourceID);
  }

  public String getSql() {
    return sql;
  }

  public void setId(String id) {
    this.id = id.trim().toUpperCase();
  }

  /**
   * Sets the qualified name normalizer this D2rMap should use to
   * @param normalizer The qualified name normalizer to use
   * @throws NullPointerException if 'normalizer' is null
   */
  public void setNormalizer(QNameNormalizer normalizer) {
    if (normalizer == null) {
      throw new NullPointerException("the QNameNormalizer isn't allowed to be null");
    }
    this.normalizer = normalizer;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }
}