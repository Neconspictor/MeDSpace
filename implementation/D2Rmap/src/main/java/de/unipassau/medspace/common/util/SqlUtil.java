package de.unipassau.medspace.common.util;

import java.sql.*;
import java.util.List;

/**
 * Utility class for sql statements and result sets
 */
public class SqlUtil {

  public static String createKeywordCondition(List<String> keywords, List<String> columnNames) {
    StringBuilder builder = new StringBuilder();
    final String and = " AND ";

    for (String keyword : keywords) {
      String condition = createOrColumnCondition("LIKE '%" + keyword + "%'", columnNames);
      builder.append("(");
      builder.append(condition);
      builder.append(")");
      builder.append(and);
    }

    if (keywords.size() > 0) {
      // delete last and
      builder.delete(builder.length() - and.length(), builder.length());
    }

    return builder.toString();
  }

  public static String createOrColumnCondition(String condition, List<String> columnNames) {
    StringBuilder builder = new StringBuilder();
    final String or = " OR ";
    boolean available = columnNames.size() > 0;

    for (String column : columnNames) {
      builder.append(column + " " + condition + or);
    }

    if (available) {
      builder.delete(builder.length() - or.length(), builder.length());
    }

    return builder.toString();
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
}