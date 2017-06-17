package de.fuberlin.wiwiss.d2r;

import org.apache.jena.rdf.model.Resource;

import java.util.HashMap;

/**
 * A Utility class for instantiating result instances of a SQL query.
 * This class is package private as it is not intended to be used globally.
 */
class ResultResource {
  private HashMap<String, String> table;
  private Resource resource;

  ResultResource(int columnCount) {
    table = new HashMap<>(columnCount);
  }

  ResultResource() {
    table = new HashMap<>();
  }

  Resource getResource() {
    return resource;
  }

  /**
   * Provides the value of a specific column.
   * @param columnName The name of the column to get the value from.
   * @return The value of the specified column name; Null if the column couldn't be found.
   */
  String getValueByColmnName(String columnName) {
    String key = D2rUtil.getFieldNameUpperCase(columnName);
    return table.get(key);
  }

  void put(String columnName, String value) {
    String key = D2rUtil.getFieldNameUpperCase(columnName);
    table.put(key, value);
  }

  void setResource(Resource resource) {
    this.resource = resource;
  }
}