package controllers;

import akka.stream.javadsl.*;
import akka.util.ByteString;
import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.network.data_collector.UniqueIdResponse;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.stream.TripleInputStream;
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
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

import scala.concurrent.duration.FiniteDuration;

/**
 * TODO
 */
public class DataCollectorController extends Controller {

  /**
   * TODO
   */
  private final DataCollector dataCollector;

  /**
   * TODO
   */
  private final RDFProvider provider;

  /**
   * TODO
   */
  private ExecutorService executor;

  /**
   * Logger instance for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(DataCollectorController.class);

  @Inject
  public DataCollectorController(DataCollector dataCollector, RDFProvider provider) {
    this.dataCollector = dataCollector;
    this.provider = provider;
    executor = Executors.newFixedThreadPool(8);
  }

  /**
   * TODO
   * @return
   * @throws IOException
   */
  public Result createQueryResult() throws IOException {
    BigInteger id = dataCollector.createQueryResult();
    return ok(Json.toJson(new UniqueIdResponse(id)));
  }

  /**
   * TODO
   * @param resultID
   * @param rdfFormat
   * @param baseURI
   * @return
   */
  @BodyParser.Of(InputStreamBodyParser.class)
  public CompletionStage<Result> addPartialQueryResult(String resultID, String rdfFormat, String baseURI) {

    InputStream in = request().body().as(InputStream.class);
    return CompletableFuture.supplyAsync((()->
            addPartialQueryResultAction(resultID, rdfFormat, baseURI, in)), executor);
  }

  /**
   * TODO
   * @param resultID
   * @return
   */
  public Result deleteQueryResult(String resultID) {
    BigInteger id;
    try {
      id = new BigInteger(resultID);
    } catch (NumberFormatException e) {
      return badRequest("resultID isn't a valid big integer");
    }

    boolean resultIdExists;

    try {
      resultIdExists = dataCollector.deleteQueryResult(id);
    } catch (IOException | NoValidArgumentException e) {
      String errorMessage = "Couldn't delete query result with id '" + id + "'";
      log.error(errorMessage, e);
      return internalServerError(errorMessage);
    }

    if (resultIdExists)
      return ok("Query result with id '" + id + "' successfully deleted.");
    else
      return badRequest("Query result with id '" + id + "' doesn't exist");
  }

  /**
   * TODO
   * @param resultID
   * @param rdfFormat
   * @return
   */
  public Result queryResult(String resultID, String rdfFormat) {
    BigInteger id;
    try {
      id = new BigInteger(resultID);
    } catch (NumberFormatException e) {
      return badRequest("resultID isn't a valid big integer");
    }

    if (!provider.isValid(rdfFormat)) {
      return badRequest("rdfFormat '" + rdfFormat + "' isn't a supported rdf format");
    }

    Stream<Triple> triples;
    InputStream in;
    try {
      triples = dataCollector.queryResult(id, rdfFormat);
      Set<Namespace> namespaces = dataCollector.getNamespaces(id);
      // TODO get namesapces from the repository
      in = new TripleInputStream(triples, rdfFormat, namespaces, provider.getWriterFactory());
    } catch (IOException | NoValidArgumentException e) {
      String errorMessage = "Error while retrieving query result for resultID=" + id;
      log.error(errorMessage, e);
      return internalServerError(errorMessage + ": " + e.getMessage());
    }

    return ok(in);
  }

  /**
   * TODO
   * @param resultID
   * @param rdfFormat
   * @param baseURI
   * @param in
   * @return
   */
  private Result addPartialQueryResultAction(String resultID, String rdfFormat, String baseURI, InputStream in) {
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
    try {
      dataCollector.addPartialQueryResult(id, in, rdfFormat, baseURI);
    } catch (IOException | NoValidArgumentException e) {
      log.error("An error occurred while collecting a partial query result", e);
      return internalServerError("Couldn't collect partial query result: " + e.getMessage());
    }

    return ok("Done!");
  }

  /**
   * TODO
   */
  public static class InputStreamBodyParser implements BodyParser<InputStream> {

    /**
     * TODO
     */
    private Executor executor;

    /**
     * TODO
     * @param executor
     */
    @Inject
    public InputStreamBodyParser(Executor executor) {
      this.executor = executor;
    }

    /**
     * TODO
     * @param request
     * @return
     */
    @Override
    public Accumulator<ByteString, F.Either<Result, InputStream>> apply(Http.RequestHeader request) {

      Sink<ByteString, InputStream> streamSink = StreamConverters.asInputStream(
          new FiniteDuration(5, TimeUnit.SECONDS));
      Sink<ByteString, CompletionStage<InputStream>> test = streamSink.mapMaterializedValue(
          (in)->CompletableFuture.completedFuture(in));

      return Accumulator.fromSink(test).map(F.Either::Right, executor);
    }
  }
}