package controllers;

import akka.actor.*;
import static akka.pattern.Patterns.ask;
import de.unipassau.medspace.SQLWrapperService;
import de.unipassau.medspace.test.HelloActor;
import de.unipassau.medspace.test.HelloActorProtocol;
import play.mvc.*;
import scala.compat.java8.FutureConverters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
@Singleton
public class SQLWrapperController extends Controller {

  private SQLWrapperService wrapperService;
  final ActorRef helloActor;

  @Inject
  SQLWrapperController(SQLWrapperService wrapperService, ActorSystem system) {
    this.wrapperService = wrapperService;
    helloActor = system.actorOf(HelloActor.getProps());
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
}