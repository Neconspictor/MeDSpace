package de.fuberlin.wiwiss.d2r;

import java.util.Vector;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.sql.*;

import de.unipassau.medspace.util.SqlUtil;
import org.apache.jena.rdf.model.*;

import de.fuberlin.wiwiss.d2r.exception.D2RException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * D2R Map Class. A Map class is created for every d2r:ClassMap element in the mapping file.
 * The Map class contains a Vector with all Bridges and an HashMap with all resources.
 * <BR><BR>History:
 * <BR>18-05-2017   : Updated for Java 8; removed unsafe operations
 * <BR>07-21-2004   : Error handling changed to Log4J.
 * <BR>09-25-2003   : Changed for Jena2.
 * <BR>01-15-2003   : Initial version of this class.
 *
 * @author Chris Bizer chris@bizer.de / David Goeth goeth@fim.uni-passau.de
 * @version V0.3
 */
public class Map {
  private HashMap<String, Resource> resources;
  private Vector<Bridge> bridges;
  private String uriPattern;
  private String uriColumn;
  private String sql;
  private String id;
  private Vector<String> groupBy;

  /** log4j logger used for this class */
  private static Logger log = LogManager.getLogger(Map.class);

  protected Map() {
    resources = new HashMap();
    bridges = new Vector();
    groupBy = new Vector();
  }

  /**
   * Generates all resources for this map.
   * @param  processor Reference to an D2R processor instance.
   */
  protected void generateResources(D2rProcessor processor) throws D2RException {

    try {

      //get a connection from the processor
      Connection con = processor.getConnection();

      String query = this.sql.trim();

      // Add ORDER BY statements to the query
      query = addOrderByStatements(query);

      //generate resources using the Connection
      this.generateResources(processor, con, query);

      //close the connection
      con.close();
    }
    catch (SQLException ex) {

      //an error occurred while closing the connection
      throw new D2RException("Could not close JDBC Connection.", ex);
    }
  }

