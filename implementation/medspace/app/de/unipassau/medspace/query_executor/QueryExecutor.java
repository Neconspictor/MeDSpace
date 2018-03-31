package de.unipassau.medspace.query_executor;

import de.unipassau.medspace.common.exception.UnsupportedServiceException;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.register.Datasource;
import de.unipassau.medspace.common.register.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;

/**
 * The Query executor is responsible to execute queries on all registered datasources.
 */
public class QueryExecutor implements Closeable {

  private static Logger log = LoggerFactory.getLogger(QueryExecutor.class);

  private final ServiceInvoker serviceInvoker;
  private final URL registerBase;
  private final URL dataCollectorBase;
  private final QueryCache queryCache;

  /**
   * Creates a new QueryExecutor object.
   * @param serviceInvoker The service invoker to use.
   * @param registerBase The base URL to the register.
   * @param dataCollectorBase The base URL to the data collector.
   */
  public QueryExecutor(ServiceInvoker serviceInvoker, URL registerBase, URL dataCollectorBase) {
    this.serviceInvoker = serviceInvoker;
    this.registerBase = registerBase;
    this.dataCollectorBase = dataCollectorBase;
    queryCache = new QueryCache(10, event->{
      if (event.getOldValue() == null) return;
      deleteQueryResult(event.getOldValue());
    });
  }

  /**
   * Clears the query cache.
   */
  public void clearCache() {
    queryCache.clear();
  }

  /**
   * Executes a keyword search query on all registered datasources and provides the resulting query result.
   * @param keywords The keywords used for querying.
   * @param operator The boolean operator to use.
   * @param rdfFormat The RDF language format the query result should use.
   * @return The query result of the keyword search query.
   * @throws IOException If an IO error occurs.
   */
  public InputStream keywordService(List<String> keywords,
                                    KeywordSearcher.Operator operator,
                                    String rdfFormat) throws IOException {

    BigInteger resultID = queryCache.get(keywords, operator);

    boolean cached = resultID != null;


    if (!cached) {
      resultID = queryKeywordService(keywords, operator);

      //add the query result to the cache
      queryCache.add(keywords, operator, resultID);
    }

    InputStream in;

    try {
      in = serviceInvoker.invokeDataCollectorGetQueryResult(dataCollectorBase, resultID, rdfFormat);
    } catch (IOException e) {
      try{
        serviceInvoker.invokeDataCollectorDeleteQueryResult(dataCollectorBase, resultID);
      } catch (IOException e1) {
        log.error("Couldn't delete query result:", e1);
      }
      throw new IOException("Couldn't get query result",e );
    }

    return new DataCollectorResultInputStream(in, resultID);
  }

  private void deleteQueryResult(BigInteger resultID) {
    try {
      serviceInvoker.invokeDataCollectorDeleteQueryResult(dataCollectorBase, resultID);
    } catch (IOException e) {
      log.error("Couldn't delete query result with resultID=" + resultID, e);
    }
  }

  private BigInteger queryKeywordService(List<String> keywords,
                                         KeywordSearcher.Operator operator) throws IOException {
    List<Datasource> datasources;
    String queryString = "keywords=";

    for (String keyword : keywords) {
      queryString += keyword + " ";
    }

    queryString = queryString.trim();

    //default is AND operator
    if (operator == KeywordSearcher.Operator.OR)
      queryString += "&useOr=true";

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

    Service service = Service.KEYWORD_SEARCH;
    Query query =  new Query(service, queryString);

    for (Datasource datasource : datasources) {
      log.info(datasource.toString());

      try (InputStream in = serviceInvoker.queryDatasourceInputStream(datasource, query)) {
        writeInputStreamToRepository(in, datasource.getUrl(), datasource.getRdfFormat(), resultID);
      }catch (UnsupportedServiceException e){

        // This type of exception shouldn't be thrown; if it is thrown nevertheless, there is a bug in code!

        // Release acquired resources
        deleteQueryResult(resultID);

        log.error("There is a bug in your code! Fix it!");
        throw new IOException("Service " + service.getName() + " not supported by data source " + datasource, e);

      } catch (IOException e) {

        log.error("Couldn't query datasource " + datasource.getUrl(), e);

        // inform the register that the data source has problems providing content
        try {
          serviceInvoker.invokeRegisterDatasourceIOError(registerBase, datasource);
        } catch (IOException e1) {
          log.error("IO Error while reporting register that an IO Error occurred while querying a datasource", e1);
        }
      }
    }

    return resultID;
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

  @Override
  public void close() throws IOException {
    queryCache.close();
  }


  /**
   * An utility class that wraps an input stream provided by the Data collector and stores a corresponding
   * ID of the query result repository where the input stream was created from.
   */
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
      in.close();
      log.debug("Closed DataCollectorResultInputStream with resultID=" + resultID);
    }
  }
}