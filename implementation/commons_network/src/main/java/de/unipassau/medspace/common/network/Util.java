package de.unipassau.medspace.common.network;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;


/**
 * Created by David Goeth on 06.10.2017.
 */
public final class Util {

  private static Logger log = LoggerFactory.getLogger(Util.class);

  public static Response executeAndWait(Supplier<CompletionStage<? extends WSResponse>>
                                            function, int triesOnFailure) {
    assert triesOnFailure >= 1;

    Response response = new Response();

    // try at most three times to get a message
    // If the retrieving is successful leave the loop (-> no more tries)
    for (int i = 1; i <= triesOnFailure; ++i) {
      try {
        // reset defensively the data
        response.setData(null);
        response.setException(null);

        CompletionStage<? extends WSResponse> responsePromise = function.get();
        CompletionStage<JsonNode> jsonResponse = responsePromise.thenApply(WSResponse::asJson);
        response.setData(jsonResponse.toCompletableFuture().get());

        // retrieving was successful -> leave the loop
        break;
      } catch (ExecutionException | InterruptedException e) {
        log.warn("Couldn't retrieve data at try " +  i);
        log.debug("Cause", e);
        response.setException(e);
      }
    }

    return response;
  }

  public static Response getAndWait(WSRequest request, int triesOnFailure) {
    return executeAndWait(() -> request.get(), triesOnFailure);
  }

  public static Response postAndWait(WSRequest request, JsonNode body, int triesOnFailure) {
    return executeAndWait(()-> request.post(body), triesOnFailure);
  }
}