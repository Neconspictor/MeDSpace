package de.unipassau.medspace.common.play;

import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.rdf.TripleWriterFactory;
import de.unipassau.medspace.common.stream.LogWrapperInputStream;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.stream.TripleInputStream;
import de.unipassau.medspace.common.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * TODO
 */
public class WrapperController extends Controller {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(WrapperController.class);


  /**
   * TODO
   */
  protected final GeneralWrapperConfig generalConfig;

  /**
   * TODO
   */
  protected final RDFProvider rdfProvider;

  /**
   * TODO
   */
  protected final TripleWriterFactory tripleWriterFactory;

  protected final WrapperService wrapperService;


  public WrapperController(GeneralWrapperConfig generalConfig,
                           RDFProvider rdfProvider,
                           WrapperService wrapperService) {
    super();
    this.generalConfig = generalConfig;
    this.rdfProvider = rdfProvider;
    tripleWriterFactory = rdfProvider.getWriterFactory();
    this.wrapperService = wrapperService;
  }


  /**
   * Does invoke a keyword keywordSearch on the SQL wrapper and provides the result as serialized rdf triples.
   * @param keywords The keywords to keywordSearch for on the SQl wrapper.
   * @param attach Specifies if the caller wants the result stored in an HTML attachment field.
   * @return RDF triples representing the keyword keywordSearch result.
   */
  public Result keywordSearch(String keywords, boolean useOr, boolean attach)  {
    if (log.isDebugEnabled())
      log.debug("keyword keywordSearch query: " + keywords);

    KeywordSearcher.Operator operator = KeywordSearcher.Operator.AND;
    if (useOr)
      operator = KeywordSearcher.Operator.OR;

    Stream<Triple> triples = null;
    try {
      triples = wrapperService.keywordSearch(keywords, operator);
    } catch (IOException e) {
      FileUtil.closeSilently(triples);
      log.error("Error while querying the D2rWrapper", e);
      return internalServerError("Internal server error");
    } catch (NoValidArgumentException e) {
      FileUtil.closeSilently(triples);
      return badRequest("keyword keywordSearch query isn't valid: \"" + keywords + "\"");
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
}