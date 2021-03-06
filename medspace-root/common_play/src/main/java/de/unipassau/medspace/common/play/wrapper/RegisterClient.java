package de.unipassau.medspace.common.play.wrapper;

import com.fasterxml.jackson.databind.JsonNode;
import de.unipassau.medspace.common.message.Response;
import de.unipassau.medspace.common.register.Datasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.*;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

/**
 * This is a helper for communicating with the MeDSpace Register module.
 */
public class RegisterClient implements WSBodyReadables, WSBodyWritables {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(RegisterClient.class);


  private static final String REGISTER_ADD_SERVICE_SUBPATH = "add";


  private static final String REGISTER_REMOVE_SERVICE_SUBPATH = "remove";


  private final WSClient ws;

  /**
   * Creates a new RegisterClient object.
   * @param ws The websocket client used for networking.
   */
  @Inject
  public RegisterClient(WSClient ws) {
    this.ws = ws;
  }

  /**
   * Invokes the deRegister REST service of the Register for a datasource.
   *
   * @param datasource The datasource.
   * @param registerBase The base URL of the register.
   * @return true if the service call was successful
   */
  public boolean deRegister(Datasource datasource, URL registerBase) {
    return invokeRegisterService(registerBase, REGISTER_REMOVE_SERVICE_SUBPATH, datasource);
  }

  /**
   * Invokes the register REST service of the Register for a datasource.
   *
   * @param datasource The datasource.
   * @param registerBase The base URL of the register.
   * @return true if the service call was successful
   */
  public boolean register(Datasource datasource, URL registerBase) {
    return invokeRegisterService(registerBase, REGISTER_ADD_SERVICE_SUBPATH, datasource);
  }


  private boolean invokeRegisterService(URL registerBase, String serviceSubPath, Datasource datasource) {
    if (datasource == null) throw new NullPointerException("datasource mustn't be null!");
    if (registerBase == null) throw new NullPointerException("registerBase mustn't be null!");

    JsonNode serialized = Json.toJson(new Datasource.Builder(datasource));

    URL addServiceURL;
    try {
      addServiceURL = new URL(registerBase, serviceSubPath);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Couldn't construct url to the register add service!", e);
    }

    WSRequest request = ws.url(addServiceURL.toExternalForm())
        .setRequestTimeout(Duration.of(5, ChronoUnit.SECONDS))
        .setFollowRedirects(true);

    Result result = postAndWait(request, serialized, 3);

    if (result.exception != null) {
      log.error("Error while sending/retrieving data from the register", result.exception);
      return false;
    }

    Response response = Json.fromJson(result.data, Response.class);
    if (response == null) {
      log.error("Couldn't convert server message to an Response object: " + result.data.toString());
      return false;
    }

    if (!response.getSuccess())
      log.error("Message from the register: " + response.getMessage());
    else
      log.debug("Message from the register: " + response.getMessage());


    return response.getSuccess();
  }


  private Result postAndWait(WSRequest request, JsonNode body, int triesOnFailure) {

    assert triesOnFailure >= 1;

    Result result = new Result();

    // try at most three times to get a message
    // If the retrieving is successful leave the loop (-> no more tries)
    for (int i = 1; i <= triesOnFailure; ++i) {
      try {
        // reset defensively the data
        result.data = null;
        result.exception = null;

        CompletionStage<? extends WSResponse> responsePromise = request.post(body);
        CompletionStage<JsonNode> jsonResponse = responsePromise.thenApply(WSResponse::asJson);
        result.data = jsonResponse.toCompletableFuture().get();

        // retrieving was successful -> leave the loop
        break;
      } catch (ExecutionException | InterruptedException e) {
        log.warn("Couldn't retrieve data at try " +  i);
        log.debug("Cause", e);
        result.exception = e;
      }
    }

    return result;
  }

  /**
   * A simple data container for storing Json data and an optional exception that was thrown while
   * retrieving the data.
   */
  private static class Result {

    /**
     * The json data of the result.
     */
    public JsonNode data; // public modifier as this class contains no logic.

    /**
     * The thrown exception (if one has occurred).
     */
    public Exception exception;
  }
}