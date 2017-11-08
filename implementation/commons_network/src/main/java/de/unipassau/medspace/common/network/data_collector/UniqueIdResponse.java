package de.unipassau.medspace.common.network.data_collector;

import java.math.BigInteger;

/**
 * Created by David Goeth on 07.11.2017.
 */
public class UniqueIdResponse {

  private final BigInteger id;

  public UniqueIdResponse(BigInteger id) {
    this.id = id;
  }

  public BigInteger getId() {
    return id;
  }
}