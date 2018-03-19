package de.unipassau.medspace.d2r;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.sql.*;

import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.rdf.*;
import de.unipassau.medspace.d2r.bridge.Bridge;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.common.SQL.SelectStatement;

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
   * TODO
   */
  private Bridge rdfTypeProperty;

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
   * A pattern that combined form a unique id for the rdf resources to be created.
   */
  private String resourceIdPattern;

  /**
   * Used to normalize the rdf triples.
   */
  private QNameNormalizer normalizer;

  /**
   * Meta data tags, tuple instances of this d2r map are related to.
   */
  private final List<String> metaDataTags;

  /**
   * TODO
   */
  private RDFFactory rdfFactory;

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(D2rMap.class);

  /**
   * Default constructor. Creates a new D2rMap.
   */
  public D2rMap(RDFFactory rdfFactory) {
    bridges = new ArrayList<>();
    normalizer = qName -> qName;
    this.rdfFactory = rdfFactory;
    this.metaDataTags = new ArrayList<>();
  }


  /**
   * Adds a D2rMap bridge to this D2rMap.
   * @param bridge The bridge to be added.
   */
  public void addBridge(Bridge bridge) {
    this.bridges.add(bridge);
    if (bridge.getPropertyQName().equals("rdf:type")) {
      this.rdfTypeProperty = bridge;
    }
  }

  /**
   * Adds a meta data tag to this D2rMap.
   * @param tag The tag to be added. Mustn't be null
   *
   * @throws NullPointerException If tag is null
   */
  public void addMetaDataTag(String tag) {
    if (tag == null) throw new NullPointerException("Null is not supported for a meta data tag");
    this.metaDataTags.add(tag);
  }


  /**
   * TODO
   * @param resourceIdPattern
   */
  public void setResourceIdPattern(String resourceIdPattern) {
    this.resourceIdPattern = resourceIdPattern;
  }

  /**
   * TODO
   * @param id
   * @return
   */
  public boolean compareIdTo(String id) {
    if (id == null) return false;
    return id.trim().toUpperCase().equals(this.id);
  }

  /**
   * Creates a list of rdf triples from a given sql result tuple.
   * @param tuple The sql result tuple to create rdf triples from.
   * @return A  list of rdf triples that represent the sql result tuple.
   */
  public List<Triple> createTriples(SQLResultTuple tuple) throws IOException {
    List<Triple> triples = new ArrayList<>();
    RDFResource resource;

    // set instance id
    String resourceID = D2rUtil.parsePattern(getResourceIdPattern(),
        D2R.PATTERN_DELIMINATOR,
        tuple);

    // define URI and generate instance
    String uri = rdfTypeProperty.getPattern() + "#" + resourceID;
    uri = normalizer.normalize(uri);
    resource = rdfFactory.createResource(uri);

    if (resource == null || resourceID.equals("")) {
      throw new IllegalStateException( "Couldn't create resource " + resourceID + " in map " + getId());
    }

    for (Bridge bridge : getBridges()) {
      // generate propertyQName
      RDFResource prop = bridge.createProperty(normalizer);
      RDFObject value = bridge.getValue(tuple, normalizer);
      if (prop != null && value != null) {
        Triple triple = rdfFactory.createTriple(resource, prop, value);
        triples.add(triple);
      }
    }

    return triples;
  }

  /**
   * Provides the base URi that is used by this class to give created rdf triples a base URI.
   * @return The base URI of this D2rMap.
   */
  public Bridge getRdfTypeProperty() {
    return rdfTypeProperty;
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
   * Provides an unmodifiable list of the meta tags used by this class.
   * @return The list of used meta tags. The result is unmodifiable.
   */
  public List<String> getMetaDataTags() {
    return Collections.unmodifiableList(metaDataTags);
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
   * TODO
   * Provides an unmodifiable list of resource id columns that is used to create unique URIs for rdf resources which are created by
   * this class.
   * @return An unmodifiable list of the resource id columns.
   */
  public String getResourceIdPattern() {
    return resourceIdPattern;
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
   * Creates an URI for an rdf resource which is an instance of this D2rMap.
   *
   * @param resourceID An distinct id referring to an rdf resource of this D2rMap.
   * @return The URI for the provided resource id
   */
  public String urify(String resourceID) {
    return normalizer.normalize(rdfTypeProperty.getPattern() + resourceID);
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