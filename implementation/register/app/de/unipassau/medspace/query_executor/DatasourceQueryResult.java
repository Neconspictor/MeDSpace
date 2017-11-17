package de.unipassau.medspace.query_executor;

import akka.Done;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import de.unipassau.medspace.common.network.HttpChunkedWriter;
import de.unipassau.medspace.common.register.Datasource;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.common.util.StringUtil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.*;

/**
 * Created by David Goeth on 14.10.2017.
 */
public class DatasourceQueryResult implements Closeable {

  private static final String base = "./_work/query-executor/temp/";

  private final Query query;
  private CompletableFuture<File> resultFuture;
  private File resultFile = null;
  private boolean isOpen = true;
  private String contentType;

  public DatasourceQueryResult(Query query, Datasource datasource, Source<ByteString, ?> source,
                               String contentType, Materializer materializer) throws IOException {
    this.query = query;

    //assure that the base directory exists
    FileUtil.createDirectory(base);

    File file;

    do {
      String encoded = StringUtil.encodeString(datasource.getUrl().toExternalForm()
          + query.hashCode()
          +  System.nanoTime());

      file = new File(base + "Result" +  encoded);
    } while(file.exists());

    file.createNewFile();
    OutputStream outputStream = java.nio.file.Files.newOutputStream(file.toPath());
    final File finalCopy = file;

    HttpChunkedWriter myWriter = new HttpChunkedWriter(source, outputStream, materializer);

    resultFuture = myWriter.future()
        .whenComplete((value, error) -> {
        // Close the output stream whether there was an error or not
        try {
          setResultFile(finalCopy);
          outputStream.close();
        }
        catch(IOException e) {}
      })
        .thenApply(v -> resultFile)
        .toCompletableFuture();

    this.contentType = contentType;
  }

  public CompletableFuture<File> future() {
    return resultFuture;
  }

  public Query getQuery() {
    return query;
  }

  public synchronized void cleanup() throws IOException {
    if (!isOpen) return;
    isOpen = false;
    if (resultFile == null) {
      boolean cancelled = resultFuture.cancel(true);
      if (!cancelled) throw new IOException("Couldn't cancel resultFuture!");
      return;
    }
    try {
      Files.delete(resultFile.toPath());
    } catch (SecurityException e) {
      throw new IOException("Couldn't delete result file", e);
    }
  }

  private synchronized void setResultFile(File file) {
    resultFile = file;
  }

  public String getContentType() {
    return contentType;
  }

  @Override
  public void close() throws IOException {
    cleanup();
  }
}