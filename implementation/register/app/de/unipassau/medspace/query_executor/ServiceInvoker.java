package de.unipassau.medspace.query_executor;

import akka.stream.*;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.core.type.TypeReference;
import de.unipassau.medspace.common.exception.UnsupportedServiceException;
import de.unipassau.medspace.common.network.JsonResponse;
import de.unipassau.medspace.common.network.Util;
import de.unipassau.medspace.common.register.Datasource;
import de.unipassau.medspace.common.register.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.*;

import javax.inject.Inject;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Created by David Goeth on 06.10.2017.
 */
public class ServiceInvoker implements WSBodyReadables, WSBodyWritables {

  /**
   * Logger instance for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(ServiceInvoker.class);

  private static final Service REGISTER_GET_DATASOURCES_SUBPATH = new Service("get-datasources");

  private final WSClient ws;
  private final Materializer materializer;

  @Inject
  public ServiceInvoker(WSClient ws, Materializer materializer) {
    this.ws = ws;
    this.materializer = materializer;
  }

  public List<Datasource> invokeRegisterGetDatasources(URL registerBase) throws IOException {
    if (registerBase == null) throw new NullPointerException("datasource mustn't be null!");

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

  public DatasourceQueryResult queryDatasource(Datasource datasource, Query query) throws UnsupportedServiceException, IOException {
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

    WSResponse result = Util.getAndWait(request, 2);

    if (result == null) {
      throw new IOException("Error while retrieving rdf result data from the datasource " + datasource.getUrl());
    }

    String status = result.getStatusText();
    log.warn("QUERY String from source " + datasource.getUrl() + ": " + queryString);
    log.warn("STATUS from source " + datasource.getUrl() + ": " + result.getStatus() + " - " + status);
    log.warn("CONTENT-TYPE from source " + datasource.getUrl() + ": " + result.getContentType());
    //log.warn("BODY: " + result.getBody());

    Source<ByteString, ?> source = result.getBodyAsSource();

    DatasourceQueryResult queryResult = new DatasourceQueryResult(
        query,
        datasource,
        source,
        result.getContentType(),
        materializer);

    return queryResult;
  }

  private URL constructServiceURL(URL base, Service service) {
    try {
      return new URL(base, service.getName());
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Couldn't construct service url", e);
    }
  }
}