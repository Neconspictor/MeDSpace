package de.unipassau.medspace.query_executor;

import de.unipassau.medspace.common.register.Datasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * The Query executor is responsible to execute a query on a database.
 */
public class QueryExecutor {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(QueryExecutor.class);

  private final ServiceInvoker serviceInvoker;
  private final URL registerBase;

  public QueryExecutor(ServiceInvoker serviceInvoker) {

    this.serviceInvoker = serviceInvoker;
    try {
      registerBase = new URL("http://localhost:9500");
    } catch (MalformedURLException e) {
      throw new IllegalStateException("registerBase is not a valid URL!");
    }
  }

  public void testService() {
    List<Datasource> datasources = retrieveFromRegister();
    if (datasources == null) {
      log.error("Couldn't retrieve datasource list from the register!");
      return;
    }

    for (Datasource datasource : datasources) {
      log.info(datasource.toString());
    }

  }

  private List<Datasource> retrieveFromRegister() {

    return serviceInvoker.invokeRegisterGetDatasources(registerBase);
  }

}