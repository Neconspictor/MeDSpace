package de.unipassau.medspace.query_executor;

import akka.stream.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import de.unipassau.medspace.common.exception.UnsupportedServiceException;
import de.unipassau.medspace.common.network.JsonResponse;
import de.unipassau.medspace.common.network.Util;
import de.unipassau.medspace.common.network.data_collector.UniqueIdResponse;
import de.unipassau.medspace.common.register.Datasource;
import de.unipassau.medspace.common.register.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.*;
import play.mvc.Http;

import javax.inject.Inject;
import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * An utility class used to execute services from MeDSpace modules.
 */
public class ServiceInvoker implements WSBodyReadables, WSBodyWritables {

  private static final Logger log = LoggerFactory.getLogger(ServiceInvoker.class);

  private static final Service REGISTER_GET_DATASOURCES_SUBPATH = new Service("get-datasources");

  private static final Service REGISTER_DATASOURCE_IO_ERROR = new Service("io-error");

  private static final Service DATA_COLLECTOR_ADD_PARTIAL_QUERY_RESULT = new Service("add-partial-query-result");

  private static final Service DATA_COLLECTOR_CREATE_QUERY_RESULT = new Service("create-query-result");

  private static final Service DATA_COLLECTOR_DELETE_QUERY_RESULT = new Service("delete-query-result");

  private static final Service DATA_COLLECTOR_QUERY_RESULT = new Service("query-result");

  private final WSClient ws;
  private final Materializer materializer;

  /**
   * Creates a new ServiceInvoker object.
   * @param ws The web socket client to use.
   * @param materializer The materializer to use.
   */
  @Inject
  public ServiceInvoker(WSClient ws, Materializer materializer) {
    this.ws = ws;
    this.materializer = materializer;
  }


  /**
   * Invokes the REST service 'add partial query result' from the Data Collector module.
   *
   * @param dataCollectorBase The base URL of the Data Collector.
   * @param resultID The ID of the query result repository.
   * @param rdfFormat The RDF language format of the partial query result.
   * @param baseURI The base URI of the partial RDF query result.
   * @param in The partial RDF query result which should be added.
   * @throws IOException If an IO error occurs.
   */
  public void invokeDataCollectorAddPartialQueryResultInputStream(URL dataCollectorBase, String resultID,
                                                                 String rdfFormat, String baseURI,
                                                                 InputStream in) throws IOException {

    URL serviceURL = constructServiceURL(dataCollectorBase, DATA_COLLECTOR_ADD_PARTIAL_QUERY_RESULT);

    WSRequest request = ws.url(serviceURL.toExternalForm())
        .addQueryParameter("resultID", resultID)
        .addQueryParameter("rdfFormat", rdfFormat)
        .addQueryParameter("baseURI", baseURI)
        .setRequestTimeout(Duration.of(200, ChronoUnit.SECONDS))
        .setFollowRedirects(true);

    WSResponse response = Util.postInputStreamAndWait(request, in, 1);

    if (Http.Status.OK != response.getStatus()) {
      throw new IOException("Error while adding a partial query result to the data-collector: response message= "
          + response.getBody());
    }
  }

  /**
   * Invokes the REST service 'create query result ID' from the Data Collector module.
   * This service creates a new ID for a (new) query result repository.
   *
   * @param dataCollectorBase The base URL of the Data Collector.
   * @return The created ID for the query result repository.
   * @throws IOException If an IO error occurs.
   */
  public BigInteger invokeDataCollectorCreateQueryResultID(URL dataCollectorBase) throws IOException {
    if (dataCollectorBase == null) throw new NullPointerException("dataCollector URL mustn't be null!");

    URL serviceURL = constructServiceURL(dataCollectorBase, DATA_COLLECTOR_CREATE_QUERY_RESULT);

    WSRequest request = ws.url(serviceURL.toExternalForm())
        .setFollowRedirects(true);

    JsonResponse result = Util.getAndWaitJson(request, 1);

    if (result.getException() != null) {
      throw new IOException("Error while sending/retrieving data from the data-collector", result.getException());
    }

    UniqueIdResponse reponse;

    try {
      reponse = Json.fromJson(result.getData(), UniqueIdResponse.class);
    } catch (RuntimeException e) {
      log.debug("JsonResponse object: ", result.getData());
      throw new IOException("Couldn't convert server message to an JsonResponse object", e);
    }

    return reponse.getId();
  }

