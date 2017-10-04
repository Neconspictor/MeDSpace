package de.unipassau.medspace.wrapper.sqlwrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigException;
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
 * Created by David Goeth on 04.10.2017.
 */
public class TestClient implements WSBodyReadables, WSBodyWritables {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(TestClient.class);

  private final WSClient ws;

  @Inject
  public TestClient(WSClient ws) {
    this.ws = ws;
  }

  public boolean deRegister(Datasource datasource, URL registerBase) {

    if (datasource == null) throw new NullPointerException("datasource mustn't be null!");
    if (registerBase == null) throw new NullPointerException("registerBase mustn't be null!");

    JsonNode serialized = Json.toJson(datasource);

    String addPath = "remove";
    URL removeServiceURL;
    try {
      removeServiceURL = new URL(registerBase, addPath);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Couldn't construct url to the register add service!", e);
    }

    WSRequest request = ws.url(removeServiceURL.toExternalForm())
        .setRequestTimeout(Duration.of(30, ChronoUnit.SECONDS))
        .setFollowRedirects(true);

    CompletionStage<? extends WSResponse> responsePromise = request.post(serialized);

    CompletionStage<JsonNode> jsonResponse = responsePromise.thenApply(WSResponse::asJson);
    JsonNode data;
    try {
      data = jsonResponse.toCompletableFuture().get();
    } catch (ExecutionException | InterruptedException e) {
      log.error("Error while sending/retrieving data from the register", e);
      return false;
    }

    Response response = Json.fromJson(data, Response.class);
    if (response == null) {
      log.error("Couldn't convert server response to an Response object: " + data.toString());
      return false;
    }

    if (!response.getResult())
      log.error("Message from the register: " + response.getResponse());
    else
      log.debug("Message from the register: " + response.getResponse());


    return response.getResult();
  }

  public boolean register(Datasource datasource, URL registerBase) {

    if (datasource == null) throw new NullPointerException("datasource mustn't be null!");
    if (registerBase == null) throw new NullPointerException("registerBase mustn't be null!");

    JsonNode serialized = Json.toJson(datasource);

    String addPath = "add";
    URL addServiceURL;
    try {
      addServiceURL = new URL(registerBase, addPath);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Couldn't construct url to the register add service!", e);
    }

    WSRequest request = ws.url(addServiceURL.toExternalForm())
        .setRequestTimeout(Duration.of(30, ChronoUnit.SECONDS))
        .setFollowRedirects(true);

    CompletionStage<? extends WSResponse> responsePromise = request.post(serialized);

    CompletionStage<JsonNode> jsonResponse = responsePromise.thenApply(WSResponse::asJson);
    JsonNode data;
    try {
      data = jsonResponse.toCompletableFuture().get();
    } catch (ExecutionException | InterruptedException e) {
      log.error("Error while sending/retrieving data from the register", e);
      return false;
    }

    Response response = Json.fromJson(data, Response.class);
    if (response == null) {
      log.error("Couldn't convert server response to an Response object: " + data.toString());
      return false;
    }

    if (!response.getResult())
      log.error("Message from the register: " + response.getResponse());
    else
      log.debug("Message from the register: " + response.getResponse());


    return response.getResult();
  }
}