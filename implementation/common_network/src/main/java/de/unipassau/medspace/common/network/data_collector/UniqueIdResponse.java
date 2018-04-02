package de.unipassau.medspace.common.network.data_collector;

import java.math.BigInteger;

/**
 * A response message for a unique id.
 */
public class UniqueIdResponse {

  private BigInteger id;

  /**
   * Creates a new UniqueIdResponse object.
   */
  public UniqueIdResponse() {
    id = null;
  }

  /**
   * Creates a new UniqueIdResponse object.
   * @param id The id for this response.
   */
  public UniqueIdResponse(BigInteger id) {
    this.id = id;
  }

  /**
   * Provides the ID.
   * @return the ID.
   */
  public BigInteger getId() {
    return id;
  }

  /**
   * Sets the ID.
   * @param id the ID.
   */
  public void setId(BigInteger id) {
    this.id = id;
  }
}