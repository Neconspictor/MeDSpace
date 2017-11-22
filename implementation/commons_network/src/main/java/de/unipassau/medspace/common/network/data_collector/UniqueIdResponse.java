package de.unipassau.medspace.common.network.data_collector;

import java.math.BigInteger;

/**
 * Created by David Goeth on 07.11.2017.
 */
public class UniqueIdResponse {

  private BigInteger id;

  public UniqueIdResponse() {
    id = null;
  }

  public UniqueIdResponse(BigInteger id) {
    this.id = id;
  }

  public BigInteger getId() {
    return id;
  }

  public void setId(BigInteger id) {
    this.id = id;
  }
}