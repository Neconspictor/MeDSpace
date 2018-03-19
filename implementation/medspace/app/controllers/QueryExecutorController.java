package controllers;

import com.typesafe.config.Config;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.common.stream.LogWrapperInputStream;
import de.unipassau.medspace.query_executor.QueryExecutor;
import de.unipassau.medspace.query_executor.ServiceInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.inject.ApplicationLifecycle;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * TODO
 */
public class QueryExecutorController extends Controller {

  private final QueryExecutor queryExecutor;

  private final RDFProvider rdfProvider;

      /**
      * Logger instance for this class.
      */
    private static final  Logger log = LoggerFactory.getLogger(QueryExecutorController.class);

  @Inject
  public QueryExecutorController(ApplicationLifecycle lifecycle,
                                 ServiceInvoker serviceInvoker,
                                 Config playConfig,
                                 RDFProvider rdfProvider) throws MalformedURLException,
      ExecutionException, InterruptedException {

    //TODO the url (with port) should be stated in the config file for the QueryExecutor once it is split from the register
    int port = playConfig.getInt("play.server.http.port");
    log.warn("Readed port number: " +  port);
    queryExecutor = new QueryExecutor(serviceInvoker, new URL("http://localhost:" + port + "/register/"),
        new URL("http://localhost:" + port + "/data-collector/"));
    this.rdfProvider = rdfProvider;

    lifecycle.addStopHook(()->{
        try{
          queryExecutor.close();
        } catch (IOException e) {}
        return CompletableFuture.completedFuture(null);
        });
  }

  /**
   * TODO
   * @param query
   * @param rdfFormat
   * @param useOr
   * @param attach
   * @return
   */
  public Result searchByKeyword(String query, String rdfFormat, boolean useOr, boolean attach) {
    log.debug("query param: " + query);

    KeywordSearcher.Operator operator = KeywordSearcher.Operator.AND;
    if (useOr)
      operator = KeywordSearcher.Operator.OR;

    InputStream tripleStream;
    try {
      tripleStream = queryExecutor.keywordService(getKeywords(query), operator, rdfFormat);
    } catch (IOException e) {
      StringWriter strWriter = new StringWriter();
      PrintWriter writer = new PrintWriter(strWriter);
      e.printStackTrace(writer);
      return internalServerError(strWriter.getBuffer().toString());
    }

    String mimeType = "text/plain; charset=utf-8";
    String dispositionValue = "inline";

    if (attach) {
      List<String> extensions = rdfProvider.getFileExtensions(rdfFormat);
      String fileExtension = extensions.size() == 0 ? "txt" : extensions.get(0);

      Date date = new Date();
      String filename = "SearchResult" + date.getTime() + "." + fileExtension;
      dispositionValue = "attachement; filename=" + filename;
      mimeType = Http.MimeTypes.BINARY;
    }

    // If an exception is thrown, play catches it and drops the connection
    // Unfortunately no error logging or something similar is done.
    // So we wrap the triple stream around an input stream, that will log any error before rethrowing the error.
    LogWrapperInputStream logWrapper = new LogWrapperInputStream(tripleStream);

    return ok(logWrapper).as(mimeType).withHeader("Content-Disposition", dispositionValue);
  }

  public Result clearCache() {
    queryExecutor.clearCache();
    return ok("Cleared query cache.");
  }

  private List<String> getKeywords(String query) {
    List<String> result = new ArrayList<>();
    StringTokenizer tokenizer = new StringTokenizer(query);
    while(tokenizer.hasMoreTokens()) {
      result.add(tokenizer.nextToken());
    }
    return result;
  }
}