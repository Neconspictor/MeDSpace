package de.unipassau.medspace.util;

import de.fuberlin.wiwiss.d2r.exception.D2RException;
import de.fuberlin.wiwiss.d2r.exception.FactoryException;
import de.fuberlin.wiwiss.d2r.factory.DriverFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.*;

/**
 * Utility class for sql statements and result sets
 */
public class SqlUtil {


  public static SQLQueryResult executeQuery(Connection connection, String query, int maxRowSize) throws SQLException {
    return new SQLQueryResult(connection, query, maxRowSize);
  }

  /**
   * Provides the column name of a given result set at a specific index.
   * @param index the index to get the column name from
   * @param set the result set to operate on
   * @return the column name at the specified index.
   * @throws SQLException If a database error occurs
   */
  public static String getColumnNameUpperCase(int index, ResultSet set) throws SQLException {
    if (set == null) throw new NullPointerException("set mustn't be null!");
    if (index < 0) throw new IllegalArgumentException("index has to be > 0!");

    ResultSetMetaData metaData = set.getMetaData();//.getColumnName(index).toUpperCase();
    assert metaData != null;

    String columnName = metaData.getColumnName(index);
    return columnName.toUpperCase();
  }

  public static String unwrapMessage(SQLException e) {
    StringBuilder message = new StringBuilder();
    while (e != null) {
      message.append(" SQLState: ");
      message.append(e.getSQLState());
      message.append("Message:  ");
      message.append(e.getMessage());
      message.append("Vendor:   ");
      message.append(e.getErrorCode());
      e = e.getNextException();
    }
    return message.toString();
  }

  public static void closeSilently(Connection con) {
    try {
      if (!con.isClosed()) con.close();
    } catch (SQLException e) {
      // ignore purposely
    }
  }


  /**
   * Creates a new JDBC Driver from this Object's Connection properties.
   *
   * @throws D2RException Thrown if an error occurs while creating the Driver
   * @return Driver The JDBC driver to the datasource. NOTE: It is guaranteed, that the result is not null
   */
  public static Driver createDriver(String jdbcDriverClass) throws D2RException {
    //value to be returned
    Driver driver;

    //get required information
    if (jdbcDriverClass == null) {
      throw new D2RException("Could not connect to database because of " +
          "missing Driver.");
    }

    try {
      //if there is a classpath supplied, use it to instantiate Driver
      /*if (getDriverClasspath() != null) {

        //dynamically load and instantiate Driver from the classPath URL
        driver = DriverFactory.getInstance().getDriverInstance(driverClass,
            getDriverClasspath());
      }
      else {
      */
        //attempt to load and instantiate Driver from the current classpath
        driver = DriverFactory.getInstance().getDriverInstance(jdbcDriverClass);
      //}
    }
    catch (FactoryException factoryException) {

      throw new D2RException("Could not instantiate Driver class.",
          factoryException);
    }

    if (driver == null)
      throw new D2RException("Driver is supposed to be != null! Fix the bug!");

    return driver;
  }

  public static class SQLQueryResult {
    private ResultSet set;
    private int numColumns;
    private Statement statement;
    private static Logger log = LogManager.getLogger(SQLQueryResult.class);

    SQLQueryResult(Connection connection, String query, int maxRowSize) throws SQLException {
        statement = connection.createStatement();
        statement.setMaxRows(maxRowSize);
        int fetchSize = 10;
        if (fetchSize > maxRowSize && (maxRowSize != 0))
          fetchSize = maxRowSize;
        statement.setFetchSize(fetchSize);
        set = statement.executeQuery(query);
      numColumns = set.getMetaData().getColumnCount();
    }

    public void close() {
      try {
        statement.close();
      } catch (SQLException e) {
        log.warn("Couldn't close statement!", e);
      }
      try {
        set.close();
      } catch (SQLException e) {
        log.warn("Couldn't close result set!", e);
      }
    }

    public ResultSet getResultSet() {
      return set;
    }

    public int getColumnCount() {
      return numColumns;
    }
  }
}