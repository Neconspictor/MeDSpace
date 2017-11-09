package controllers;

import akka.stream.IOResult;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;
import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.network.data_collector.UniqueIdResponse;
import de.unipassau.medspace.common.rdf.RDFProvider;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * Created by David Goeth on 07.11.2017.
 */
public class DataCollectorController extends Controller {

  private final DataCollector dataCollector;

  private static final String TEMP = "./_work/data_collector/temp/";

  private final RDFProvider provider;

  /**
   * Logger instance for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(DataCollectorController.class);

  @Inject
  public DataCollectorController(DataCollector dataCollector, RDFProvider provider) {
    this.dataCollector = dataCollector;
    this.provider = provider;
  }

  public Result createUniqueQueryResultID() {
    BigInteger id = dataCollector.createUniqueQueryResultID();
    return ok(Json.toJson(new UniqueIdResponse(id)));
  }

  @BodyParser.Of(MyBodyParser.class)
  public CompletionStage<Result> addPartialQueryResult(String resultID, String rdfFormat, String baseURI)
      throws IOException, NoValidArgumentException {
    Http.Request request = request();

    File file = request.body().as(File.class);

    return CompletableFuture.supplyAsync(()-> addPartialQueryResultAction(resultID, rdfFormat, baseURI, file))
        .whenComplete((result, error)-> file.delete());
  }

  private Result addPartialQueryResultAction(String resultID, String rdfFormat, String baseURI, File file) {
    //BigInteger resultID, String rdfFormat, String baseURI
    //String resultIDString = request().getQueryString("resultID");
    BigInteger id;
    try {
      id = new BigInteger(resultID);
    } catch (NumberFormatException e) {
      return badRequest("resultID isn't a valid big integer");
    }

    if (!provider.isValid(rdfFormat)) {
      return badRequest("rdfFormat '" + rdfFormat + "' isn't a supported rdf format");
    }

    //TODO check baseURI ???
    try(FileInputStream in = new FileInputStream(file)) {
      dataCollector.addPartialQueryResult(id, in, rdfFormat, baseURI);
    } catch (IOException | NoValidArgumentException e) {
      log.error("An error occurred while collecting a partial query result", e);
      return internalServerError("Couldn't collect partial query result");
    }

    return ok(file.getAbsolutePath() + ", id= " + id + ", rdfFormat= '" + rdfFormat + "', baseURI= " + baseURI);
  }




  private static class MyBodyParser implements BodyParser<File> {

    private Executor executor;

    @Inject
    public MyBodyParser(Executor executor) {
      this.executor = executor;
    }


    @Override
    public Accumulator<ByteString, F.Either<Result, File>> apply(Http.RequestHeader request) {


      Map map = request.getHeaders().toMap();
      String fileName = createUniqueFileName();
      final File file = new File(fileName);

      final Sink<ByteString, CompletionStage<IOResult>> sink = FileIO.toFile(file);

      return Accumulator.fromSink(sink).map(ioResult -> {
        System.out.println(ioResult);
        if (ioResult.wasSuccessful()) {
          return F.Either.Right(file);
        }
        Result error =  internalServerError("Couldn't accumulate to file " + file.getAbsolutePath());
        return F.Either.Left(error);
      }, executor);
    }

    private String createUniqueFileName() {
      Random random = new Random();
      byte[] bytes = new byte[16];
      random.nextBytes(bytes);
      String fileName = bytesToHex(bytes);
      return TEMP + fileName;
    }

    private String bytesToHex(byte[] hash) {
      StringBuffer hexString = new StringBuffer();
      for (int i = 0; i < hash.length; i++) {
        String hex = Integer.toHexString(0xff & hash[i]);
        if(hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    }
  }
}