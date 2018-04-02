package de.unipassau.medspace.common.message;

/**
 * A base class for defining response messages to the client.
 * If a service is called on the register the client will be performed whether the call was successfully executed and
 * a response message that describes the result in more detail.
 */
public class Response {

  /**
   * Used to store whether a service call was successful.
   */
  protected  boolean success;

  /**
   * A description of the result.
   */
  protected  String message;

  public Response() {
    success = false;
    message = null;
  }

  public Response(boolean success, String message) {
    this.message = message;
    this.success = success;
  }


  /**
   * Provides the response message, that describes in more detail the success of the service call.
   * @return The response message.
   */
  public String getMessage() {
    return message;
  }

  /**
   * Checks whether the service call was successful.
   * @return true if the service call was successful.
   */
  public boolean getSuccess() {
    return success;
  }
}