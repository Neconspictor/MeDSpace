package de.unipassau.medspace.wrapper.sqlwrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.*;

import javax.inject.Inject;
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

  public boolean register() {

    ObjectNode test = Json.newObject();
    test.put("url", "http://test.com/datasourceTest");
    test.put("description", "Test datasource send by WSClient.");
    test.put("services[0]", "KEYWORD");
    test.put("services[1]", "ADVANCED_KEYWORD");


    WSRequest request = ws.url("http://localhost:9500/add")
        .setRequestTimeout(Duration.of(30, ChronoUnit.SECONDS))
        .setFollowRedirects(true);

    CompletionStage<? extends WSResponse> responsePromise = request.post(test);

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

    if (!response.isResult())
      log.error("Message from the register: " + response.getResponse());
    else
      log.debug("Message from the register: " + response.getResponse());


    return response.isResult();
  }

  public static class Response {
    private boolean result;
    private String response;

    public boolean isResult() {
      return result;
    }

    public void setResult(boolean result) {
      this.result = result;
    }

    public String getResponse() {
      return response;
    }

    public void setResponse(String response) {
      this.response = response;
    }
  }
}