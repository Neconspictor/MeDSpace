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
      log.warn("Warning: Couldn't create resource " + resourceID + " in map " + getId() +
          ".");
      return null;
    }

    for (Bridge bridge : getBridges()) {
      // generate property
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

  public String getId() {
    return this.id;
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

  public String urify(String resourceID) {
    return baseURI + resourceID;
  }

  public String getSql() {
    return sql;
  }

  public void setId(String id) {
    this.id = id.trim().toUpperCase();
  }

  public void setNormalizer(QNameNormalizer normalizer) {
    this.normalizer = normalizer;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }
}