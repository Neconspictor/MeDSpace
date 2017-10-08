package de.unipassau.medspace.query_executor;

import de.unipassau.medspace.common.exception.UnsupportedServiceException;
import de.unipassau.medspace.common.register.Datasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
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

  public QueryExecutor(ServiceInvoker serviceInvoker, URL registerBase) {

    this.serviceInvoker = serviceInvoker;
    this.registerBase = registerBase;
  }

  public void keywordService() {
    List<Datasource> datasources;
    try {
      datasources = retrieveFromRegister();
    } catch (IOException e) {
      log.error("Couldn't retrieve datasource list from the register!", e);
      return;
    }

    for (Datasource datasource : datasources) {
      log.info(datasource.toString());
      try {
        serviceInvoker.queryDatasource(datasource, "SEARCH", "keywords=male female spanish");
      } catch (IOException | UnsupportedServiceException e) {
        log.error("Error while querying datasource " + datasource.getUrl(), e);
      }
    }
  }

  private List<Datasource> retrieveFromRegister() throws IOException {

    return serviceInvoker.invokeRegisterGetDatasources(registerBase);
  }
}