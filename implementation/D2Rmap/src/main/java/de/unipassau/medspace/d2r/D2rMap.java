package de.unipassau.medspace.d2r;

import java.io.Serializable;
import java.util.*;
import java.sql.*;

import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.rdf.rdf4j.TripleFactory;
import de.unipassau.medspace.d2r.bridge.Bridge;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.common.SQL.SelectStatement;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * A D2rMap is an object that describes a mapping from sql data to rdf triples.
 */
public class D2rMap implements Serializable {

  /**
   * a list of used D2R bridges
   */
  private List<Bridge> bridges;

  /**
   * The base URI rdf resources created by this class should have.
   */
  private String baseURI;

  /**
   * Defines a sql query, that is used to fetch the data from a sql database that wounding will be converted to rdf
   * triples.
   */
  private String sql;

  /**
   * A select query statement. Is used to easily query the database.
   */
  private SelectStatement statement;

  /**
   * The D2rMap id. Should be unique in order to identify different D2rMaps.
   */
  private String id;

  /**
   * The columns of the sql query, that combined form a unique id for the rdf resources to be created.
   */
  private List<String> resourceIdColumns;

  /**
   * Used to normalize the rdf triples.
   */
  private QNameNormalizer normalizer;

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(D2rMap.class);

  private static final ValueFactory factory = SimpleValueFactory.getInstance();

  /**
   * Default constructor. Creates a new D2rMap.
   */
  public D2rMap() {
    bridges = new ArrayList<>();
    resourceIdColumns = new ArrayList<>();
    normalizer = qName -> qName;
  }


  /**
   * Adds a D2rMap bridge to this D2rMap.
   * @param bridge The bridge to be added.
   */
  public void addBridge(Bridge bridge) {
    this.bridges.add(bridge);
  }

  /**
   * Adds columns from the given string to the resource id column list of this class.
   * The resource id columns ure used to construct unique ids for rdf resources.
   * @param  columns String containing all resource id columns. The columns are expected  to be separated by ','.
   */
  public void addResourceIdColumns(String columns) {
    StringTokenizer tokenizer = new StringTokenizer(columns, ",");
    while (tokenizer.hasMoreTokens()) {
      String columnName = tokenizer.nextToken().toUpperCase().trim();
      resourceIdColumns.add(columnName);
    }
  }

  /**
   * Creates a list of rdf triples from a given sql result tuple.
   * @param tuple The sql result tuple to create rdf triples from.
   * @return A  list of rdf triples that represent the sql result tuple.
   */
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
    resource = factory.createIRI(uri);

    if (resource == null || resourceID.equals("")) {
      log.warn("Warning: Couldn't createDoc resource " + resourceID + " in map " + getId() +
          ".");
      return null;
    }

    /*for (Bridge bridge : getBridges()) {
      // generate propertyQName
      Property prop = bridge.createProperty(normalizer);
      RDFNode value = bridge.getValue(tuple, normalizer);
      if (prop != null && value != null) {
        Triple triple = Triple.create(resource.asNode(), prop.asNode(), value.asNode());
        triples.add(triple);
      }
    }*/

    for (Bridge bridge : getBridges()) {
      // generate propertyQName
      IRI prop = bridge.createPropertyRDF4J(normalizer);
      Value value = bridge.getValueRDF4J(tuple, normalizer);
      if (prop != null && value != null) {
        Statement stmt = factory.createStatement(resource, prop, value);
        triples.add(TripleFactory.create(stmt));
      }
    }

    return triples;
  }

  /**
   * Provides the base URi that is used by this class to give created rdf triples a base URI.
   * @return The base URI of this D2rMap.
   */
  public String getBaseURI() {
    return baseURI;
  }

  /**
   * Provides an unmodifiable list of D2r bridges used by this class.
   * @return The list of used D2r bridges. The result is unmodifiable.
   */
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

  /**
   * Provides the used qualified name normalizer used by this class to normalize rdf triples.
   * @return The normalizer used by this class.
   */
  public QNameNormalizer getNormalizer() {
    return normalizer;
  }

  /**
   * Provides the sql query to get the sql data that this class can map to rdf triples.
   * @return The used sql select query.
   */
  public SelectStatement getQuery() {
    return statement;
  }

  /**
   * Provides an unmodifiable list of resource id columns that is used to create unique URIs for rdf resources which are created by
   * this class.
   * @return An unmodifiable list of the resource id columns.
   */
  public List<String> getResourceIdColumns() {
    return Collections.unmodifiableList(resourceIdColumns);
  }

  /**
   * Inits this D2rMap. The Datasource is needed to get column names of the query and verify that the select query of
   * is valid.
   * @param dataSource The datasource used to init the select query.
   * @throws D2RException
   */
  public void init(DataSource dataSource) throws D2RException {
    try {
      statement = new SelectStatement(this.sql, dataSource);
    } catch (SQLException e) {
      throw new D2RException("Couldn't init D2rMap: ", e);
    }
  }

  /**
   * Sets the base URI this class should use as a base for all rdf triples created by this class.
   * @param baseURI The base URI to use.
   */
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

  /**
   * Provides the sql query used to fetch data from a datasource.
   * @return
   */
  public String getSql() {
    return sql;
  }

  /**
   * Sets the D2rMap for this class.
   * @param id The new D2rMap id.
   */
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

  /**
   * Sets the sql select query this class should use to fetch sql data from a datasource.
   * @param sql The sql select query to use.
   */
  public void setSql(String sql) {
    this.sql = sql;
  }


  @Override
  public String toString() {
    return "D2rMap:\n" +
        "id= " + id;
  }
}