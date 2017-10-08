package controllers;

import com.typesafe.config.Config;
import de.unipassau.medspace.query_executor.QueryExecutor;
import de.unipassau.medspace.query_executor.ServiceInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by David Goeth on 06.10.2017.
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
    queryExecutor = new QueryExecutor(serviceInvoker, new URL("http://localhost:" + port));
  }

  public Result queryExecutorTest() {
    queryExecutor.keywordService();
    return ok();
  }
}