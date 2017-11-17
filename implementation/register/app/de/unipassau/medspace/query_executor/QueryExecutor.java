package de.unipassau.medspace.query_executor;

import de.unipassau.medspace.common.exception.UnsupportedServiceException;
import de.unipassau.medspace.common.register.Datasource;
import de.unipassau.medspace.common.register.Service;
import de.unipassau.medspace.common.util.FileUtil;
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
import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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

    BigInteger resultID;
    try {
      resultID = getResultID();
    } catch (IOException e) {
      log.error("Couldn't create unique result id", e);
      return;
    }

    Service service = new Service("search");
    Query query =  new Query(service, queryString);

    for (Datasource datasource : datasources) {
      log.info(datasource.toString());

      try (DatasourceQueryResult queryResult = serviceInvoker.queryDatasource(datasource, query)){
        queryResult.future().whenComplete((file, error) -> {
            queryResultWhenCompleted(queryResult, datasource.getUrl(),file, resultID, error);
        });
        queryResult.future().get();
      } catch (ExecutionException | InterruptedException | IOException | UnsupportedServiceException e) {
        log.error("Couldn't query datasource " + datasource.getUrl(), e);
      }
    }
  }

  private List<Datasource> retrieveFromRegister() throws IOException {
    return serviceInvoker.invokeRegisterGetDatasources(registerBase);
  }

  private void queryResultWhenCompleted(DatasourceQueryResult result, URL source, File file,
                                        BigInteger resultID, Throwable error) {
    if (error != null) {
      log.error("Couldn't fetch query result", error);
      return;
    }

    try {
      writeFileToRepository(file, source, RDFFormat.TURTLE, resultID);
      queryRepository(System.out, RDFFormat.TURTLE, resultID);
    } catch (IOException e) {
      log.error("Couldn't cleanup query result", e);
    } finally {
      FileUtil.closeSilently(result, true);
    }
  }

  /**
   * Queries an rdf data_collector and writes the query result to an output stream.
   * @param out The output stream the query result should be written to.
   * @param format The rdf format that should be used for writing the query result to the output stream.
   * @param resultID The id of the query result
   * @throws IOException If an io error occurs.
   */
  private static void queryRepository(OutputStream out, RDFFormat format, BigInteger resultID) throws IOException {


    Set<Namespace> namespaces = null;

// write the rdf data to the data_collector
    try (ClosableRDFWriter writer = new ClosableRDFWriter(format, out)) {

      // write namespaces
      for(Namespace namespace : namespaces)
        writer.writeNamespace(namespace.getPrefix(), namespace.getName());

      // write statements
      try (RepositoryResult<Statement> result = conn.getStatements(null, null, null, contexts)) {
        while (result.hasNext()) {
          Statement st = result.next();
          writer.writeStatement(st);
        }
      }
    }

    // Open a connection to the database
   /* try (RepositoryConnection conn = db.getConnection()) {

      // write the rdf data to the data_collector
      try (ClosableRDFWriter writer = new ClosableRDFWriter(format, out)) {

        // write namespaces
        try(RepositoryResult<Namespace> result = conn.getNamespaces()) {
          while(result.hasNext()) {
            Namespace namespace = result.next();
            writer.writeNamespace(namespace.getPrefix(), namespace.getName());
          }
        }

        // write statements
        try (RepositoryResult<Statement> result = conn.getStatements(null, null, null, contexts)) {
          while (result.hasNext()) {
            Statement st = result.next();
            writer.writeStatement(st);
          }
        }
      }
    }*/
  }

  /**
   * Writes rdf content from a file to an rdf data collector.
   * @param file The file containing rdf data
   * @param baseURI The base URI the rdf data uses
   * @param format The rdf format used in the file
   * @param resultID The id of the query result
   * @throws IOException If an IO-Error occurs.
   */
  private void writeFileToRepository(File file, URL baseURI, RDFFormat format,
                                            BigInteger resultID) throws IOException {

    serviceInvoker.invokeDataCollectorAddPartialQueryResult(dataCollectorBase, resultID.toString(),
        format.toString(), baseURI.toExternalForm(), file);
  }

  private BigInteger getResultID() throws IOException {
    return serviceInvoker.invokeDataCollectorCreateQueryResultID(dataCollectorBase);
  }

  /**
   * This class is a wrapper for an RDF4J RDFWriter.
   */
  private static class ClosableRDFWriter implements AutoCloseable {

    /**
     * The wrapped rdf writer.
     */
    private final RDFWriter writer;

    /**
     * Creates a new {@link ClosableRDFWriter}.
     * @param format The rdf format that should be used for writing rdf.
     * @param out The rdf write destination.
     * @throws IOException If an io error occurs.
     */
    public ClosableRDFWriter(RDFFormat format, OutputStream out) throws IOException {
      writer = Rio.createWriter(format, out);
      writer.setWriterConfig(new WriterConfig());

      try{
        writer.startRDF();
      } catch (RDFHandlerException e) {
        throw new IOException("Couldn't start RDF writer stream", e);
      }
    }

    /**
     * Writes a given namespace to the output stream, this object holds.
     * @param prefix The prefix of the namespace.
     * @param uri The uri of the namespace.
     * @throws IOException If an io error occurs.
     */
    public void writeNamespace(String prefix, String uri) throws IOException {
      try{
        writer.handleNamespace(prefix, uri);
      } catch (RDFHandlerException e) {
        throw new IOException("Couldn't handle namespace", e);
      }
    }

    /**
     * Writes an rdf statement.
     * @param st The rdf statement.
     * @throws IOException If an io error occurs.
     */
    public void writeStatement(Statement st) throws IOException {
      try{
        writer.handleStatement(st);
      } catch (RDFHandlerException e) {
        throw new IOException("Couldn't handle statement", e);
      }
    }

    /**
     * Writes a comment.
     * @param comment The comment to write.
     * @throws IOException If an io error occurs.
     */
    public void writeComment(String comment) throws IOException {
      try{
        writer.handleComment(comment);
      } catch (RDFHandlerException e) {
        throw new IOException("Couldn't handle comment", e);
      }
    }

    /**
     * Closes this writer.
     * @throws IOException If an io error occurs.
     */
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