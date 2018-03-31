package de.unipassau.medspace.query_executor;

import de.unipassau.medspace.common.register.Service;

/**
 * Represents a query.
 */
public class Query {

  private final Service service;

  private final String queryString;

  /**
   * Creates a Query new object.
   * @param service The service to call.
   * @param queryString The query.
   */
  public Query(Service service, String queryString) {
    this.service = service;
    this.queryString = queryString;
  }

  /**
   * Provides the service.
   * @return the service.
   */
  public Service getService() {
    return service;
  }

  /**
   * Provides the query.
   * @return the query.
   */
  public String getQueryString() {
    return queryString;
  }
}