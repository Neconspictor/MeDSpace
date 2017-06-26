package de.fuberlin.wiwiss.d2r;

import java.util.*;
import java.sql.*;

import de.unipassau.medsapce.SQL.SQLQueryResultStream;
import de.unipassau.medspace.util.FileUtil;
import de.unipassau.medspace.util.SqlUtil;
import de.unipassau.medspace.util.sql.SelectStatement;
import org.apache.jena.rdf.model.*;

import de.fuberlin.wiwiss.d2r.exception.D2RException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.sql.DataSource;

/**
 * D2RMap Class. A D2RMap class is created for every d2r:ClassMap element in the mapping file.
 * The D2RMap class contains a Vector with all Bridges and an HashMap with all resources.
 * <BR><BR>History:
 * <BR>18-05-2017   : Updated for Java 8; removed unsafe operations
 * <BR>07-21-2004   : Error handling changed to Log4J.
 * <BR>09-25-2003   : Changed for Jena2.
 * <BR>01-15-2003   : Initial version of this class.
 *
 * @author Chris Bizer chris@bizer.de / David Goeth goeth@fim.uni-passau.de
 * @version V0.3
 */
public class D2RMap {
  private HashMap<String, ResultResource> resources;
  private Vector<Bridge> bridges;
  private String baseURI;
  private String sql;
  private SelectStatement statement;
  private String id;
  private Vector<String> resourceIdColumns;

  /** log4j logger used for this class */
  private static Logger log = LogManager.getLogger(D2RMap.class);

  private static Vector<String> querySelectStatementOrder = new Vector(Arrays.asList("SELECT", "FROM", "WHERE", "GROUP BY",
  "HAVING", "UNION", "ORDER BY"));

  protected D2RMap() {
    resources = new HashMap<>();
    bridges = new Vector<>();
    resourceIdColumns = new Vector<>();
  }

  /**
   * Generates all resources for this map.
   * @param  processor Reference to an D2R processor instance.
   */
  void generateResources(D2rProcessor processor, DataSource dataSource,
                         List<String> conditionList) throws D2RException {
      String query = this.sql.trim();

      String ucQuery = query.toUpperCase();
      if (ucQuery.contains("UNION"))
        throw new D2RException("SQL statement should not contain UNION: " + query);

      query = addConditionStatements(query, conditionList);

      // Add ORDER BY statements to the query
      query = addOrderByStatements(query);

      query = statement.toString();

      //generate resources using the Connection
      this.generateResources(processor, dataSource, query);
  }

