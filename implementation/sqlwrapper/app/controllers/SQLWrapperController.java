package controllers;

import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.play.WrapperController;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.wrapper.sqlwrapper.ConfigProvider;
import de.unipassau.medspace.wrapper.sqlwrapper.SQLWrapperService;
import de.unipassau.medspace.d2r.config.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xbill.DNS.Address;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.net.InetAddress;
import java.net.UnknownHostException;

import play.twirl.api.Html;

/**
 * This controller handles the access of the SQL wrapper services and manages the proper GUI rendering of the services.
 */
@Singleton
public class SQLWrapperController extends WrapperController {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(SQLWrapperController.class);

  /**
   * The SQL wrapper model.
   */
  private final SQLWrapperService sqlWrapperService;

  /**
   * Factory for reading and writing HTML forms.
   */
  private final FormFactory formFactory;


  /**
   * TODO
   */
  private final ConnectionPool connectionPool;

  /**
   * TODO
   */
  private final Configuration d2rConfig;


  /**
   * Creates a new SQLWrapperController
   * @param wrapperService TODO
   * @param formFactory
   * @param rdfProvider
   */
  @Inject
  SQLWrapperController(SQLWrapperService wrapperService,
                       ConnectionPool connectionPool,
                       FormFactory formFactory,
                       RDFProvider rdfProvider,
                       ConfigProvider configProvider) {
    super(configProvider.getGeneralWrapperConfig(), rdfProvider, wrapperService);

    this.formFactory = formFactory;
    this.connectionPool = connectionPool;
    this.d2rConfig = configProvider.getD2rConfig();
    this.sqlWrapperService = wrapperService;
  }

  /**
   * Provides the test page of the SQL Wrapper.
   * @return
   */
  public Result guiTest() {
    return ok(views.html.testGui.render());
  }

  /**
   * Provides the SQL Wrapper status and debug page.
   */
  public Result index() {
    return ok(views.html.index.render(sqlWrapperService, d2rConfig, generalConfig, connectionPool,
        sqlWrapperService.getMetaData()));
  }


  /**
   * Test service. Used for the later implementation of the registration on the Dataspace Register.
   * @return Status report whether the registration was successful.
   */
  public Result registerDataSourceTest() {
    String clientHostName;
    Http.Request request = request();
    DynamicForm form = formFactory.form().bindFromRequest(request, "description");
    String desc  = form.get("description");

    if (desc == null) {
      //assign default description
      desc ="";
    }

    try {
      clientHostName = getClientHostName(request);
    } catch (UnknownHostException e) {
      return badRequest("Couldn't get client host name: " + request().remoteAddress());
    }

    String result = "Request accepted. Datasource was registered: " + clientHostName + " <br>" +
        "description: '" + desc + "' <br>" +
        "exports in namespaces: ";

    return ok(views.html.minimal.render("Accepted", Html.apply(result)));
  }

  /**
   * Provides the host name of the client from a http request header.
   * @param request The http request header to get the host name of the client.
   * @return The host name of the client or its ip address if the client's ip address couldn't be resolved to a host
   * name.
   * @throws UnknownHostException If no ip address could be found from the request's client.
   */
  private String getClientHostName(Http.Request request) throws UnknownHostException {
    InetAddress client = InetAddress.getByName(request.remoteAddress());
    return Address.getHostName(client);
  }
}