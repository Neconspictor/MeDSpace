package de.unipassau.medspace.query_executor;

import de.unipassau.medspace.common.exception.UnsupportedServiceException;
import de.unipassau.medspace.common.register.Datasource;
import de.unipassau.medspace.common.register.Service;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
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
  private final Repository db;

  public QueryExecutor(ServiceInvoker serviceInvoker, URL registerBase, Repository db) {

    this.serviceInvoker = serviceInvoker;
    this.registerBase = registerBase;
    this.db = db;
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
          queryResultWhenCompleted(queryResult, datasource.getUrl(),file, db, error));
        queryResult.future().get();
      } catch (IOException | UnsupportedServiceException e) {
        log.error("Error while querying datasource " + datasource.getUrl(), e);
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
  }

  private List<Datasource> retrieveFromRegister() throws IOException {
    return serviceInvoker.invokeRegisterGetDatasources(registerBase);
  }

  private void queryResultWhenCompleted(DatasourceQueryResult result, URL source, File file, Repository db, Throwable error) {
    if (error != null) {
      log.error("Couldn't fetch query result", error);
      return;
    }
    /*try {
      //processQueryResult(file,source, result.getContentType());
    } catch (IOException e) {
      log.error("Error while reading query result file", e);
    }*/

    ValueFactory factory = SimpleValueFactory.getInstance();
    Resource namedGraph = factory.createIRI(source.toString());

    try {
      writeFileToRepository(file, source, RDFFormat.TURTLE, db, namedGraph);
      queryRepository(System.out, db, namedGraph);
      result.cleanup();
    } catch (IOException e) {
      log.error("Couldn't cleanup query result", e);
    }
  }

  private static void queryRepository(OutputStream out, Repository db, Resource... contexts) throws IOException {
    // Open a connection to the database
    try (RepositoryConnection conn = db.getConnection()) {
      try (ClosableRDFWriter writer = new ClosableRDFWriter(RDFFormat.TURTLE, System.out)) {

        try(RepositoryResult<Namespace> result = conn.getNamespaces()) {
          while(result.hasNext()) {
            Namespace namespace = result.next();
            writer.handleNamespace(namespace.getPrefix(), namespace.getName());
          }
        }

        try (RepositoryResult<Statement> result = conn.getStatements(null, null, null, contexts);) {
          while (result.hasNext()) {
            Statement st = result.next();
            writer.handleStatement(st);
          }
        }
      }
    }
  }

  private static void writeFileToRepository(File file, URL baseURI, RDFFormat format, Repository db, Resource... contexts) throws IOException {
    try (RepositoryConnection conn = db.getConnection()) {
      //RDFParser rdfParser = new TurtleParser();
      //RDFHandler repositoryWriter = new RepositoryWriter(conn, contexts);
      //rdfParser.setRDFHandler(repositoryWriter);

      //ByteArrayOutputStream out = new ByteArrayOutputStream();
      //Rio.write(model, out, RDFFormat.TURTLE);
      //rdfParser.parse(new ByteArrayInputStream(out.toByteArray()), "");
      conn.add(file, baseURI.toString(), format, contexts);
    }
  }

  private static class RepositoryWriter implements RDFHandler {

    private final RepositoryConnection connection;
    private final ArrayList<Resource> namedGraphs;

    private RepositoryWriter(RepositoryConnection connection, Resource... namedGraphs) {
      this.connection = connection;
      this.namedGraphs = new ArrayList<>();

      for (Resource namedGraph : namedGraphs) {
        if (namedGraph != null) {
          this.namedGraphs.add(namedGraph);
        }
      }
    }

    @Override
    public void startRDF() throws RDFHandlerException {}

    @Override
    public void endRDF() throws RDFHandlerException {}

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
      connection.setNamespace(prefix, uri);
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
      Resource context = st.getContext();
      List<Resource> namedGraphs = new ArrayList<>(this.namedGraphs);

      if (context != null)
        namedGraphs.add(context);

      Resource[] resources = new Resource[namedGraphs.size()];
      resources = namedGraphs.toArray(resources);
      connection.add(st, resources);
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {}
  }

  private static class ClosableRDFWriter implements AutoCloseable {

    private final RDFWriter writer;

    public ClosableRDFWriter(RDFFormat format, OutputStream out) throws IOException {
      writer = Rio.createWriter(format, out);
      writer.setWriterConfig(new WriterConfig());

      try{
        writer.startRDF();
      } catch (RDFHandlerException e) {
        throw new IOException("Couldn't start RDF writer stream", e);
      }
    }

    public void handleNamespace(String prefix, String uri) throws IOException {
      try{
        writer.handleNamespace(prefix, uri);
      } catch (RDFHandlerException e) {
        throw new IOException("Couldn't handle namespace", e);
      }
    }

    public void handleStatement(Statement st) throws IOException {
      try{
        writer.handleStatement(st);
      } catch (RDFHandlerException e) {
        throw new IOException("Couldn't handle statement", e);
      }
    }

    public void handleComment(String comment) throws IOException {
      try{
        writer.handleComment(comment);
      } catch (RDFHandlerException e) {
        throw new IOException("Couldn't handle comment", e);
      }
    }

    @Override
    public void close() throws IOException {
      try{
        writer.endRDF();
      } catch (RDFHandlerException e) {
        throw new IOException("Couldn't stop RDF writer stream", e);
      }
    }
  }
}