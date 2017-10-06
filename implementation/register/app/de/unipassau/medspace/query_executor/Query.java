package de.unipassau.medspace.query_executor;

import de.unipassau.medspace.common.register.Datasource;

/**
 * A dummy class for a base Query class
 */
public class Query {

  private final String name;

  public Query(String name) {
    this.name = name.toLowerCase();
  }

  public static boolean supportsQuery(Query query, Datasource datasource) {
    return datasource.getServices().stream().anyMatch(service ->
        service.toLowerCase().equals(query.name));
  }

  public String getName() {
    return name;
  }
}