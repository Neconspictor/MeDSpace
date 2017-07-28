package controllers;

import akka.NotUsed;
import akka.actor.*;
import static akka.pattern.Patterns.ask;

import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.StreamConverters;
import akka.util.ByteString;
import de.unipassau.medspace.SQLWrapperService;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.test.HelloActor;
import de.unipassau.medspace.test.HelloActorProtocol;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.*;
import scala.compat.java8.FutureConverters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
@Singleton
public class SQLWrapperController extends Controller {

  private final SQLWrapperService wrapperService;
  private final ActorRef helloActor;
  private static Logger log = LoggerFactory.getLogger(SQLWrapperController.class);

private final FormFactory formFactory;
  @Inject
  SQLWrapperController(SQLWrapperService wrapperService, ActorSystem system,
                       FormFactory formFactory) {
    this.wrapperService = wrapperService;
    helloActor = system.actorOf(HelloActor.getProps());
    this.formFactory = formFactory;
  }

  /**
   * An action that renders an HTML page with a welcome message.
   * The configuration in the <code>routes</code> file means that
   * this method will be called when the application receives a
   * <code>GET</code> request with a path of <code>/</code>.
   */
  public Result index() {
    return ok(views.html.index.render());
  }

  public CompletionStage<Result> testDatasourceConnection() {
    return CompletableFuture.supplyAsync(() ->
        wrapperService.isConnected())
    .thenApply((isConnected)->
        ok(views.html.connectionTest.render(isConnected)));
  }

  public CompletionStage<Result> sayHello(String name) {
    return FutureConverters.toJava(ask(helloActor, new HelloActorProtocol.SayHello(name), 1000))
        .thenApply(response -> ok((String) response));
  }

  public Result fileTest() throws FileNotFoundException {

    InputStream input = Play.current().classloader().getResourceAsStream("public/large.txt");
    if (input == null) {
      throw new FileNotFoundException("large.bin was not found!");
    }

    Source<ByteString, ?> source = Source.<ByteString>actorRef(256, OverflowStrategy.dropNew())
        .mapMaterializedValue(sourceActor -> {
          sourceActor.tell(ByteString.fromString("My cool response"), null);
          sourceActor.tell(new Status.Success(NotUsed.getInstance()), null);
          return NotUsed.getInstance();
        });

    Source<ByteString, ?> source2 = StreamConverters.fromInputStream(()-> input);

    source = source.concat(source2);

    String filename = "MyCoolFile.bin";
    return ok().chunked(source).as(Http.MimeTypes.BINARY).withHeader("Content-Disposition",
        "attachement; filename=" + filename);
  }

  public Result search() throws IOException {
    DynamicForm requestData = formFactory.form().bindFromRequest();
    String keywords = requestData.get("keywords");
    if (keywords == null)
      return redirect(routes.SQLWrapperController.index());

    DataSourceStream<Triple> triples = wrapperService.search(keywords);

    StringBuilder builder = new StringBuilder();
    for (Triple triple : triples) {
      builder.append(triple.toString());
      builder.append("\n");
    }

    return ok(builder.toString());
  }
}