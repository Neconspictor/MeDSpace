package controllers;

import akka.actor.*;


import de.unipassau.medspace.SQLWrapperService;
import de.unipassau.medspace.common.exception.NotValidArgumentException;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.stream.JenaRDFInputStream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.test.HelloActor;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.shared.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xbill.DNS.Address;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.xbill.DNS.*;
import play.twirl.api.Html;
import scala.Dynamic;

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

  public Result guiTest() {
    return ok(views.html.testGui.render());
  }

  /**
   * An action that renders an HTML page with a welcome message.
   * The configuration in the <code>routes</code> file means that
   * this method will be called when the application receives a
   * <code>GET</code> request with a path of <code>/</code>.
   */
  public Result index() {
    return ok(views.html.index.render(wrapperService, wrapperService.getConfig(), wrapperService.getConnectionPool(),
        wrapperService.getMetaData()));
  }

  public Result search(String keywords, boolean attach)  {
    if (log.isDebugEnabled())
      log.debug("keyword search query: " + keywords);

    DataSourceStream<Triple> triples = null;
    try {
      triples = wrapperService.search(keywords);
    } catch (IOException e) {
      FileUtil.closeSilently(triples);
      log.error("Error while querying the D2rWrapper", e);
      return internalServerError("Internal server error");
    } catch (NotValidArgumentException e) {
      FileUtil.closeSilently(triples);
      return badRequest("keyword search query isn't valid: \"" + keywords + "\"");
    }

    Configuration config = wrapperService.getConfig();
    PrefixMapping mapping = wrapperService.getWrapper().getNamespacePrefixMapper();
    Lang lang = config.getOutputFormat();
    List<String> extensions = lang.getFileExtensions();
    String fileExtension = extensions.size() == 0 ? "txt" : extensions.get(0);
    InputStream tripleStream = new JenaRDFInputStream(triples, lang, mapping);

    String mimeType = Http.MimeTypes.TEXT;
    String dispositionValue = "inline";

    if ((lang == Lang.RDFTHRIFT)) {
      mimeType = Http.MimeTypes.BINARY;
      attach = true;
    }

    if (attach) {
      Date date = new Date();
      String filename = "SearchResult" + date.getTime() + "." + fileExtension;
      dispositionValue = "attachement; filename=" + filename;
    }

    return ok(tripleStream).as(mimeType).withHeader("Content-Disposition", dispositionValue);
  }

  public Result registerDataSourceTest() {
    String clientHostName = null;
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

  public Result reindex() {
    if (!wrapperService.getWrapper().isIndexUsed())
      return ok("No index used, nothing to do.");

    try {
      wrapperService.getWrapper().reindexData();
    } catch (IOException e) {
      log.error("Error while reindexing: ", e);
      return internalServerError("Internal Server error");
    }

    return ok("Data reindexed.");
  }

  private String getClientHostName(Http.Request request) throws UnknownHostException {
      InetAddress client = InetAddress.getByName(request.remoteAddress());
      return Address.getHostName(client);
  }
}