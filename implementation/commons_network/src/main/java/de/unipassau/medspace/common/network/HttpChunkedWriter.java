package de.unipassau.medspace.common.network;

import akka.Done;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Created by David Goeth on 17.11.2017.
 */
public class HttpChunkedWriter {

  private CompletableFuture<Done> resultFuture;

  public HttpChunkedWriter(Source<ByteString, ?> source, OutputStream out, Materializer materializer) {

    // The sink that writes to the output stream
    Sink<ByteString, CompletionStage<Done>> outputWriter =
        Sink.<ByteString>foreach(bytes -> out.write(bytes.toArray()));

    // materialize and run the stream
    resultFuture = source.runWith(outputWriter, materializer)
        .toCompletableFuture();
  }

  public CompletableFuture<Done> future() {
    return resultFuture;
  }

}