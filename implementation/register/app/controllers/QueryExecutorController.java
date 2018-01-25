package controllers;

import com.typesafe.config.Config;
import de.unipassau.medspace.query_executor.QueryExecutor;
import de.unipassau.medspace.query_executor.ServiceInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

/**
 * TODO
 */
public class QueryExecutorController extends Controller {

  private final QueryExecutor queryExecutor;

      /**
      * Logger instance for this class.
      */
    private static final  Logger log = LoggerFactory.getLogger(QueryExecutorController.class);

  @Inject
  public QueryExecutorController(ServiceInvoker serviceInvoker, Config playConfig) throws MalformedURLException,
      ExecutionException, InterruptedException {

    //TODO the url (with port) should be stated in the config file for the QueryExecutor once it is split from the register
    int port = playConfig.getInt("play.server.http.port");
    log.warn("Readed port number: " +  port);
    queryExecutor = new QueryExecutor(serviceInvoker, new URL("http://localhost:" + port),
        new URL("http://localhost:" + port + "/data-collector/"));
  }

  public Result queryExecutorTest(String query, String rdfFormat) {
    log.warn("query param: " + query);

    InputStream in;
    try {
      in = queryExecutor.keywordService(getKeywords(query), rdfFormat);
    } catch (IOException e) {
      StringWriter strWriter = new StringWriter();
      PrintWriter writer = new PrintWriter(strWriter);
      e.printStackTrace(writer);
      return internalServerError(strWriter.getBuffer().toString());
    }

    return ok(in);
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