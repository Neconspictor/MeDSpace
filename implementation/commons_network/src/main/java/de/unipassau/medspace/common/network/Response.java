package de.unipassau.medspace.common.network;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A simple data container for storing Json data and an optional exception that was thrown while
 * retrieving the data.
 */
public class Response {
  private JsonNode data; // public modifier as this class contains no logic.
  private Exception exception;

  public JsonNode getData() {
    return data;
  }

  public void setData(JsonNode data) {
    this.data = data;
  }

  public Exception getException() {
    return exception;
  }

  public void setException(Exception exception) {
    this.exception = exception;
  }
}