  public void init(DataSource dataSource) throws D2RException {
    try {
      statement = new SelectStatement(this.sql, dataSource);
    } catch (SQLException | D2RException e) {
      log.error(e);
      throw new D2RException("Couldn't init D2RMap: ");
    }
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

  private static String getAfter(String query, int index) {
    if ((index <= -1)
    || (index >= query.length())) {
      return "";
    }
    return query.substring(index, query.length());
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

  private static int getBeforeIndex(String query, Vector<String> querySelectStatementOrder, int index) {
    assert index != -1;
    int splitIndex = -1;
    for (int currentIndex = index; currentIndex != querySelectStatementOrder.size(); ++currentIndex) {
      String clause = querySelectStatementOrder.get(currentIndex);
        splitIndex = query.indexOf(clause);
        if (splitIndex != -1) break;
    }
    return splitIndex;
  }

  /**
   * Generates all resources for this map.
   * @param  processor Reference to an D2R processor instance.
   * @param  dataSource The database connection.
   */
  private void generateResources(D2rProcessor processor,
                                 DataSource dataSource, String query) throws D2RException {

    if (log.isDebugEnabled()) {
      log.debug("Generating resources for D2rProcessor: " + processor);
    }

    //get model from processor
    Model model = processor.getModel();

    SQLQueryResultStream queryResult = null;

    try {
      // Create and execute SQL statement
      queryResult = SqlUtil.executeQuery(dataSource, query, 0, 10);
      ResultSet rs = queryResult.getResultSet();
      int numCols = queryResult.getColumnCount();
      boolean more = rs.next();
      // loop over the result set and create new resources if resourceIdColumns values differ.
      while (more) {
        // cache resource data from the last tuple
        createResource(processor, model, rs, numCols);
        // Fetch the next result set row
        more = rs.next();
      }
    }
    catch (SQLException ex) {
      String message = "SQL Exception caught: ";
      message += SqlUtil.unwrapMessage(ex);
      throw new D2RException(message);
    }
    catch (D2RException ex) {
      throw ex;
    }
    catch (java.lang.Throwable ex) {
      // Got some other type of exception.  Dump it.
      throw new D2RException("Error: " + ex.toString(), ex);
    } finally {
      // do cleanup stuff
      FileUtil.closeSilently(queryResult);
    }
  }

  /**
   * Generates properties for all resources of this map.
   * @param  processor Reference to an D2R processor instance.
   */
  void generateResourceProperties(D2rProcessor processor)
      throws D2RException {

    for (ResultResource result : resources.values()) {
      generateTupleProperties(processor, result);
    }
  }

  private void generateTupleProperties(D2rProcessor processor, ResultResource tuple) {
    Resource inst = tuple.getResource();
    assert inst != null;

    for (Bridge bridge : this.bridges) {
      // generate property
      Property prop = bridge.getProperty(processor);
      RDFNode referredNode = bridge.getValue(processor, tuple);
      if (prop != null && referredNode != null) {
        inst.addProperty(prop, referredNode);
      }
    }
  }

  void addBridge(Bridge bridge) {
    this.bridges.add(bridge);
  }

  void setBaseURI(String baseURI) {
    this.baseURI = baseURI;
  }

  protected String getSql() {
    return sql;
  }

  protected void setSql(String sql) {
    this.sql = sql;
  }

  protected String getId() {
    return this.id;
  }

  protected void setId(String id) {
    this.id = id.trim();
  }

  /**
   * Adds GroupBy fields to the map.
   * @param  fields String containing all GroupBy fields separated be ','.
   */
  void addResourceIdColumns(String fields) {
    StringTokenizer tokenizer = new StringTokenizer(fields, ",");
    while (tokenizer.hasMoreTokens())
      this.resourceIdColumns.add(tokenizer.nextToken().trim());
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
    for (String aGroupBy : this.resourceIdColumns) {
      builder.append(aGroupBy);
      builder.append(", ");
    }

    // Replace the last two characters (", ") by a ";"
    builder.delete(builder.length() - 2, builder.length());
    builder.append(";");

    return builder.toString();
  }

  private void createResource(D2rProcessor processor, Model model, ResultSet rs, int numCols)
      throws SQLException, D2RException {
    ResultResource currentTuple = new ResultResource();
    for (int i = 1; i <= numCols; i++) {
      String columnName = SqlUtil.getColumnNameUpperCase(i, rs);
      currentTuple.put(columnName, rs.getString(i));
    }

    Resource resource;

    // set instance id
    StringBuilder resourceIDBuilder = new StringBuilder();
    for (String aGroupBy : this.resourceIdColumns) {
      resourceIDBuilder.append(currentTuple.getValueByColmnName(aGroupBy));
    }
    String resourceID = resourceIDBuilder.toString();

    // define URI and generate instance
    String uri = baseURI + resourceID;
    uri = processor.getNormalizedURI(uri);
    resource = model.createResource(uri);

    if (resource != null && !resourceID.equals("")) {
      currentTuple.setResource(resource);
      if (resources.get(resourceID) != null)
        log.warn("Resources with suplicate resource id " + resourceID + " in map " + this.getId());
      resources.put(resourceID, currentTuple);
    } else {
      log.warn("Warning: Couldn't create resource " + resourceID + " in map " + this.getId() +
          ".");
    }
  }

  /**
   * Provides a unmodifiable list of the column names of the SQL query used by this class.
   * @return A unmodifiable list of the column names
   */
  public List<String> getColumnNames() {
    return statement.getColumns();
  }

  public Resource getResourceById(String resourceID) {
    ResultResource instance = resources.get(resourceID);
    if (instance != null) return  instance.getResource();
    return null;
  }

  public Resource createNewResource(D2rProcessor processor, String resourceID) {
    String uri = baseURI + resourceID;  //TODO parse it properly
    uri = processor.getNormalizedURI(uri);
    Model model = processor.getModel();
    Resource resource = model.createResource(uri);
    ResultResource result = new ResultResource();
    result.setResource(resource);
    resources.put(resourceID, result);

    // TODO decide to export a type property or not
   /* Bridge bridge = null;
    for (Bridge b : bridges) {
      if (b.getProperty().equals("rdf:type")) {
        bridge = b;
        break;
      }
    }

    assert bridge != null;

    Property prop = bridge.getProperty(processor);
    RDFNode referredNode = bridge.getValue(processor, result);
    if (prop != null && referredNode != null) {
      resource.addProperty(prop, referredNode);
    }*/

    return resource;
  }

  public void clear() {
    resources.clear();
    statement.reset();
  }

  public SelectStatement getQuery() {
    return statement;
  }
}