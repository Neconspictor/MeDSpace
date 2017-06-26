package de.unipassau.medsapce.SQL;

import javafx.util.Pair;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by David Goeth on 26.06.2017.
 */
public class SQLResultTuple {
  private String[] columns;
  private String[] values;
  private HashMap<String, Integer> indices;
  private int columnCount;

  public  SQLResultTuple(List<Pair<String, String>> tuple) {
    columnCount = tuple.size();
    columns = new String[columnCount];
    values = new String[columnCount];
    indices = new HashMap<>(columnCount);

    int index = 0;
    for (Pair<String, String> pair : tuple) {
      columns[index] = pair.getKey();
      values[index] = pair.getValue();
      indices.put(columns[index], index);
    }
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

  public static SQLResultTuple create(ResultSet resultSet) throws SQLException {
    ResultSetMetaData meta = resultSet.getMetaData();
    int numColumns = meta.getColumnCount();
    List<Pair<String, String>> tuple = new ArrayList<>(numColumns);

    for (int i = 1; i <= numColumns; ++i) {
      String columnName = meta.getColumnName(i);
      String value = resultSet.getString(i);
      tuple.add(new Pair<>(columnName, value));
    }
    return new SQLResultTuple(tuple);
  }
}
