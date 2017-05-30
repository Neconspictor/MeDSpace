package de.fuberlin.wiwiss.d2r;

import java.util.HashMap;

/**
 * Created by David Goeth on 29.05.2017.
 */
public class ResultInstance {
  private HashMap<String, String> table;

  public ResultInstance(int columnCount) {
    table = new HashMap<>(columnCount);
  }

  public ResultInstance() {
    table = new HashMap<>();
  }

  /**
   * Provides the value of a specific column.
   * @param columnName The name of the column to get the value from.
   * @return The value of the specified column name; Null if the column couldn't be found.
   */
  public String getValueByColmnName(String columnName) {
    String key = D2rUtil.getFieldNameUpperCase(columnName);
    return table.get(key);
  }

  public void put(String columnName, String value) {
    String key = D2rUtil.getFieldNameUpperCase(columnName);
    table.put(key, value);
  }
}
