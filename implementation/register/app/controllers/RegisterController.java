package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.response.*;
import de.unipassau.medspace.common.message.Response;
import de.unipassau.medspace.common.register.Datasource;
import de.unipassau.medspace.register.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.*;
import play.routing.JavaScriptReverseRouter;

import javax.inject.Inject;
import java.sql.Timestamp;
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
    private final Register register;

    private static Logger log = LoggerFactory.getLogger(RegisterController.class);

    @Inject
    public RegisterController(FormFactory formFactory) {
        this.formFactory = formFactory;
        register = new Register();
    }

    /**
     * An action that renders a page for testing the register services.
     */
    public Result index() {
        Map<Datasource, Timestamp> datasourceModifiedMap = register.getDatasources();
        Set<Datasource> datasources = datasourceModifiedMap.keySet();
        List<Datasource> list = new LinkedList<>(datasources);
        Collections.sort(list, Comparator.comparing(o -> o.getUrl().toExternalForm()));

        return ok(views.html.index.render("Welcome to the register home page!", list, datasourceModifiedMap));
    }

    /**
     * An action that reads a Datasource.Builder from a sent form, creates a Datasource object from
     * it and adds it to the register.
     * @return A Results.Add serialized to a JSON object.
     */
    public Result add() {
        Datasource.Builder builder = readMutableDatasource();

        // early exit...
        if (builder == null) return flawedOrMissingData();

        boolean result = register.addDatasource(builder.build());
        Response response =  new AddResponse(result);
        return ok(Json.toJson(response));
    }

    /**
     * An action that provides the set of registered datasources.
     * @return The set of registered datasources serialized to JSON.
     */
    public Result getDatasources() {
        Map<Datasource, Timestamp> map = register.getDatasources();
        Set<Datasource> datasources = map.keySet();
        JsonNode serialized = Json.toJson(datasources);
        return ok(serialized);
    }

    /**
     * An action that reads a Datasource.Builder from a sent form, creates a Datasource object from
     * it. Finally the register is informed, that the sent datasource is not responding anymore.
     * @return A Results.NoResponse serialized to a JSON object.
     */
    public Result noResponse() {
        Datasource.Builder builder = readMutableDatasource();

        // early exit...
        if (builder == null) return flawedOrMissingData();

        Datasource datasource = builder.build();
        Register.NoResponse result = register.datasourceNoRespond(datasource);
        Response response = new NoResponseResponse(result);
        return ok(Json.toJson(response));
    }

    /**
     * An action that reads a Datasource.Builder from a sent form, creates a Datasource object from
     * it. Finally the register is informed, that the sent datasource should be removed.
     * @return A Results.Remove serialized to a JSON object.
     */
    public  Result remove() {
        Datasource.Builder builder = readMutableDatasource();

        // early exit...
        if (builder == null) return flawedOrMissingData();

        Datasource datasource = builder.build();
        boolean remove = register.removeDatasource(datasource);
        Response response = new RemoveResponse(remove);
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
     * Creates a JSON response that informs the client that the sent data is flawed or incomplete
     * @return A JSON serialization of an error response message
     */
    private static Result flawedOrMissingData() {
        return ok(Json.toJson(new Response(false, "Missing or wrong data was send.")));
    }

    /**
     * Tries to read a Datasource.Builder from a sent form request.
     * @return The read Datasource.Builder or null, if no Datasource.Builder object could
     * be bound.
     */
    private Datasource.Builder readMutableDatasource() {
        try {
            Form<Datasource.Builder> requestData = formFactory.form(Datasource.Builder.class)
                .bindFromRequest();
            return requestData.get();
        } catch (IllegalStateException e) {
            //There were binding errors; nothing valid was found
            log.warn("Couldn't bind mutable datasource object", e);
            return null;
        }
    }
}