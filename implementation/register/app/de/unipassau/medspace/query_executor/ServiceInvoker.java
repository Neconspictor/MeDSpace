package de.unipassau.medspace.query_executor;

import com.fasterxml.jackson.core.type.TypeReference;
import de.unipassau.medspace.common.exception.UnsupportedServiceException;
import de.unipassau.medspace.common.network.JsonResponse;
import de.unipassau.medspace.common.network.Util;
import de.unipassau.medspace.common.register.Datasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.*;

import javax.inject.Inject;
import java.io.IOException;
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

  private static final String REGISTER_GET_DATASOURCES_SUBPATH = "getDatasources";

  private final WSClient ws;

  @Inject
  public ServiceInvoker(WSClient ws) {
    this.ws = ws;
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
    List<Datasource.Builder> list;
    try {
      list = Json.mapper().readValue(data, new TypeReference<List<Datasource.Builder>>(){});
    } catch (IOException e) {
      log.debug("JsonResponse object: ", data);
      throw new IOException("Couldn't convert server message to an JsonResponse object", e);
    }

    List<Datasource> datasources = new ArrayList<>();
    for (Datasource.Builder builder : list) {
      datasources.add(builder.build());
    }

    return datasources;
  }

  public void queryDatasource(Datasource datasource, String service, String queryString) throws UnsupportedServiceException, IOException {
    service = service.toLowerCase();
    if (!datasource.getServices().contains(service)) {
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
    log.warn("QUERY String: " + queryString);
    log.warn("STATUS: " + result.getStatus() + " - " + status);
    log.warn("CONTENT-TYPE: " + result.getContentType());
    log.warn("BODY: " + result.getBody());
  }

  private URL constructServiceURL(URL base, String service) {
    try {
      return new URL(base, service);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Couldn't construct service url", e);
    }
  }
}