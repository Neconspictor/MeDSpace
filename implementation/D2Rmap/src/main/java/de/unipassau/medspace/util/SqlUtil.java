package de.unipassau.medspace.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.*;

/**
 * Utility class for sql statements and result sets
 */
public class SqlUtil {


  public static SQLQueryResult executeQuery(Connection connection, String query) throws SQLException {
    return new SQLQueryResult(connection, query);
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

  public static class SQLQueryResult {
    private ResultSet set;
    private int numColumns;
    private Statement statement;
    private static Logger log = LogManager.getLogger(SQLQueryResult.class);

    SQLQueryResult(Connection connection, String query) throws SQLException {
        statement = connection.createStatement();
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