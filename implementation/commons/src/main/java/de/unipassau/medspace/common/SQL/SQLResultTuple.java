package de.unipassau.medspace.common.SQL;

import org.javatuples.Pair;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A result tuple of an SQL query.
 */
public class SQLResultTuple {
  private String[] columns;
  private String[] values;
  private HashMap<String, Integer> indices;
  private int columnCount;

  /**
   * Creates a new SQLResultTuple object.
   * @param tuple The content of the SQL result tuple.
   */
  public  SQLResultTuple(ArrayList<Pair<String, String>> tuple) {
    columnCount = tuple.size();
    columns = new String[columnCount];
    values = new String[columnCount];
    indices = new HashMap<>(columnCount);

    for (int i = 0; i < columnCount; ++i) {
      Pair<String, String> pair = tuple.get(i);
      columns[i] = pair.getValue0();
      values[i] = pair.getValue1();
      indices.put(columns[i], i);
    }
  }

  /**
   * Fetches a result tuple from a SQL result set.
   * @param resultSet the  SQL result set.
   * @return The fetched result tuple.
   * @throws SQLException If an SQL exception occurrs.
   */
  public static SQLResultTuple create(ResultSet resultSet) throws SQLException {
    ResultSetMetaData meta = resultSet.getMetaData();
    int numColumns = meta.getColumnCount();
    ArrayList<Pair<String, String>> tuple = new ArrayList<>(numColumns);

    for (int i = 1; i <= numColumns; ++i) {
      String columnName = meta.getColumnName(i).toUpperCase();
      String value = resultSet.getString(i);
      tuple.add(new Pair<>(columnName, value));
    }
    return new SQLResultTuple(tuple);
  }

  /**
   * Provides the number of columns this tuple has.
   * @return the number of columns this tuple has.
   */
  public int getColumnCount() {
    return columnCount;
  }

  /**
   * Provides the value of a given column name.
   * @param columnName The colunmn name.
   * @return the value of the given column name.
   *
   * @throws IllegalArgumentException if the column name is not valid for this tuple.
   */
  public String getValue(String columnName) {
    Integer index = indices.get(columnName);
    if (index == null)
      throw new IllegalArgumentException("Column name not found: " + columnName + ", indices are: " + mapToString(indices));
    return values[index];
  }

  String mapToString(Map<String, Integer> map) {

    StringBuilder builder = new StringBuilder();
    for (Map.Entry<String, Integer> entry : map.entrySet()) {
      builder.append("(" + entry.getKey() + ", " + entry.getValue() + "),\n");
    }
    return builder.toString();
  }

  /**
   * Provides the value of a given tuple index.
   * @param index The index to get the value from.
   * @return the value of the given index.
   *
   * @throws IndexOutOfBoundsException If the index is smaller zero or is greater/equal
   * the number of columns this tuple has.
   */
  public String getValue(int index) {
    if (index < 0 || index >= columnCount)
      throw new IndexOutOfBoundsException("Index is not bounded to [0, " + columnCount + ") : index=" + index);
    return values[index];
  }

  /**
   * Provides the column name of a given tuple index.
   * @param index The index to get the column name from.
   * @return the column name of the given index.
   *
   * @throws IndexOutOfBoundsException If the index is smaller zero or is greater/equal
   * the number of columns this tuple has.
   */
  public String getColumnName(int index) {
    if (index < 0 || index >= columnCount)
      throw new IndexOutOfBoundsException("Index is not bounded to [0, " + columnCount + ") : index=" + index);
    return columns[index];
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    final String semi = ", ";
    builder.append("(");
    for (int i = 0; i < columnCount; ++i) {
      builder.append(columns[i]);
      builder.append("=");
      builder.append(values[i]);
      builder.append(", ");
    }
    if (columnCount > 0)
      builder.delete(builder.length() - semi.length(), builder.length());
    builder.append(")");
    return builder.toString();
  }
}