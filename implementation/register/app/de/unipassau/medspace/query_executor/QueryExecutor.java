package de.unipassau.medspace.query_executor;

import de.unipassau.medspace.common.exception.UnsupportedServiceException;
import de.unipassau.medspace.common.register.Datasource;
import de.unipassau.medspace.common.register.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
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
  private final URL dataCollectorBase;

  public QueryExecutor(ServiceInvoker serviceInvoker, URL registerBase, URL dataCollectorBase) {
    this.serviceInvoker = serviceInvoker;
    this.registerBase = registerBase;
    this.dataCollectorBase = dataCollectorBase;
  }

  public InputStream keywordService(List<String> keywords, String rdfFormat) throws IOException {
    List<Datasource> datasources;

    String queryString = "keywords=";

    for (String keyword : keywords) {
      queryString += keyword + " ";
    }

    queryString = queryString.trim();

    try {
      datasources = retrieveFromRegister();
    } catch (IOException e) {
      throw new IOException("Couldn't retrieve datasource list from the register!", e);
    }

    BigInteger resultID;
    try {
      resultID = getResultID();
    } catch (IOException e) {
      throw new IOException("Couldn't create unique result id", e);
    }

    Service service = new Service("search");
    Query query =  new Query(service, queryString);

    for (Datasource datasource : datasources) {
      log.info(datasource.toString());

      try (InputStream in = serviceInvoker.queryDatasourceInputStream(datasource, query)){
        writeInputStreamToRepository(in, datasource.getUrl(), datasource.getRdfFormat(), resultID);
      } catch (IOException | UnsupportedServiceException e) {
        serviceInvoker.invokeDataCollectorDeleteQueryResult(dataCollectorBase, resultID);
        throw new IOException("Couldn't query datasource " + datasource.getUrl(), e);
      }
    }

    InputStream in;

    try {
      in = serviceInvoker.invokeDataCollectorQueryQueryResult(dataCollectorBase, resultID, rdfFormat);
    } catch (IOException e) {
      serviceInvoker.invokeDataCollectorDeleteQueryResult(dataCollectorBase, resultID);
      throw new IOException("Couldn't get query result",e );
    }

    return new DataCollectorResultInputStream(in, resultID);
  }

  private List<Datasource> retrieveFromRegister() throws IOException {
    return serviceInvoker.invokeRegisterGetDatasources(registerBase);
  }


  private void writeInputStreamToRepository(InputStream in, URL baseURI, String rdfFormat,
                                     BigInteger resultID) throws IOException {

    serviceInvoker.invokeDataCollectorAddPartialQueryResultInputStream(dataCollectorBase, resultID.toString(),
        rdfFormat, baseURI.toExternalForm(), in);
  }

  private BigInteger getResultID() throws IOException {
    return serviceInvoker.invokeDataCollectorCreateQueryResultID(dataCollectorBase);
  }


  private class DataCollectorResultInputStream extends InputStream {

    private final InputStream in;

    private final BigInteger resultID;

    public DataCollectorResultInputStream(InputStream in, BigInteger resultID) {
      this.in = in;
      this.resultID = resultID;
    }

    @Override
    public int read() throws IOException {
      return in.read();
    }

    @Override
    public void close() throws IOException {
      try {
        in.close();
      } finally {
        try {
          serviceInvoker.invokeDataCollectorDeleteQueryResult(dataCollectorBase, resultID);
        } catch (IOException e) {
          log.error("Couldn't delete query result with resultID=" + resultID, e);
        }
      }

      log.debug("Closed DataCollectorResultInputStream with resultID=" + resultID);
    }
  }
}