  /**
   * Generates all resources for this map.
   * @param  processor Reference to an D2R processor instance.
   * @param  con The database connection.
   */
  protected void generateResources(D2rProcessor processor,
                                   Connection con, String query) throws D2RException {

    if (log.isDebugEnabled()) {

      log.debug("Generating resources for D2rProcessor: " + processor);
    }

    //get model from processor
    Model model = processor.getModel();

    SqlUtil.SQLQueryResult queryResult = null;

    try {

      // Create and execute SQL statement
      queryResult = SqlUtil.executeQuery(con, query);
      ResultSet rs = queryResult.getResultSet();
      int numCols = queryResult.getColumnCount();
      boolean more = rs.next();
      ResultInstance lastTuple = new ResultInstance();
      // loop over the result set and create new resources if groupBy values differ.
      while (more) {
        // cache resource data from the last tuple
        lastTuple = createResource(processor, model, rs, numCols, lastTuple);
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
      if (queryResult != null) queryResult.close();
    }
  }

  /**
   * Generates properties for all resources of this map.
   * @param  processor Reference to an D2R processor instance.
   */
  protected void generatePropertiesForAllInstances(D2rProcessor processor)
      throws D2RException {

    try {

      //get a connection from the processor
      Connection con = processor.getConnection();

      //generate properties using the Connection
      this.generatePropertiesForAllInstances(processor, con);

      //close the connection
      con.close();
    }
    catch (SQLException ex) {

      //an error occurred while closing the connection
      throw new D2RException("Could not close JDBC Connection.", ex);
    }
  }

  /**
   * Generates properties for all resources of this map.
   * @param  processor Reference to an D2R processor instance.
   * @param  con The database connection.
   */
  protected void generatePropertiesForAllInstances(D2rProcessor processor,
      Connection con) throws D2RException {
    Model model = processor.getModel();
    String query = this.sql;
    try {

      java.sql.Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      int numCols = rs.getMetaData().getColumnCount();
      // create properties for all resources
      boolean more = rs.next();
      while (more) {
        // cache tuple data
        ResultInstance tuple = new ResultInstance(numCols);
        for (int i = 1; i <= numCols; i++)
          tuple.put(rs.getMetaData().getColumnName(i).trim().toUpperCase(), rs.getString(i));
          // get instance id
        String instID = "";
        for (Iterator<String> it = this.groupBy.iterator(); it.hasNext(); ) {
          instID += tuple.getValueByColmnName(it.next());
        }
        //get instance
        generateTupleProperties(processor, model, tuple, instID);
        more = rs.next();
      }
      // Close result set and statement
      rs.close();
      stmt.close();
    }
    catch (SQLException ex) {
      String message = "SQL Exception caught: ";
      while (ex != null) {
        message += " SQLState: " + ex.getSQLState();
        message += "Message:  " + ex.getMessage();
        message += "Vendor:   " + ex.getErrorCode();
        ex = ex.getNextException();
      }
      throw new D2RException(message);
    }
    catch (java.lang.Throwable ex) {
      // Got some other type of exception.  Dump it.
      throw new D2RException(ex.getMessage());
    }
  }

  private void generateTupleProperties(D2rProcessor processor, Model model, ResultInstance tuple, String instID) {
    Resource inst = getInstanceById(instID);
    if (inst == null) {
      log.warn("Warning: (CreateProperties) Didn't find instance " +
          instID + " in map " + this.getId() + ".");
      return;
    }

    for (Iterator<Bridge> propIt = this.bridges.iterator(); propIt.hasNext(); ) {
      Bridge bridge = propIt.next();
      // generate property
      Property prop = bridge.getProperty(processor);
      RDFNode referredNode = bridge.getReferredNode(processor, model, tuple);
      if (prop != null && referredNode != null) {
        inst.addProperty(prop, referredNode);
      }
    }
  }

  protected Vector getBridges() {
    return bridges;
  }

  protected void addBridge(Bridge bridge) {
    this.bridges.add(bridge);
  }

  protected String getUriPattern() {
    return uriPattern;
  }

  protected void setUriPattern(String uriPattern) {
    this.uriPattern = uriPattern;
  }

  protected String getUriColumn() {
    return uriColumn;
  }

  protected void setUriColumn(String uriColumn) {
    this.uriColumn = uriColumn;
  }

  protected String getSql() {
    return sql;
  }

  protected void setSql(String sql) {
    this.sql = sql;
  }

  protected Vector getGroupBy() {
    return groupBy;
  }

  protected void setGroupBy(Vector groupBy) {
    this.groupBy = groupBy;
  }

  protected String getId() {
    return this.id;
  }

  protected void setId(String id) {
    this.id = id;
  }

  /**
   * Adds GroupBy fields to the map.
   * @param  fields String containing all GroupBy fields separated be ','.
   */
  protected void addGroupByFields(String fields) {
    StringTokenizer tokenizer = new StringTokenizer(fields, ",");
    while (tokenizer.hasMoreTokens())
      this.groupBy.add(tokenizer.nextToken().trim());
  }

  /**
   * Return the instance with the specified ID.
   * @param id ID. Instances are identified by the values of the d2r:groupBy fields.
   * return Instance with the specified ID.
   */
  protected Resource getInstanceById(String id) {
    return this.resources.get(id);
  }

  private String addOrderByStatements(String query) throws D2RException {
    if (query == null) throw new NullPointerException("query mustn't be null!");

    // Create upper case version for checking for ORDER BY statements
    String ucQuery = query.toUpperCase();
    if (ucQuery.contains("ORDER BY"))
        throw new D2RException("SQL statement should not contain ORDER BY: " + query);

    // Query contains a semicolon at the end?
    int semicolonIndex = query.indexOf(";");
    if (semicolonIndex != -1)
      query = query.substring(0, query.indexOf(";"));

    StringBuilder builder = new StringBuilder(query); // Stringbuilder for faster string creation
    builder.append(" ORDER BY ");
    for (Iterator<String> it = this.groupBy.iterator(); it.hasNext(); ) {
      builder.append(it.next());
      builder.append(", ");
    }

    // Replace the last two characters (", ") by a ";"
    builder.delete(builder.length() - 3, builder.length());
    builder.append(";");

    return builder.toString();
  }

  private ResultInstance createResource(D2rProcessor processor, Model model, ResultSet rs, int numCols,
                                        ResultInstance lastTuple)
      throws SQLException, D2RException {
    ResultInstance currentTuple = new ResultInstance();
    for (int i = 1; i <= numCols; i++) {
      String columnName = SqlUtil.getColumnNameUpperCase(i, rs);
      currentTuple.put(columnName, rs.getString(i));
    }
    // check if new instance
    // It checks if the last tuple and the current are identical
    // Only if they don't match a new resource instance will be created
    // TODO check why it is important to check the current and the last tuple for matching.
    // TODO Duplicates could be spread along the whole result set; testing only consecutive tuples doesn't suffice in general(?)
    // TODO Or is the whole newResource testing stuff eventually obsolete???
    // TODO jena.model treats resources with the same uri as equal, so the model class shouldn't be a problem
    boolean newResource = false;
    for (Iterator<String> it = this.groupBy.iterator(); it.hasNext(); ) {
      String fieldName = it.next();
      String current = currentTuple.getValueByColmnName(fieldName); // TODO if current is null, something is not right!!!
      if (!current.equals(lastTuple.getValueByColmnName(fieldName))) {
        newResource = true;
        break;
      }
    }

    if (!newResource) return currentTuple;

    Resource resource = null;
    // define URI and generate instance
    if (this.uriPattern != null) {
      String uri = D2rUtil.parsePattern(this.uriPattern, D2R.DELIMINATOR,
          currentTuple);
      uri = processor.getNormalizedURI(uri);
      resource = model.createResource(uri);

    } else if (this.uriColumn != null) {
      String uri = currentTuple.getValueByColmnName(this.uriColumn);
      if (uri == null)
        throw new D2RException(
            "No NULL value in the URI colunm '" +
                D2rUtil.getFieldNameUpperCase(this.uriColumn) + "' allowed.");
      uri = processor.getNormalizedURI(uri);
      resource = model.createResource(uri);

    } else {
      // generate blank node instance
      resource = model.createResource();
    }
    // set instance id
    String resourceID = "";
    for (Iterator<String> it = this.groupBy.iterator(); it.hasNext(); ) {
      resourceID += currentTuple.getValueByColmnName(it.next());
    }
    if (resource != null && resourceID != "") {
      //inst.setInstanceID(instID); TODO
      resources.put(resourceID, resource);
    } else {
      log.warn("Warning: Couldn't create resource " + resourceID + " in map " + this.getId() +
          ".");
    }
    return currentTuple;
  }
}