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
 * A writer that writes http messages in chunks.
 */
public class HttpChunkedWriter {

  private CompletableFuture<Done> resultFuture;

  /**
   * Creates a new HttpChunkedWriter object.
   * @param source The source byte content.
   * @param out The output stream for writing.
   * @param materializer the materializer to use.
   */
  public HttpChunkedWriter(Source<ByteString, ?> source, OutputStream out, Materializer materializer) {

    // The sink that writes to the output stream
    Sink<ByteString, CompletionStage<Done>> outputWriter =
        Sink.<ByteString>foreach(bytes -> out.write(bytes.toArray()));

    // materialize and run the stream
    resultFuture = source.runWith(outputWriter, materializer)
        .toCompletableFuture();
  }

  /**
   * Provides the result future.
   * @return the result future.
   */
  public CompletableFuture<Done> future() {
    return resultFuture;
  }

}