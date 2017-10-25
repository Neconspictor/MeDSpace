package de.unipassau.medspace.query_executor;

import de.unipassau.medspace.common.exception.UnsupportedServiceException;
import de.unipassau.medspace.common.rdf.FileTripleStream;
import de.unipassau.medspace.common.register.Datasource;
import de.unipassau.medspace.common.register.Service;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.store.DatasetGraphTDB;
import org.apache.jena.tdb.store.GraphNonTxnTDB;
import org.apache.jena.tdb.sys.TDBInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * The Query executor is responsible to execute a query on a database.
 */
public class QueryExecutor {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(QueryExecutor.class);

  private static String dir = "./_work/query-executor/test/dataset1";

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
        queryResult.future().get();
      } catch (IOException | UnsupportedServiceException e) {
        log.error("Error while querying datasource " + datasource.getUrl(), e);
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }

    Dataset dataset = TDBFactory.createDataset(dir);

    dataset.begin(ReadWrite.READ);
    Model model = dataset.getDefaultModel();
    StmtIterator it = model.listStatements();
    PrefixMapping mapping = model.getGraph().getPrefixMapping();
    while(it.hasNext()) {
      Statement stmt = it.nextStatement();
      Triple triple = stmt.asTriple();
      log.warn("Read triple: " + triple.toString(mapping));
    }
    dataset.end();
    dataset.close();
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
    TripleSink sink = new TripleSink(dir);

    try {
      StreamRDF stream = StreamRDFLib.sinkTriples(sink);
      RDFDataMgr.parse(stream, file.getAbsolutePath(), Lang.NTRIPLES) ;
    } catch (Exception e) {
      throw new IOException("Couldn't create triple stream", e);
    } finally {
      sink.close();
    }
  }

  private static class TripleSink implements Sink<Triple> {

    private Dataset dataset;

    public TripleSink(String directory) {
      dataset = TDBFactory.createDataset(directory);
    }

    @Override
    public void send(Triple item) {
      /*dataset.begin(ReadWrite.WRITE);

      Model model = dataset.getDefaultModel();
      Graph graph = model.getGraph();
      graph.add(item);

      dataset.commit();
      dataset.end();*/

      dataset.begin(ReadWrite.WRITE);
      DatasetGraphTDB dsg;
      dsg = TDBInternal.getDatasetGraphTDB(dataset);
      GraphNonTxnTDB test = dsg.getDefaultGraphTDB();
      test.add(item);
      dataset.commit();
      dataset.end();
    }

    @Override
    public void flush() {
      //dataset.end();
      //dsg.end();
    }

    @Override
    public void close() {
      dataset.close();
      //dsg.close();
    }
  }
}