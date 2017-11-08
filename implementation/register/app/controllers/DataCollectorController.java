package controllers;

import akka.stream.IOResult;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import de.unipassau.medspace.common.network.data_collector.UniqueIdResponse;
import de.unipassau.medspace.data_collector.DataCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.F;
import play.libs.Json;
import play.libs.streams.Accumulator;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * Created by David Goeth on 07.11.2017.
 */
public class DataCollectorController extends Controller {

  private final DataCollector dataCollector;

  /**
   * Logger instance for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(DataCollectorController.class);

  @Inject
  public DataCollectorController(DataCollector dataCollector) {
    this.dataCollector = dataCollector;
  }

  public Result createUniqueQueryResultID() {
    BigInteger id = dataCollector.createUniqueQueryResultID();
    return ok(Json.toJson(new UniqueIdResponse(id)));
  }

  @BodyParser.Of(MyBodyParser.class)
  public Result addPartialQueryResult() {
    //BigInteger resultID, String rdfFormat, String baseURI
    File file = request().body().as(File.class);
    return ok(file.getAbsolutePath());
  }




  private static class MyBodyParser implements BodyParser<File> {

    private Executor executor;

    @Inject
    public MyBodyParser(Executor executor) {
      this.executor = executor;
    }


    @Override
    public Accumulator<ByteString, F.Either<Result, File>> apply(Http.RequestHeader request) {

      final File file = new File("./_work/data_collector/test/accumulation");

      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }

      final Sink<ByteString, CompletionStage<IOResult>> sink = FileIO.toFile(file);

      return Accumulator.fromSink(sink).map(ioResult -> {
        if (ioResult.wasSuccessful()) {
          return F.Either.Right(file);
        }
        Result error =  internalServerError("Couldn't accumulate to file " + file.getAbsolutePath());
        return F.Either.Left(error);
      }, executor);
    }
  }
}