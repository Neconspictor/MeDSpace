package de.unipassau.medspace.common.network;


import akka.stream.IOResult;
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

  public static WSResponse postInputStreamAndWait(WSRequest request, InputStream in, int triesOnFailure) {

    Source<ByteString, ?> source = StreamConverters.fromInputStream(()->in);

    //return executeAndWait(()->request.post(new MyBodyWritable(in)), triesOnFailure);
    return executeAndWait(()->request.post(new SourceBodyWritable(source)), triesOnFailure);
  }

  public static InputStream getGETInputStream(WSRequest request) throws IOException {
    StringBuilder builder = new StringBuilder();
    String query = "";
    Map<String, List<String>> parameters = request.getQueryParameters();
    for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
      List<String> values = entry.getValue();
      if (values != null && values.size() > 0) {
        String value  = java.net.URLEncoder.encode(values.get(0),"UTF-8");
        builder.append(entry.getKey() + "=" + value + "&");
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