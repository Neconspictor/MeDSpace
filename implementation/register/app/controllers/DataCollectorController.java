package controllers;

import akka.stream.*;
import akka.stream.javadsl.*;
import akka.stream.stage.AbstractInHandler;
import akka.stream.stage.GraphStageLogic;
import akka.stream.stage.GraphStageWithMaterializedValue;
import akka.util.ByteString;
import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.network.data_collector.UniqueIdResponse;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.stream.TripleInputStream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.data_collector.DataCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.F;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
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

import de.unipassau.medspace.common.util.StringUtil;
import scala.Tuple2;
import scala.concurrent.duration.FiniteDuration;

/**
 * Created by David Goeth on 07.11.2017.
 */
public class DataCollectorController extends Controller {

  private final DataCollector dataCollector;

  private static final String TEMP = "E:/bachelorThesisTest/_work/data_collector/temp/";

  private final RDFProvider provider;

  private final HttpExecutionContext httpExecutionContext;

  private ExecutorService executor;

  /**
   * Logger instance for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(DataCollectorController.class);

  @Inject
  public DataCollectorController(DataCollector dataCollector, RDFProvider provider,
                                 HttpExecutionContext httpExecutionContext) {
    this.dataCollector = dataCollector;
    this.provider = provider;
    this.httpExecutionContext = httpExecutionContext;
    executor = Executors.newFixedThreadPool(8);
  }

  public Result createQueryResult() {
    BigInteger id = dataCollector.createQueryResult();
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

  @BodyParser.Of(InputStreamBodyParser.class)
  //@BodyParser.Of(MyBodyParser.class)
  public CompletionStage<Result>  addPartialQueryResultInputStream(String resultID, String rdfFormat, String baseURI) {

    InputStream in = request().body().as(InputStream.class);
    return CompletableFuture.supplyAsync(()->test(in), executor);

   // return test(in).thenApplyAsync( result -> ok(result), executor);
    //Http.Request request = request();
    //File file = request.body().as(File.class);
    //return test2(file);
  }


  private Result test2(File file) {

    //TODO check baseURI ???
    FileOutputStream out = null;
    try(FileInputStream in = new FileInputStream(file)) {
      String fileName = "testFile" + new Random().nextInt();
      File outFile = new File(TEMP + fileName);
      outFile.createNewFile();

      out = new FileOutputStream(outFile);
      byte[] buffer = new byte[1024*16];
      int len = in.read(buffer);
      while (len != -1) {
        out.write(buffer, 0, len);
        out.flush();
        len = in.read(buffer);
      }
      out.flush();

    } catch (IOException  e) {
      log.error("An error occurred while collecting a partial query result", e);
      return internalServerError("Couldn't collect partial query result: " + e.getMessage());
    } finally {
      FileUtil.closeSilently(out);
      file.delete();
    }

    return ok(file.getAbsolutePath());
  }

  private Result test(InputStream in) {
    FileOutputStream out = null;
    try {
      String fileName = "testFile" + new Random().nextInt();
      File file = new File(TEMP + fileName);
      file.createNewFile();
      out = new FileOutputStream(file);
      byte[] buffer = new byte[256];
      int len = in.read(buffer);
      while (len != -1) {
        out.write(buffer, 0, len);
        out.flush();
        len = in.read(buffer);
      }
      out.flush();
    } catch (IOException e) {
      log.error("Error: ", e);
      return internalServerError(e.getMessage());
    }  finally {
      FileUtil.closeSilently(out);
      FileUtil.closeSilently(in);
    }

    return ok("Done!");
  }

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
      triples = dataCollector.queryResult(rdfFormat, id);
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
      return internalServerError("Couldn't collect partial query result: " + e.getMessage());
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
      String fileName = StringUtil.bytesToHex(bytes);
      return TEMP + fileName;
    }
  }

  public static class CsvBodyParser implements BodyParser<List<List<String>>> {
    private Executor executor;

    @Inject
    public CsvBodyParser(Executor executor) {
      this.executor = executor;
    }

    @Override
    public Accumulator<ByteString, F.Either<Result, List<List<String>>>> apply(Http.RequestHeader request) {
      // A flow that splits the stream into CSV lines
      Sink<ByteString, CompletionStage<List<List<String>>>> sink = Flow.<ByteString>create()
          // We split by the new line character, allowing a maximum of 1000 characters per line
          .via(Framing.delimiter(ByteString.fromString("\n"), 1000, FramingTruncation.ALLOW))
          // Turn each line to a String and split it by commas
          .map(bytes -> {
            String[] values = bytes.utf8String().trim().split(",");
            return Arrays.asList(values);
          })
          // Now we fold it into a list
          .toMat(Sink.<List<List<String>>, List<String>>fold(
              new ArrayList<>(), (list, values) -> {
                list.add(values);
                return list;
              }), Keep.right());

      // Convert the body to a Right either
      return Accumulator.fromSink(sink).map(F.Either::Right, executor);
    }
  }


  public static class InputStreamBodyParser implements BodyParser<InputStream> {

    private Executor executor;

    @Inject
    public InputStreamBodyParser(Executor executor) {
      this.executor = executor;
    }

    @Override
    public Accumulator<ByteString, F.Either<Result, InputStream>> apply(Http.RequestHeader request) {

      Sink<ByteString, InputStream> streamSink = StreamConverters.asInputStream(new FiniteDuration(5, TimeUnit.HOURS));
      Sink<ByteString, CompletionStage<InputStream>> test = streamSink.mapMaterializedValue(
          (in)->CompletableFuture.completedFuture(in));

      Sink<ByteString, CompletionStage<InputStream>> testSink = Flow.<ByteString>create()
          .toMat(test, Keep.right());

      return Accumulator.fromSink(test).map(F.Either::Right, executor);
    }
  }

  /*public static class ForwardingBodyParser implements BodyParser<WSResponse> {
    private WSClient ws;
    private Executor executor;

    @Inject
    public ForwardingBodyParser(WSClient ws, Executor executor) {
      this.ws = ws;
      this.executor = executor;
    }

    String url = "http://example.com";

    public Accumulator<ByteString, F.Either<Result, WSResponse>> apply(Http.RequestHeader request) {
      Accumulator<ByteString, Source<ByteString, ?>> forwarder = Accumulator.source();

      Sink<ByteString, F.Either<Result, WSResponse>> sinkResponse


      final ActorSystem system = ActorSystem.create("QuickStart");
      final Materializer materializer = ActorMaterializer.create(system);


      final Source<Integer, NotUsed> source = Source.range(1, 100);
      final CompletionStage<Done> done =
          source.runForeach(i -> System.out.println(i), materializer);
      done.thenRun(() -> system.terminate());

      final Source<BigInteger, NotUsed> factorials = source
              .scan(BigInteger.ONE, (acc, next) -> acc.multiply(BigInteger.valueOf(next)));

      final CompletionStage<IOResult> result = factorials
              .map(num -> ByteString.fromString(num.toString() + "\n"))
              .runWith(FileIO.toPath(Paths.get("factorials.txt")), materializer);

      Sink<String, CompletionStage<IOResult>> lineSink = Flow.of(String.class)
          .map(s-> ByteString.fromString(s.toString() + "\n"))
          .toMat(FileIO.toPath(Paths.get("filename")), Keep.right());

      factorials.map(BigInteger::toString).runWith(lineSink, materializer);



      Sink<ByteString, InputStream> testSink =  StreamConverters.asInputStream();

      try {
        Source<ByteString, CompletionStage<IOResult>> source2 = FileIO.fromPath(Paths.get(""));

        Sink<ByteString, InputStream> testFlowSink = Flow.of(ByteString.class)
            .toMat(testSink, Keep.right());

        source2.runWith(testFlowSink, materializer);

        InputStream test = new GZIPInputStream(.runWith(testSink));
      } catch (IOException e) {
        e.printStackTrace();
      }

      return forwarder.mapFuture(source -> {
        // TODO: when streaming upload has been implemented, pass the source as the body
        return ws.url(url)setMethod("POST")
            .
            // .setBody(source)
            .execute().thenApply(F.Either::Right);
      }, executor);
    }
  }*/