  /**
   * Invokes the REST service 'delete query result' from the Data Collector module.
   *
   * @param dataCollectorBase The base URL of the Data Collector.
   * @param resultID The ID of the query result repository.
   * @throws IOException If an IO error occurs.
   */
  public void invokeDataCollectorDeleteQueryResult(URL dataCollectorBase, BigInteger resultID) throws IOException {
    if (dataCollectorBase == null) throw new NullPointerException("dataCollector URL mustn't be null!");

    URL serviceURL = constructServiceURL(dataCollectorBase, DATA_COLLECTOR_DELETE_QUERY_RESULT);

    WSRequest request = ws.url(serviceURL.toExternalForm())
        .addQueryParameter("resultID", resultID.toString())
        .setFollowRedirects(true);

    WSResponse response = Util.getAndWait(request, 1);

    if (response.getStatus() != Http.Status.OK) {
      String status = response.getStatusText();
      String message = response.getBody();
      String errorMessage = status + ": " + message;
      throw new IOException("Data-Collector response error: " + errorMessage);
    }
  }

  /**
   * Invokes the REST service 'query result' from the Data Collector module.
   *
   * @param dataCollectorBase The base URL of the Data Collector.
   * @param resultID The ID of the query result repository.
   * @param rdfFormat The RDF language format the result should be exported.
   * @return The query result.
   * @throws IOException If an IO error occurs.
   */
  public InputStream invokeDataCollectorGetQueryResult(URL dataCollectorBase, BigInteger resultID,
                                                       String rdfFormat) throws IOException {

    URL serviceURL = constructServiceURL(dataCollectorBase, DATA_COLLECTOR_QUERY_RESULT);
    WSRequest request = ws.url(serviceURL.toExternalForm())
        .addQueryParameter("resultID", resultID.toString())
        .addQueryParameter("rdfFormat", rdfFormat)
        .setFollowRedirects(true);
    return Util.getGETInputStream(request);

  }

  /**
   * Invokes the REST service 'get datasources' from the Register module.
   * @param registerBase The base URL of the Register.
   * @return The list of registered datasources.
   * @throws IOException If an IO error occurs.
   */
  public List<Datasource> invokeRegisterGetDatasources(URL registerBase) throws IOException {
    if (registerBase == null) throw new NullPointerException("register URL mustn't be null!");

    URL serviceURL = constructServiceURL(registerBase, REGISTER_GET_DATASOURCES_SUBPATH);

    WSRequest request = ws.url(serviceURL.toExternalForm())
        .setRequestTimeout(Duration.of(10, ChronoUnit.SECONDS))
        .setFollowRedirects(true);

    JsonResponse result = Util.getAndWaitJson(request, 2);

    if (result.getException() != null) {
      throw new IOException("Error while sending/retrieving data from the register", result.getException());
    }

    String data = result.getData().toString();

    // We cannot deserialize the result directly to Datasource objects, as the class is immutable and has a
    // private constructor.
    // Instead we use its builder which has the same member fields
    List<Datasource> datasources;
    try {
      datasources = Json.mapper().readValue(data, new TypeReference<List<Datasource>>(){});
    } catch (IOException e) {
      log.debug("JsonResponse object: ", data);
      throw new IOException("Couldn't convert server message to an JsonResponse object", e);
    }

    return datasources;
  }

  /**
   * Invokes the REST service 'io error' from the Register module.
   * This REST service provides the number of occured io errors for a specific datasource.
   *
   * @param registerBase The base URL of the Register.
   * @param datasource The datasource to get the IO error counter.
   * @throws IOException If an IO error occurs.
   */
  public void invokeRegisterDatasourceIOError(URL registerBase, Datasource datasource) throws IOException {
    if (registerBase == null) throw new NullPointerException("register URL mustn't be null!");

    URL serviceURL = constructServiceURL(registerBase, REGISTER_DATASOURCE_IO_ERROR);

    WSRequest request = ws.url(serviceURL.toExternalForm())
        .setRequestTimeout(Duration.of(10, ChronoUnit.SECONDS))
        .setFollowRedirects(true);

    JsonNode root = Json.toJson(datasource);

    WSResponse result = Util.postJsonAndWait(request, root, 2);

    if (result == null) {
      throw new IOException("IO Error while sending/retrieving data from the register");
    }
  }


  /**
   * Executes a query on a datasource.
   *
   * @param datasource The datasource
   * @param query The query that should be executed by the datasource.
   * @return The query result provided by the datasource.
   * @throws UnsupportedServiceException If the datasource doesn't support the service type of the query.
   * @throws IOException If an IO error occurs.
   */
  public InputStream queryDatasourceInputStream(Datasource datasource, Query query) throws UnsupportedServiceException,
      IOException {
    Service service = query.getService();
    String queryString = query.getQueryString();

    if (!Service.supportsService(service, datasource)) {
      throw new UnsupportedServiceException(datasource.getUrl() + " doesn't support service '" + service + "'");
    }

    URL serviceURL = constructServiceURL(datasource.getUrl(), service);

    WSRequest request = ws.url(serviceURL.toExternalForm())
        .setRequestTimeout(Duration.of(30, ChronoUnit.SECONDS))
        .setQueryString(queryString)
        .setFollowRedirects(true);

    return Util.getGETInputStream(request);
  }

  private URL constructServiceURL(URL base, Service service) {
    try {
      return new URL(base, service.getName());
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Couldn't construct service url", e);
    }
  }
}