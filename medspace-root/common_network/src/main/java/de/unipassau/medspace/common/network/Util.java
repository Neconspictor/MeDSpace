package de.unipassau.medspace.common.network;


import akka.stream.javadsl.Source;
import akka.stream.javadsl.StreamConverters;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.ws.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;


/**
 * Utility methods useful when doing network communication.
 */
public final class Util {

  private static Logger log = LoggerFactory.getLogger(Util.class);

  /**
   * Executes a function that provides a completion stage object for a Json object.
   * Then this method waits until the response has been arrived.
   * @param function The function to call.
   * @param triesOnFailure Maximal tries if an error occurrs while waiting for a response.
   * @return The Json response message.
   */
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

  /**
   * Executes a function that provides a completion stage object for a WSResponse object.
   * Then this method waits until the response has been arrived.
   * @param function The function to call.
   * @param triesOnFailure Maximal tries if an error occurrs while waiting for a response.
   * @return The response message.
   *
   * @throws IOException if the response message couldn't be retrieved after  (triesOnFailure + 1) tries.
   */
  public static WSResponse executeAndWait(Supplier<CompletionStage<? extends WSResponse>>
                                                    function, int triesOnFailure) throws IOException {
    assert triesOnFailure >= 1;

    WSResponse response = null;
    Exception thrownException = null;

    // try at most three times to get a message
    // If the retrieving is successful leave the loop (-> no more tries)
    for (int i = 1; i <= triesOnFailure; ++i) {
      try {
        // reset defensively the data

        CompletionStage<? extends WSResponse> responsePromise = function.get();
        response = responsePromise.toCompletableFuture().get();

        // if get() doesn't throw any exception, we needn't try anymore!
        break;

      } catch (ExecutionException | InterruptedException e) {
        log.warn("Couldn't retrieve data at try " +  i);
        log.debug("Cause", e);
        thrownException = e;
      }
    }

    //check errors
    if (response == null) {
      throw new IOException("Couldn't retrieve response. Last error was: ", thrownException);
    }

    return response;
  }

  /**
   * Executes a http request using the GET method. This methods waits for the response.
   * @param request The http request
   * @param triesOnFailure Maximal tries if an error occurrs while waiting for a response.
   * @return the response message
   * @throws IOException if the response message couldn't be retrieved after  (triesOnFailure + 1) tries.
   */
  public static WSResponse getAndWait(WSRequest request, int triesOnFailure) throws IOException {
    return executeAndWait(() -> request.get(), triesOnFailure);
  }

  /**
   * Executes a http request using the GET method. It is expected that the response message will be a JSON response.
   * This methods waits for the response.
   * @param request The http request
   * @param triesOnFailure Maximal tries if an error occurrs while waiting for a response.
   * @return the response message
   */
  public static JsonResponse getAndWaitJson(WSRequest request, int triesOnFailure) {
    return executeAndWaitJson(() -> request.get(), triesOnFailure);
  }

  /**
   * Executes a http request using the POST method.
   * This methods waits for the response.
   *
   * @param request The http request
   * @param body The http POST body
   * @param triesOnFailure Maximal tries if an error occurrs while waiting for a response.
   * @return the response message
   * @throws IOException if the response message couldn't be retrieved after  (triesOnFailure + 1) tries.
   */
  public static WSResponse postJsonAndWait(WSRequest request, JsonNode body, int triesOnFailure) throws IOException {
    return executeAndWait(()-> request.post(body), triesOnFailure);
  }

  /**
   * Executes a http request using the POST method.
   * It is expected that the response message will be a JSON response.
   * This methods waits for the response.
   *
   * @param request The http request
   * @param body The http POST body
   * @param triesOnFailure Maximal tries if an error occurrs while waiting for a response.
   * @return the response message
   */
  public static JsonResponse postAndWaitJson(WSRequest request, JsonNode body, int triesOnFailure) {
    return executeAndWaitJson(()-> request.post(body), triesOnFailure);
  }

  /**
   * Executes a http request using the POST method. As POST body a file will be included.
   * @param request The http request
   * @param file Will be set as POST the body
   * @param triesOnFailure Maximal tries if an error occurrs while waiting for a response.
   * @return the response message
   * @throws IOException if the response message couldn't be retrieved after  (triesOnFailure + 1) tries.
   */
  public static WSResponse postFileAndWait(WSRequest request, File file, int triesOnFailure) throws IOException {

    return executeAndWait(()->request.post(file), triesOnFailure);
  }

  /**
   * Sends content from an input stream using http POST.
   * The input stream will be sent by using chunked http messages.
   * This methods waits for the response.
   *
   * @param request The http request
   * @param in The content to sent.
   * @param triesOnFailure Maximal tries if an error occurrs while waiting for a response.
   * @return the response message
   * @throws IOException if the response message couldn't be retrieved after  (triesOnFailure + 1) tries.
   */
  public static WSResponse postInputStreamAndWait(WSRequest request, InputStream in, int triesOnFailure)
      throws IOException {

    Source<ByteString, ?> source = StreamConverters.fromInputStream(()->in);

    //return executeAndWait(()->request.post(new MyBodyWritable(in)), triesOnFailure);
    return executeAndWait(()->request.post(new SourceBodyWritable(source)), triesOnFailure);
  }

  /**
   * Executes a http request using the GET method.
   * The result of the request is returned as an input stream.
   * @param request The http request to execute.
   * @return The response message.
   * @throws IOException If an io error occurs.
   */
  public static InputStream getGETInputStream(WSRequest request) throws IOException {
    StringBuilder builder = new StringBuilder();
    String query = "";
    Map<String, List<String>> parameters = request.getQueryParameters();
    for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
      List<String> values = entry.getValue();
      if (values != null && values.size() > 0) {
        String unencoded = values.get(0);
        if (unencoded == null) unencoded = "";
        String encodedValue  = java.net.URLEncoder.encode(unencoded,"UTF-8");
        builder.append(entry.getKey() + "=" + encodedValue + "&");
      }
    }

    if (builder.length() > 0) {
      builder = builder.delete(builder.length() - "&".length(), builder.length());
      query = builder.toString();
    }

    String url = request.getUrl().toString() + "?" + query;

    URLConnection connection = new URL(url).openConnection();
    //connection.setRequestProperty("Accept-Charset", "UTF-8");
    return connection.getInputStream();
  }

  /*private static Source<ByteString, ?> fromInputStream(InputStream in) {


    // Prepare a chunked text stream
    Source<ByteString, ?> source = Source.<ByteString>actorRef(256, OverflowStrategy.backpressure())
        .mapMaterializedValue(sourceActor -> {
          byte[] buffer = new byte[256];
          int len = in.read(buffer);
          while(len != -1) {
            sourceActor.tell(ByteString.fromArray(buffer), null);
            len = in.read(buffer);
          }

          sourceActor.tell(new Status.Success(NotUsed.getInstance()), null);
          return NotUsed.getInstance();
        });
  }*/

  private static class MyBodyWritable implements BodyWritable<InputStream> {

    private final WSBody<InputStream> body;

    public MyBodyWritable(InputStream in) {
      this.body = () -> in;
    }

    @Override
    public WSBody<InputStream> body() {
      return body;
    }

    @Override
    public String contentType() {
      return "UTF-8";
    }
  }
}