package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import controllers.response.AddResultResponse;
import controllers.response.NoResponseResultResponse;
import controllers.response.RemoveResultResponse;
import controllers.response.ResultResponse;
import de.unipassau.medspace.register.*;
import de.unipassau.medspace.register.Results;
import de.unipassau.medspace.register.common.Datasource;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.*;
import play.routing.JavaScriptReverseRouter;

import javax.inject.Inject;
import java.util.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class RegisterController extends Controller {

    private final ActorRef registerActor;
    private final ActorRef testActor;
    private final FormFactory formFactory;
    private final BlockingRegister registerBlocking;

    @Inject
    public RegisterController(ActorSystem system, FormFactory formFactory) {
        registerActor = system.actorOf(Register.getProps());
        testActor = system.actorOf(TestActor.getProps());

        this.formFactory = formFactory;
        registerBlocking = new BlockingRegister();
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        Map<String, Datasource> datasources = registerBlocking.getDatasources();
        Collection coll = datasources.values();
        List<Datasource> list = new LinkedList<>(coll);
        Collections.sort(list, Comparator.comparing(Datasource::getUri));

        return ok(views.html.index.render("Welcome to the register home page!", list));
    }

    public Result add() {
        Datasource.MutableDatasource mutable = readMutableDatasource();
        Results.Add result = registerBlocking.addDatasource(mutable);
        ResultResponse response =  new AddResultResponse(result);
        return ok(Json.toJson(response));
    }

    public Result noResponse() {
        Datasource.MutableDatasource mutable = readMutableDatasource();
        Datasource datasource = Datasource.createFromMutable(readMutableDatasource());
        Results.NoResponse result = registerBlocking.datasourceNoRespond(datasource);
        ResultResponse response = new NoResponseResultResponse(result);
        return ok(Json.toJson(response));
    }

    public  Result remove() {
        Datasource.MutableDatasource mutable = readMutableDatasource();
        Datasource datasource = Datasource.createFromMutable(mutable);
        Results.Remove remove = registerBlocking.removeDatasource(datasource);
        ResultResponse response = new RemoveResultResponse(remove);
        return ok(Json.toJson(response));
    }

    public Result javascriptRoutes() {
        return ok(
            JavaScriptReverseRouter.create("jsRoutes",
                routes.javascript.Assets.versioned(),
                routes.javascript.RegisterController.add(),
                routes.javascript.RegisterController.noResponse(),
                routes.javascript.RegisterController.remove()
            )
        ).as("text/javascript");
    }

    private Datasource.MutableDatasource readMutableDatasource() {
        try {
            Form<Datasource.MutableDatasource> requestData = formFactory.form(Datasource.MutableDatasource.class)
                .bindFromRequest();
            return requestData.get();
        } catch (IllegalStateException e) {
            //There were binding errors; nothing valid was found
            return null;
        }
    }
}