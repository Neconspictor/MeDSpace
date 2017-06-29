package de.fuberlin.wiwiss.d2r;

import java.util.*;
import java.sql.*;

import de.unipassau.medspace.SQL.SQLQueryResultStream;
import de.unipassau.medspace.SQL.SQLResultTuple;
import de.unipassau.medspace.util.sql.SelectStatement;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.*;

import de.fuberlin.wiwiss.d2r.exception.D2RException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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

  /** log4j logger used for this class */
  private static Logger log = LogManager.getLogger(D2rMap.class);

  private static Vector<String> querySelectStatementOrder = new Vector(Arrays.asList("SELECT", "FROM", "WHERE", "GROUP BY",
  "HAVING", "UNION", "ORDER BY"));

  public D2rMap() {
    bridges = new Vector<>();
    resourceIdColumns = new ArrayList<>();
  }

  public void addBridge(Bridge bridge) {
    this.bridges.add(bridge);
  }

  public static String addConditionStatements(String query, List<String> conditionList) {
    // Nothing to do?
    if (conditionList.size() == 0)
      return query;

    String ucQuery = query.toUpperCase();
    String startClause = "WHERE";

    int beforeWhereClauseIndex = getBeforeIndex(ucQuery, querySelectStatementOrder, querySelectStatementOrder.indexOf(startClause));
    int afterWhereClauseIndex = beforeWhereClauseIndex + startClause.length();
    boolean containsStartClause = ucQuery.contains(startClause);
    if (!containsStartClause) {
      afterWhereClauseIndex = beforeWhereClauseIndex;
    }
    String beforeWhereClause = getBefore(query, beforeWhereClauseIndex);
    String afterWhereClause = getAfter(query, afterWhereClauseIndex);

    if (beforeWhereClause.matches("(.*)\\s")) { // ends with a whitesapce
      beforeWhereClause = beforeWhereClause.substring(0, beforeWhereClause.length() - 1);
    }

    if (afterWhereClause.matches("^\\s(.*)")) { // begins with a whitesapce
      afterWhereClause = afterWhereClause.substring(1, afterWhereClause.length());
    }

    // Create where condition clause statement
    StringBuilder builder = new StringBuilder(" ");
    String andStatement = " AND ";
    builder.append(startClause);
    builder.append(" ");
    for (String condition : conditionList) {
      builder.append(condition);
      builder.append(andStatement);
    }

    // delete last " AND "
    if (!containsStartClause) {
      builder.delete(builder.length() - andStatement.length(), builder.length());
      builder.append(" ");
    }

    // concatenate finally the query pieces
    return beforeWhereClause + builder.toString() + afterWhereClause;
  }

  private String addOrderByStatements(String query) throws D2RException {
    if (query == null) throw new NullPointerException("query mustn't be null!");
    if (resourceIdColumns.isEmpty()) return query;

    // Create upper case version for checking for ORDER BY statements
    String ucQuery = query.toUpperCase();
    if (ucQuery.contains("ORDER BY"))
      throw new D2RException("SQL statement should not contain ORDER BY: " + query);

    // Query contains a semicolon at the end?
    int semicolonIndex = query.indexOf(";");
    if (semicolonIndex != -1)
      query = query.substring(0, query.indexOf(";"));

    StringBuilder builder = new StringBuilder(query); // StringBuilder for faster string creation
    builder.append(" ORDER BY ");
    for (String aGroupBy : resourceIdColumns) {
      builder.append(aGroupBy);
      builder.append(", ");
    }

    // Replace the last two characters (", ") by a ";"
    builder.delete(builder.length() - 2, builder.length());
    builder.append(";");

    return builder.toString();
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

  public void clear() {
    statement.reset();
  }

  private static String getAfter(String query, int index) {
    if ((index <= -1)
    || (index >= query.length())) {
      return "";
    }
    return query.substring(index, query.length());
  }

  public SQLQueryResultStream getAllData(DataSource dataSource) throws SQLException {
    statement.reset();
    return statement.execute(dataSource);
  }

  private static String getBefore(String query, int index) {
    if (index == -1) {
      return query;
    } else {
      if (index > query.length())
        index = query.length();
      return query.substring(0, index);
    }
  }

  private static int getBeforeIndex(String query, List<String> querySelectStatementOrder, int index) {
    assert index != -1;
    int splitIndex = -1;
    for (int currentIndex = index; currentIndex != querySelectStatementOrder.size(); ++currentIndex) {
      String clause = querySelectStatementOrder.get(currentIndex);
        splitIndex = query.indexOf(clause);
        if (splitIndex != -1) break;
    }
    return splitIndex;
  }

  protected String getId() {
    return this.id;
  }

  public SelectStatement getQuery() {
    return statement;
  }

  protected String getSql() {
    return sql;
  }

  public void init(DataSource dataSource, List<D2rMap> maps) throws D2RException {
    try {
      statement = new SelectStatement(this.sql, dataSource);
    } catch (SQLException | D2RException e) {
      log.error(e);
      throw new D2RException("Couldn't init D2rMap: ");
    }

    for (Bridge bridge : bridges) {
      bridge.init(maps);
    }
  }

  public void setBaseURI(String baseURI) {
    this.baseURI = baseURI;
  }

  protected void setId(String id) {
    this.id = id.trim();
  }

  protected void setSql(String sql) {
    this.sql = sql;
  }

  public List<String> getResourceIdColumns() {
    return Collections.unmodifiableList(resourceIdColumns);
  }

  public String getBaseURI() {
    return baseURI;
  }

  public List<Bridge> getBridges() {
    return Collections.unmodifiableList(bridges);
  }

  public String urify(String resourceID) {
    return baseURI + resourceID;
  }

  public List<Triple> createTriples(SQLResultTuple tuple, URINormalizer normalizer) {
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
}