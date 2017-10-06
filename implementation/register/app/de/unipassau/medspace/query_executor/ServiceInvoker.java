package de.unipassau.medspace.query_executor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import de.unipassau.medspace.common.network.Response;
import de.unipassau.medspace.common.network.Util;
import de.unipassau.medspace.common.register.Datasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.WSBodyReadables;
import play.libs.ws.WSBodyWritables;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by David Goeth on 06.10.2017.
 */
public class ServiceInvoker implements WSBodyReadables, WSBodyWritables {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(ServiceInvoker.class);

  private final WSClient ws;

  @Inject
  public ServiceInvoker(WSClient ws) {
    this.ws = ws;
  }

  List<Datasource> invokeRegisterGetDatasources(URL registerBase) {
    if (registerBase == null) throw new NullPointerException("datasource mustn't be null!");

    URL serviceURL;
    try {
      serviceURL = new URL(registerBase, "getDatasources");
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Couldn't construct url to the register getDatasources service!", e);
    }

    WSRequest request = ws.url(serviceURL.toExternalForm())
        .setRequestTimeout(Duration.of(10, ChronoUnit.SECONDS))
        .setFollowRedirects(true);

    Response result = Util.getAndWait(request, 2);

    if (result.getException() != null) {
      log.error("Error while sending/retrieving data from the register", result.getException());
      return null;
    }

    JsonNode data = result.getData();

    List<Datasource.Builder> list;
    try {
      //list = Json.mapper().readValue(data.asText(), new TypeReference<Set<Datasource.Builder>>(){});
      list = new ObjectMapper().readValue(data.toString()
          , TypeFactory.defaultInstance().constructCollectionType(List.class,
              Datasource.Builder.class));
    } catch (IOException e) {
      log.error("Couldn't convert server message to an Response object: " + result.getData().toString(), e);
      return null;
    }

    List<Datasource> datasources = new ArrayList<>();
    for (Datasource.Builder builder : list) {
      datasources.add(builder.build());
    }

    return datasources;
  }
}