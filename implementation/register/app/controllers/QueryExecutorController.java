package controllers;

import de.unipassau.medspace.query_executor.QueryExecutor;
import de.unipassau.medspace.query_executor.ServiceInvoker;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

/**
 * Created by David Goeth on 06.10.2017.
 */
public class QueryExecutorController extends Controller {

  private final QueryExecutor queryExecutor;

  @Inject
  public QueryExecutorController(ServiceInvoker serviceInvoker) {

    queryExecutor = new QueryExecutor(serviceInvoker);
  }

  public Result queryExecutorTest() {
    queryExecutor.testService();
    return ok();
  }
}