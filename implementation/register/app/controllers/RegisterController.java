package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import de.unipassau.medspace.register.BlockingRegister;
import de.unipassau.medspace.register.TestActor;
import de.unipassau.medspace.register.common.Datasource;
import de.unipassau.medspace.register.common.DatasourceSerialized;
import de.unipassau.medspace.register.util.ActorUtil;
import play.data.Form;
import play.data.FormFactory;
import de.unipassau.medspace.register.Register;
import de.unipassau.medspace.register.Protocol;
import play.mvc.*;
import scala.concurrent.duration.Duration;

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

        Protocol.GetDatasources msg = new Protocol.GetDatasources();
        Protocol.GetDatasourcesResult result = null;
        try {
            result = (Protocol.GetDatasourcesResult) ActorUtil.sendAndAwait(registerActor, msg);
        } catch (Exception e) {
           return internalServerError(e.getMessage());
        }

        List<Datasource> datasources = result.datasources;

        return ok(views.html.index.render("Welcome to the register home page!", datasources));
    }

    public Result add() {

        Form<DatasourceSerialized> requestData = formFactory.form(DatasourceSerialized.class).bindFromRequest();
        DatasourceSerialized serialzed = requestData.get();
        Datasource datasource = new Datasource(serialzed.getUri(), serialzed.getDescription(), serialzed.getServices());

        Protocol.AddDatasource msg = new Protocol.AddDatasource(datasource);
        Object result = null;
        try {
            result =  ActorUtil.sendAndAwait(registerActor, msg);
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }

        if (result instanceof Protocol.OperationStatus.Ok) {
            return ok("Successfully added a new datasource.");
        } else {
            return ok("Datasource is already registered.");
        }
    }

    public Result blockTest1() {
        Map<String, Datasource> datasources = registerBlocking.getDatasources();
        StringBuilder builder = new StringBuilder();
        Collection coll = datasources.values();
        List<Datasource> list = new LinkedList<>(coll);
        Collections.sort(list, Comparator.comparing(Datasource::getUri));

        for (Datasource datasource : list) {
            builder.append(datasource);
            builder.append(",\n");
        }

        if (builder.length() > 0)
            builder.replace(builder.length() - 2, builder.length(), "");

        return ok(builder.toString());
    }

    public Result blockTest2() throws InterruptedException {
        Datasource datasource = new Datasource("http://hello.de/sqlDatasource", "hello description", null);
        registerBlocking.addDatasource(datasource);

        datasource = new Datasource("http://test.com/datasource1", "description 1", null);
        registerBlocking.addDatasource(datasource);
        return ok("Done.");
    }

    public Result blockTest3() throws InterruptedException {

        Map<String, Datasource> datasources = registerBlocking.getDatasources();
        Datasource datasource = new Datasource("http://medspace.com/datasource3", "description 3", null);
        registerBlocking.addDatasource(datasource);
        return ok("Done.");
    }

    public Result test1() {

        Object result = null;
        try {
            result =  ActorUtil.sendAndAwait(testActor, new TestActor.ShortProcess(),
                Duration.create(15, "seconds"));
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }

        return ok("Done.");
    }

    public Result test2() throws InterruptedException {
        Object result = null;
        try {
            result =  ActorUtil.sendAndAwait(testActor, new TestActor.LongProcess(),
                Duration.create(15, "seconds"));
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }
        return ok("Done.");
    }
}