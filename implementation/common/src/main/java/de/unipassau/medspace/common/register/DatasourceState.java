package de.unipassau.medspace.common.register;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

/**
 * A datasource state is used to store data of a datasource that can change frequently over time.
 * NOTE: This class is immutable.
 */
public class DatasourceState {

  private Timestamp timestamp;

  private int ioErrors;

  /**
   * Creates a new DatasourceState object.
   * @param timestamp The last update of the datasource.
   * @param ioErrors The occurred IO erros of the datasource.
   */
  public DatasourceState(Timestamp timestamp, int ioErrors) {
    this.timestamp = timestamp;
    this.ioErrors = ioErrors;
  }

  /**
   * Creates a new DatasourceState object.
   * @param timestamp The last update of the datasource.
   */
  public DatasourceState(Timestamp timestamp) {
    this.timestamp = timestamp;
    this.ioErrors = 0;
  }


  /**
   * Creates a new DatasourceState object.
   * @param timestamp The last update of the datasource.
   * @param ioErrors The occurred IO erros of the datasource.
   * @return a new DatasourceState object.
   */
  @JsonCreator
  public static DatasourceState create(@JsonProperty("timestamp") Timestamp timestamp,
                                @JsonProperty("ioErrors") int ioErrors) {
    return new DatasourceState(timestamp, ioErrors);
  }

  /**
   * Creates a new DatasourceState object from this object. The created DatasourceState will have an incremented
   * io error counter.
   * @return A DatasourceState with an incremented io error counter.
   */
  public DatasourceState createIncrement() {
    return new DatasourceState(timestamp, ioErrors +1);
  }

  /**
   * Creates a new DatasourceState object from this object. The io error counter of the created DatasourceState
   * will be reset to 0.
   * @return A DatasourceState with an io error counter of zero.
   */
  public DatasourceState createReset() {
    return new DatasourceState(timestamp, 0);
  }

  /**
   * Provides the timestamp.
   * @return the timestamp.
   */
  public Timestamp getTimestamp() {
    return timestamp;
  }

  /**
   * Provides the io error.
   * @return the io error.
   */
  public int getIoErrors() {
    return ioErrors;
  }
}