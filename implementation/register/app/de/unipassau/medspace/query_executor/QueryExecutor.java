package de.unipassau.medspace.query_executor;

import de.unipassau.medspace.common.exception.UnsupportedServiceException;
import de.unipassau.medspace.common.rdf.FileTripleStream;
import de.unipassau.medspace.common.register.Datasource;
import de.unipassau.medspace.common.register.Service;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
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

  public QueryExecutor(ServiceInvoker serviceInvoker, URL registerBase) {

    this.serviceInvoker = serviceInvoker;
    this.registerBase = registerBase;
  }

  public void keywordService(List<String> keywords) {
    List<Datasource> datasources;

    String queryString = "keywords=";

    for (String keyword : keywords) {
      queryString += keyword + " ";
    }

    queryString = queryString.trim();

    try {
      datasources = retrieveFromRegister();
    } catch (IOException e) {
      log.error("Couldn't retrieve datasource list from the register!", e);
      return;
    }

    Service service = new Service("search");
    Query query =  new Query(service, queryString);

    for (Datasource datasource : datasources) {
      log.info(datasource.toString());

      try {
        DatasourceQueryResult queryResult = serviceInvoker.queryDatasource(datasource, query);
        queryResult.future().whenComplete((file, error) ->
          queryResultWhenCompleted(queryResult, datasource.getUrl(),file, error));
      } catch (IOException | UnsupportedServiceException e) {
        log.error("Error while querying datasource " + datasource.getUrl(), e);
      }
    }
  }

  private List<Datasource> retrieveFromRegister() throws IOException {
    return serviceInvoker.invokeRegisterGetDatasources(registerBase);
  }

  private void queryResultWhenCompleted(DatasourceQueryResult result, URL source, File file, Throwable error) {
    if (error != null) {
      log.error("Couldn't fetch query result", error);
      return;
    }
    try {
      processQueryResult(file,source, result.getContentType());
    } catch (IOException e) {
      log.error("Error while reading query result file", e);
    }

    try {
      result.cleanup();
    } catch (IOException e) {
      log.error("Couldn't cleanup query result", e);
    }
  }

  private void processQueryResult(File file, URL source, String contentType) throws IOException{

    FileTripleStream stream = null;

    try {
      stream = new FileTripleStream(file, contentType);
      PrefixMapping mapping = stream.getPrefixMapping();
      while(stream.hasNext()) {
        Triple triple = stream.next();
        log.warn("Read triple from source " + source + ": " + triple.toString(mapping));
      }
    } catch (URISyntaxException e) {
      throw new IOException("Couldn't create triple stream", e);
    } finally {
      if (stream != null) stream.close();
    }

    /*InputStream in = null;
    try {
      in = new FileInputStream(file.getPath());
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String line = reader.readLine();
      while(line != null) {
        log.warn("Read result line from source " + source + ": " + line);
        line = reader.readLine();
      }
    } catch (FileNotFoundException e) {
      throw new RuntimeException("QueryResult file not findable", e);
    } finally {
      if (in != null)
        in.close();
    }*/
  }
}