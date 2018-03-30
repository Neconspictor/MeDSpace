package de.unipassau.medspace.common.network;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A simple data container for storing Json data and an optional exception that was thrown while
 * retrieving the data.
 */
public class JsonResponse {
  private JsonNode data = null;
  private Exception exception = null;

  /**
   * Provides the json data.
   * @return the json data.
   */
  public JsonNode getData() {
    return data;
  }

  /**
   * Sets the json data.
   * @param data the json data.
   */
  public void setData(JsonNode data) {
    this.data = data;
  }

  /**
   * Provides the thrown exception.
   * @return the thrown exception.
   */
  public Exception getException() {
    return exception;
  }

  /**
   * Sets the thrown exception
   * @param exception the thrown exception
   */
  public void setException(Exception exception) {
    this.exception = exception;
  }
}