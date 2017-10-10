package de.unipassau.medspace.query_executor;

import akka.Done;
import akka.japi.function.Function;
import akka.japi.function.Procedure;
import akka.stream.*;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import akka.util.ByteStringBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import de.unipassau.medspace.common.exception.UnsupportedServiceException;
import de.unipassau.medspace.common.network.JsonResponse;
import de.unipassau.medspace.common.network.Util;
import de.unipassau.medspace.common.register.Datasource;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.apache.jena.shared.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.*;

import javax.inject.Inject;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

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
    //log.warn("BODY: " + result.getBody());

    Source<ByteString, ?> source = result.getBodyAsSource();

    //Source.<ByteString>actorRef(256, OverflowStrategy.dropNew())

    source.map((Function<ByteString, byte[]>) param -> param.toArray());

    // The sink that writes to the output stream
    ByteStringBuilder builder = new ByteStringBuilder();

    Sink<ByteString,  CompletionStage<Done>> outputWriter =
        Sink.<ByteString>foreach((ByteString bytes) -> {
          builder.append(bytes);

        });
    try {
      source.runWith(outputWriter, materializer)
      .whenComplete((value, error) -> {
        log.warn("Fetching done. Result:\n" + builder.result().utf8String());
      }).toCompletableFuture().get();
    } catch (ExecutionException |InterruptedException e) {
      log.error("Error while fecthing datasource response", e);
    }

    /*InputStream in = new ByteArrayInputStream(result.getBody().getBytes());
    TypedInputStream typedInput = new TypedInputStream(in, result.getContentType());
   /* Iterator<Triple> triples = RDFDataMgr.createIteratorTriples(typedInput.getInputStream(),
                                                               Lang.TURTLE, typedInput.getBaseURI());*/

    /*PipedRDFIterator<Triple> itTest = new PipedRDFIterator<>();
    PipedTriplesStream out = new PipedTriplesStream(itTest);
    RDFParser.create()
        .source(in)
        .base(typedInput.getBaseURI())
        .lang(Lang.TURTLE)
        .context(null)
        .parse(out);

    Iterator<Triple> triples = itTest;

    PrefixMapping mapping = PrefixMapping.Factory.create()
        .setNsPrefixes(itTest.getPrefixes().getMappingCopyStr());

    while(triples.hasNext()) {
      Triple triple = triples.next();
      log.warn("Read triple: " + triple.toString(mapping));
    }*/

   /* Model model = ModelFactory.createDefaultModel();
    RDFDataMgr.read(model, in, Lang.TURTLE);
    PrefixMapping mapping = PrefixMapping.Factory.create()
        .setNsPrefixes(model.getNsPrefixMap())
        .lock();

    StmtIterator it = model.listStatements();
    while (it.hasNext()) {
      Triple triple = it.nextStatement().asTriple();
      log.warn("Read triple: " + triple.toString(mapping));
    }*/

  }

  private URL constructServiceURL(URL base, String service) {
    try {
      return new URL(base, service);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Couldn't construct service url", e);
    }
  }
}