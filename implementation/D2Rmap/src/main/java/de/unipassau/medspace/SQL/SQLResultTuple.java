package de.unipassau.medspace.SQL;

import org.javatuples.Pair;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by David Goeth on 26.06.2017.
 */
public class SQLResultTuple {
  private String[] columns;
  private String[] values;
  private HashMap<String, Integer> indices;
  private int columnCount;

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

  public int getColumnCount() {
    return columnCount;
  }

  public String getValue(String columnName) {
    Integer index = indices.get(columnName);
    if (index == null)
      throw new IllegalArgumentException("Column name not found: " + columnName);
    return values[index];
  }
  public String getValue(int index) {
    if (index < 0 || index >= columnCount)
      throw new IndexOutOfBoundsException("Index is not bounded to [0, " + columnCount + ") : index=" + index);
    return values[index];
  }

  public String getColumnName(int index) {
    if (index < 0 || index >= columnCount)
      throw new IndexOutOfBoundsException("Index is not bounded to [0, " + columnCount + ") : index=" + index);
    return columns[index];
  }

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