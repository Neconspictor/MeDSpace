package de.unipassau.medspace.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for sql specifics.
 */
public class SqlUtil {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(SqlUtil.class.getName());

  /**
   * Creates a new keyword condition, that can be used to search for keywords on a sql database.
   * @param keywords A list of keyords that form the condition.
   * @param columnNames The columns that should contain one or more of the keywords.
   * @param operator Specifies the operator to use. Use {@link Operator#OR} if it suffices if one keyword is found
   *                 or use {@link Operator#AND} if all keywords have to occur.
   * @return
   */
  public static String createKeywordCondition(List<String> keywords, List<String> columnNames, Operator operator) {
    StringBuilder builder = new StringBuilder();
    final String spacedOperator = " " + operator + " ";

    List<List<String>> placeholders = new ArrayList<>();
    for (String column : columnNames) {
      placeholders.add(Arrays.asList(column));
    }

    for (String keyword : keywords) {
      String condition = createOrColumnCondition("%s LIKE '%%" + keyword + "%%'", placeholders);
      builder.append("(");
      builder.append(condition);
      builder.append(")");
      builder.append(spacedOperator);
    }

    if (keywords.size() > 0) {
      // delete last and
      builder.delete(builder.length() - spacedOperator.length(), builder.length());
    }

    return builder.toString();
  }

  /**
   * Constructs a list of conditions by putting a list of placeholders into a condition string.
   * The condition is supposed to be a formatted string. See {@link String#format} for more information about formatted
   * strings.
   * After the conditions have been constructed, the conditions are ORed and the resulting condition is returned.
   * @param condition A condition that has at least one formatted string placeholder.
   * @param placeholders A list of placeholder-lists. All placeholder lists have to have the same number of items in
   *                     order that the replacement works properly.
   * @return Filled with placeholders and ORed conditions.
   */
  public static String createOrColumnCondition(String condition, List<List<String>> placeholders) {
    StringBuilder builder = new StringBuilder();
    final String or = " OR ";
    boolean available = placeholders.size() > 0;

    for (List<String> placeholderList : placeholders) {
      String filledCondition = condition;
      for (String placeholder : placeholderList) {
         filledCondition = String.format(filledCondition, placeholder);
      }
      builder.append(filledCondition);
      builder.append(or);
    }

    if (available) {
      builder.delete(builder.length() - or.length(), builder.length());
    }

    if (log.isDebugEnabled())
      log.debug(builder.toString());

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

    ResultSetMetaData metaData = set.getMetaData();
    assert metaData != null;

    String columnName = metaData.getColumnName(index);
    return columnName.toUpperCase();
  }

  /**
   * Defines SQL operators
   */
  public enum Operator {
    AND("AND"), OR("OR");

    /**
     * The name of the operator.
     */
    protected String name;

    /**
     * Constructs a new operator.
     * @param name
     */
    Operator(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }

  }
}