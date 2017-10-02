package controllers.response;

/**
 * A base class for defining response messages to the client.
 * If a service is called on the register the client will be performed whether the call was successfully executed and
 * a response message that describes the result in more detail.
 */
public class Response {

  /**
   * Used to store whether a service call was successful.
   */
  protected  boolean result;

  /**
   * A description of the result.
   */
  protected  String response;

  public Response() {
    result = false;
    response = null;
  }

  public Response(boolean result, String response) {
    this.response = response;
    this.result = result;
  }


  /**
   * Provides the response message, that describes in more detail the result of the service call.
   * @return The response message.
   */
  public String getResponse() {
    return response;
  }

  /**
   * Checks whether the service call was successful.
   * @return true if the service call was successful.
   */
  public boolean getResult() {
    return result;
  }
}