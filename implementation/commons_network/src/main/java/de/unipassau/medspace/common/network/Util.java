package de.unipassau.medspace.common.network;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;


/**
 * Created by David Goeth on 06.10.2017.
 */
public final class Util {

  private static Logger log = LoggerFactory.getLogger(Util.class);

  public static JsonResponse executeAndWaitJson(Supplier<CompletionStage<? extends WSResponse>>
                                            function, int triesOnFailure) {
    assert triesOnFailure >= 1;

    JsonResponse response = new JsonResponse();

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

  public static WSResponse executeAndWait(Supplier<CompletionStage<? extends WSResponse>>
                                                    function, int triesOnFailure) {
    assert triesOnFailure >= 1;

    WSResponse response = null;

    // try at most three times to get a message
    // If the retrieving is successful leave the loop (-> no more tries)
    for (int i = 1; i <= triesOnFailure; ++i) {
      try {
        // reset defensively the data

        CompletionStage<? extends WSResponse> responsePromise = function.get();
        response = responsePromise.toCompletableFuture().get();

      } catch (ExecutionException | InterruptedException e) {
        log.warn("Couldn't retrieve data at try " +  i);
        log.debug("Cause", e);
      }
    }

    return response;
  }

  public static WSResponse getAndWait(WSRequest request, int triesOnFailure) {
    return executeAndWait(() -> request.get(), triesOnFailure);
  }

  public static JsonResponse getAndWaitJson(WSRequest request, int triesOnFailure) {
    return executeAndWaitJson(() -> request.get(), triesOnFailure);
  }

  public static JsonResponse postAndWaitJson(WSRequest request, JsonNode body, int triesOnFailure) {
    return executeAndWaitJson(()-> request.post(body), triesOnFailure);
  }

  public static WSResponse postFileAndWait(WSRequest request, File file, int triesOnFailure) {
    return executeAndWait(()->request.post(file), triesOnFailure);
  }

  public static InputStream getGETInputStream(WSRequest request) throws IOException {
    URLConnection connection = new URL(request.getUrl()).openConnection();
    connection.setRequestProperty("Accept-Charset", "UTF-8");
    return connection.getInputStream();
  }
}