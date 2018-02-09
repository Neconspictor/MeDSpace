package controllers;

import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.rdf.TripleWriterFactory;
import de.unipassau.medspace.wrapper.pdf_wrapper.play.ConfigProvider;
import de.unipassau.medspace.wrapper.pdf_wrapper.play.LogWrapperInputStream;
import de.unipassau.medspace.wrapper.pdf_wrapper.play.PdfWrapperService;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.stream.TripleInputStream;
import de.unipassau.medspace.common.util.FileUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xbill.DNS.Address;
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
import java.util.Set;

/**
 * This controller handles the access of the SQL wrapper services and manages the proper GUI rendering of the services.
 */
@Singleton
public class ImageWrapperController extends Controller {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(ImageWrapperController.class);

  /**
   * The SQL wrapper model.
   */
  private final PdfWrapperService wrapperService;

  /**
   * Factory for reading and writing HTML forms.
   */
  private final FormFactory formFactory;

  /**
   * TODO
   */
  private final TripleWriterFactory tripleWriterFactory;

  /**
   * TODO
   */
  private final RDFProvider rdfProvider;

  /**
   * TODO
   */
  private final GeneralWrapperConfig generalConfig;

  /**
   * Creates a new SQLWrapperController
   * @param wrapperService TODO
   * @param formFactory
   * @param rdfProvider
   */
  @Inject
  ImageWrapperController(PdfWrapperService wrapperService,
                         FormFactory formFactory,
                         RDFProvider rdfProvider,
                         ConfigProvider configProvider) {
    this.wrapperService = wrapperService;
    this.formFactory = formFactory;
    this.tripleWriterFactory = rdfProvider.getWriterFactory();
    this.rdfProvider = rdfProvider;
    generalConfig = configProvider.getGeneralWrapperConfig();
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
    return ok(views.html.index.render(wrapperService, generalConfig));
  }

  /**
   * Does invoke a keyword search on the SQL wrapper and provides the result as serialized rdf triples.
   * @param keywords The keywords to search for on the SQl wrapper.
   * @param attach Specifies if the caller wants the result stored in an HTML attachment field.
   * @return RDF triples representing the keyword search result.
   */
  public Result search(String keywords, boolean attach)  {
    if (log.isDebugEnabled())
      log.debug("keyword search query: " + keywords);

    Stream<Triple> triples = null;
    try {
      triples = wrapperService.search(keywords);
    } catch (IOException e) {
      FileUtil.closeSilently(triples);
      log.error("Error while querying the D2rWrapper", e);
      return internalServerError("Internal server error");
    } catch (NoValidArgumentException e) {
      FileUtil.closeSilently(triples);
      return badRequest("keyword search query isn't valid: \"" + keywords + "\"");
    }

    String outputFormat = generalConfig.getOutputFormat();
    Set<Namespace> namespaces = wrapperService.getWrapper().getNamespaces();
    List<String> extensions = rdfProvider.getFileExtensions(outputFormat);
    String fileExtension = extensions.size() == 0 ? "txt" : extensions.get(0);
    InputStream tripleStream;
    try {
      tripleStream = new TripleInputStream(triples, outputFormat, namespaces, tripleWriterFactory);
    } catch (NoValidArgumentException | IOException e) {
      log.error("Couldn't construct triple input stream", e);
      return internalServerError("Couldn't construct triple input stream");
    }

    String mimeType = Http.MimeTypes.TEXT;
    String formatMimeType = rdfProvider.getDefaultMimeType(outputFormat);

    if (formatMimeType == null) formatMimeType = mimeType;

    String dispositionValue = "inline";

    if (formatMimeType.equals(Http.MimeTypes.BINARY)) {
      attach = true;
    }

    if (attach) {
      Date date = new Date();
      String filename = "SearchResult" + date.getTime() + "." + fileExtension;
      dispositionValue = "attachement; filename=" + filename;
      mimeType = formatMimeType;
    }

    // If an exception is thrown, play catches it and drops the connection
    // Unfortunately no error logging or something similar is done.
    // So we wrap the triple stream around an input stream, that will log any error before rethrowing the error.
    LogWrapperInputStream logWrapper = new LogWrapperInputStream(tripleStream);

    return ok(logWrapper).as(mimeType).withHeader("Content-Disposition", dispositionValue);
  }

  /**
   * The SQL wrapper does reindex the data from the underlying datasource.
   *
   * NOTE: This service is not intended o be used in production. Use it just for testing purposes!
   * @return Status report whether the reindexing was successfull.
   */
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