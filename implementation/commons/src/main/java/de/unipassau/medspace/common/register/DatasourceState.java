package de.unipassau.medspace.common.register;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

/**
 * TODO
 */
public class DatasourceState {

  private Timestamp timestamp;

  private int ioErrors;

  /**
   * TODO
   * @param timestamp
   * @param ioErrors
   */
  public DatasourceState(Timestamp timestamp, int ioErrors) {
    this.timestamp = timestamp;
    this.ioErrors = ioErrors;
  }

  /**
   * TODO
   * @param timestamp
   */
  public DatasourceState(Timestamp timestamp) {
    this.timestamp = timestamp;
    this.ioErrors = 0;
  }


  /**
   * TODO
   * @param timestamp
   * @param ioErrors
   * @return
   */
  @JsonCreator
  public static DatasourceState create(@JsonProperty("timestamp") Timestamp timestamp,
                                @JsonProperty("ioErrors") int ioErrors) {
    return new DatasourceState(timestamp, ioErrors);
  }

  /**
   * TODO
   * @return
   */
  public DatasourceState createIncrement() {
    return new DatasourceState(timestamp, ioErrors +1);
  }

  /**
   * TODO
   * @return
   */
  public DatasourceState createReset() {
    return new DatasourceState(timestamp, 0);
  }

  /**
   * TODO
   * @return
   */
  public Timestamp getTimestamp() {
    return timestamp;
  }

  /**
   * TODO
   * @return
   */
  public int getIoErrors() {
    return ioErrors;
  }
}