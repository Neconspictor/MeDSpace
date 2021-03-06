package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.response.*;
import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.message.Response;
import de.unipassau.medspace.common.register.Datasource;
import de.unipassau.medspace.common.register.DatasourceState;
import de.unipassau.medspace.register.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.*;
import play.routing.JavaScriptReverseRouter;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

/**
 * This controller defines actions that allow to access the functionality of the register via HTTP.
 */
public class MeDSpaceController extends Controller {

    private static Logger log = LoggerFactory.getLogger(MeDSpaceController.class);

    private final Register register;

  /**
   * Creates a new MeDSpaceController object.
   * @param lifecycle The lifecycle of the register.
   */
    @Inject
    public MeDSpaceController(RegisterLifecycle lifecycle) {
        register = lifecycle.getRegister();
    }

    /**
     * Provides a status page that lists all registered datasources.
     * @return a status page that lists all registered datasources.
     */
    public Result datasources() {
        Map<Datasource, DatasourceState> datasourceModifiedMap = register.getDatasources();
        Set<Datasource> datasources = datasourceModifiedMap.keySet();
        List<Datasource> list = new LinkedList<>(datasources);
        Collections.sort(list, Comparator.comparing(o -> o.getUrl().toExternalForm()));

        return ok(views.html.datasources.render(list, datasourceModifiedMap));
    }

  /**
   * Provides the user interface page for MeDSpace.
   * @return the user interface page for MeDSpace.
   */
    public Result index() {
      return ok(views.html.medspaceGUI.render());
    }

    /**
     * An action that reads a Datasource.Builder from a sent form, creates a Datasource object from
     * it and adds it to the register.
     * @return A Results.Add serialized to a JSON object.
     */
    public Result add() {
        Datasource datasource;
        try {
            datasource = readDatasource();
        } catch (IOException e) {
            log.error("Couldn't get datasource", e);
            return flawedOrMissingData();
        }

        boolean result = register.addDatasource(datasource);
        Response response =  new AddResponse(result);
        return ok(Json.toJson(response));
    }

    /**
     * An action that provides the set of registered datasources.
     * @return The set of registered datasources serialized to JSON.
     */
    public Result getDatasources() {
        Map<Datasource, DatasourceState> map = register.getDatasources();
        Set<Datasource> datasources = map.keySet();
        JsonNode serialized = Json.toJson(datasources);
        return ok(serialized);
    }

    /**
     * Provides the IO error count for a datasource.
     * The datasource is expected to be sent within the POST body and is serialized to JSON.
     * @return the IO error count for a datasource.
     */
    public Result ioError() {
        Datasource datasource;
        try {
            datasource = readDatasource();
        } catch (IOException e) {
            log.error("Couldn't get datasource", e);
            return flawedOrMissingData();
        }

        register.datasourceIOError(datasource);
        return ok("Done.");
    }

    /**
     * An action that reads a Datasource.Builder from a sent form, creates a Datasource object from
     * it. Finally the register is informed, that the sent datasource is not responding anymore.
     * @return A Results.NoResponse serialized to a JSON object.
     */
    public Result noResponse() {
        Datasource datasource;
        try {
            datasource = readDatasource();
        } catch (IOException e) {
            log.error("Couldn't get datasource", e);
            return flawedOrMissingData();
        }

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
        Datasource datasource;
        try {
            datasource = readDatasource();
        } catch (IOException e) {
            log.error("Couldn't get datasource", e);
            return flawedOrMissingData();
        }

        boolean remove = register.removeDatasource(datasource);
        Response response = new RemoveResponse(remove);
        return ok(Json.toJson(response));
    }

    /**
     * REST service that removes all registered datasources.
     * @return A status message whether the deletion was successful.
     */
    public  Result removeAllDatasources() {
        boolean result = register.removeAllDatasources();
        if (!result) {
            internalServerError("Register is closed. No data sources can be removed.");
        }
        return ok("Deleted successfully all data sources");
    }

    /**
     * An action that provides routes to services, that should be accessible from javascript.
     * @return Javascript code, that allows access to services of this application directly from javascript.
     */
    public Result javascriptRoutes() {
        return ok(
            JavaScriptReverseRouter.create("jsRoutes",
                routes.javascript.Assets.versioned(),
                routes.javascript.MeDSpaceController.add(),
                routes.javascript.MeDSpaceController.noResponse(),
                routes.javascript.MeDSpaceController.remove(),
                routes.javascript.DataCollectorController.addPartialQueryResult(),
                routes.javascript.DataCollectorController.createQueryResult(),
                routes.javascript.DataCollectorController.deleteQueryResult()
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
    private Datasource readDatasource() throws IOException {
        JsonNode root = request().body().asJson();
        if (root == null) {
            log.warn("Couldn't read datasource object");
            return null;
        }
        Datasource.Builder builder = Json.fromJson(root, Datasource.Builder.class);
        //Form<Datasource.Builder> requestData = formFactory.form(Datasource.Builder.class)
        //   .bindFromRequest();
        try {
            return builder.build();
        } catch (NoValidArgumentException e) {
            throw new IOException("Couldn't build a datasource object", e);
        }
    }
}