 /* private static class ForwardingBodyParser implements BodyParser<WSResponse> {
    private WSClient ws;
    private Executor executor;

    @Inject
    public ForwardingBodyParser(WSClient ws, Executor executor) {
      this.ws = ws;
      this.executor = executor;
    }

    String url = "http://example.com";

    public Accumulator<ByteString, F.Either<Result, WSResponse>> apply(RequestHeader request) {
      Accumulator<ByteString, Source<ByteString, ?>> forwarder = Accumulator.source();

      Sink<ByteString, CompletionStage<Source<ByteString, ?>>> sink = forwarder.toSink();

      return forwarder.mapFuture(source -> {
        // TODO: when streaming upload has been implemented, pass the source as the body
        source.map(data -> data.iterator().asInputStream())
        return ws.url(url)
            .post(new File(""))
            .execute().andThen(F.Either::Right);
      }, executor);
    }

    @Override
    public Accumulator<ByteString, F.Either<Result, WSResponse>> apply(Http.RequestHeader request) {
      return null;
    }*/

    private static class MyBodyParser2 implements BodyParser<String> {

    private Executor executor;

    @Inject
    public MyBodyParser2(Executor executor) {
      this.executor = executor;
    }


    @Override
    public Accumulator<ByteString, F.Either<Result, String>> apply(Http.RequestHeader request) {

      GraphStageWithMaterializedValue<SinkShape<ByteString>, CompletionStage<String>> graphStage = new GraphStageWithMaterializedValue<SinkShape<ByteString>, CompletionStage<String>>() {

        public final Inlet<ByteString> in = Inlet.create("TestSink.in");

        @Override
        public Tuple2<GraphStageLogic, CompletionStage<String>> createLogicAndMaterializedValue(Attributes inheritedAttributes) throws Exception {

          GraphStageLogic logic = new GraphStageLogic(shape()) {

            {
              setHandler(in, new AbstractInHandler() {
                @Override
                public void onPush() throws Exception {
                  pull(in);
                }
              });
            }

            @Override
            public void preStart() throws Exception {
              pull(in);
            }
          };

          CompletionStage<String> result = new CompletableFuture<>();
          return new Tuple2<>(logic, result);
        }

        @Override
        public SinkShape<ByteString> shape() {
          return SinkShape.of(in);
        }
      };

      Sink<ByteString, CompletionStage<String>> sink2 = Sink.fromGraph(graphStage);

      return Accumulator.fromSink(sink2).map(result -> {
        if (result != null) {
          return F.Either.Right(result);
        }
        Result error =  internalServerError("Couldn't accumulate to file ");
        return F.Either.Left(error);
      }, executor);
    }

    private String createUniqueFileName() {
      Random random = new Random();
      byte[] bytes = new byte[16];
      random.nextBytes(bytes);
      String fileName = StringUtil.bytesToHex(bytes);
      return TEMP + fileName;
    }
  }
}