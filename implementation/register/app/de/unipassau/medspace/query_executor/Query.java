package de.unipassau.medspace.query_executor;

import de.unipassau.medspace.common.register.Service;

/**
 * A dummy class for a base Query class
 */
public class Query {

  private final Service service;

  private final String queryString;

  public Query(Service service, String queryString) {
    this.service = service;
    this.queryString = queryString;
  }

  public Service getService() {
    return service;
  }

  public String getQueryString() {
    return queryString;
  }
}