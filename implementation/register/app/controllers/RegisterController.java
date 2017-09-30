package controllers;

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
 * This controller defines actions that allow to access the functionality of the register via HTTP.
 */
public class RegisterController extends Controller {

    /**
     * Used to read http request data.
     */
    private final FormFactory formFactory;

    /**
     * The register, this controller is referring to.
     */
    private final BlockingRegister registerBlocking;

    @Inject
    public RegisterController(FormFactory formFactory) {
        this.formFactory = formFactory;
        registerBlocking = new BlockingRegister();
    }

    /**
     * An action that renders a page for testing the register services.
     */
    public Result index() {
        Map<String, Datasource> datasources = registerBlocking.getDatasources();
        Collection<Datasource> coll = datasources.values();
        List<Datasource> list = new LinkedList<>(coll);
        Collections.sort(list, Comparator.comparing(o -> o.getUrl().toExternalForm()));

        return ok(views.html.index.render("Welcome to the register home page!", list));
    }

    /**
     * An action that reads a Datasource.MutableDatasource from a sent form, creates a Datasource object from
     * it and adds it to the register.
     * @return A Results.Add serialized to a JSON object.
     */
    public Result add() {
        Datasource.MutableDatasource mutable = readMutableDatasource();
        Results.Add result = registerBlocking.addDatasource(mutable);
        ResultResponse response =  new AddResultResponse(result);
        return ok(Json.toJson(response));
    }

    /**
     * An action that reads a Datasource.MutableDatasource from a sent form, creates a Datasource object from
     * it. Finally the register is informed, that the sent datasource is not responding anymore.
     * @return A Results.NoResponse serialized to a JSON object.
     */
    public Result noResponse() {
        Datasource.MutableDatasource mutable = readMutableDatasource();
        Datasource datasource = Datasource.createFromMutable(mutable);
        Results.NoResponse result = registerBlocking.datasourceNoRespond(datasource);
        ResultResponse response = new NoResponseResultResponse(result);
        return ok(Json.toJson(response));
    }

    /**
     * An action that reads a Datasource.MutableDatasource from a sent form, creates a Datasource object from
     * it. Finally the register is informed, that the sent datasource should be removed.
     * @return A Results.Remove serialized to a JSON object.
     */
    public  Result remove() {
        Datasource.MutableDatasource mutable = readMutableDatasource();
        Datasource datasource = Datasource.createFromMutable(mutable);
        Results.Remove remove = registerBlocking.removeDatasource(datasource);
        ResultResponse response = new RemoveResultResponse(remove);
        return ok(Json.toJson(response));
    }

    /**
     * An action that provides routes to services, that should be accessible from javascript.
     * @return Javascript code, that allows access to services of this application directly from javascript.
     */
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

    /**
     * Tries to read a Datasource.MutableDatasource from a sent form request.
     * @return The read Datasource.MutableDatasource or null, if no Datasource.MutableDatasource object could
     * be bound.
     */
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