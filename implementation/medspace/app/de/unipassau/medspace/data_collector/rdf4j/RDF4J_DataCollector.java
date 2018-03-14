package de.unipassau.medspace.data_collector.rdf4j;

import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.rdf.rdf4j.RDF4JLanguageFormats;
import de.unipassau.medspace.common.rdf.rdf4j.WrappedStatement;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.data_collector.DataCollector;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO
 */
public class RDF4J_DataCollector extends DataCollector {

  /**
   * TODO
   */
  private final RepoManagerWrapper manager;

  /**
   * TODO
   */
  private final static String NAMED_GRAPH_BASE_IRI = "http://medspace.com/data_collector_ids/#";

  @Inject
  public RDF4J_DataCollector(LocalRepositoryManager manager) {
    this.manager = new RepoManagerWrapper(manager);
  }


  @Override
  public void addPartialQueryResult(BigInteger resultID, InputStream rdfData, String rdfFormat, String baseURI) throws
      IOException, NoValidArgumentException {

    String repoName = resultID.toString();
    RDFFormat format = RDF4JLanguageFormats.getFormatFromString(rdfFormat);

    try (RepositoryConnection conn = manager.getConnection(repoName)) {
      conn.add(rdfData, baseURI, format);
    } catch (IllegalStateException | RepositoryException e) {
      throw new IOException("Couldn't add rdf data to the query result repository.", e);
    }
  }

  @Override
  public BigInteger createQueryResult() throws IOException {
      BigInteger id = super.createQueryResult();
      manager.createRepository(id.toString());
      return id;
  }

  @Override
  public boolean deleteQueryResult(BigInteger resultID) throws NoValidArgumentException, IOException{

      String repoName = resultID.toString();
    try {
      manager.removeRepository(repoName);
    } catch (InterruptedException e) {
      throw new IOException("Couldn't remove repo with id: " + resultID, e);
    }
    return true;
  }

  @Override
  public Set<Namespace> getNamespaces(BigInteger resultID) throws IOException {
    Set<Namespace> namespaces = new HashSet<>();

    try(RepositoryConnection conn = manager.getConnection(resultID.toString())) {
      try(RepositoryResult<org.eclipse.rdf4j.model.Namespace> result = conn.getNamespaces()) {

        while(result.hasNext()) {
          org.eclipse.rdf4j.model.Namespace modelNamespace = result.next();
          namespaces.add(new Namespace(modelNamespace.getPrefix(), modelNamespace.getName()));
        }
      }
    } catch (RepositoryException e) {
      throw new IOException("Couldn't retrieve namespaces from repository for resultID=" + resultID,e);
    }

    return namespaces;
  }

  @Override
  public Stream<Triple> queryResult(BigInteger resultID, String rdfFormat) throws IOException {
    try {
      RepositoryConnection conn = manager.getConnection(resultID.toString());
      RepositoryResult<Statement> result = conn.getStatements(null, null, null);

      //connection is closed in RepositoryResultStream
      return new RepositoryResultStream(result, conn);
    } catch (RepositoryException e) {
     throw new IOException("Couldn't retrieve query result from repository for resultID=" + resultID,e);
    }
  }

  /**
   * TODO
   * @param resultID
   * @return
   */
  private Resource getNamedGraph(BigInteger resultID) {
    return SimpleValueFactory.getInstance().createIRI(NAMED_GRAPH_BASE_IRI + resultID);
  }

  /**
   * TODO
   */
  private class RepositoryResultStream implements Stream<Triple> {

    /**
     * TODO
     */
    private final RepositoryResult<Statement> result;

    /**
     * TODO
     */
    private final RepositoryConnection conn;

    /**
     * TODO
     * @param result
     * @param conn
     */
    public RepositoryResultStream(RepositoryResult<Statement> result, RepositoryConnection conn) {
      this.result = result;
      this.conn = conn;
    }

    @Override
    public Triple next() throws IOException {
      Statement stmt;
      try{
        stmt = result.next();
      } catch (RepositoryException e) {
        throw new IOException("Couldn't retrieve next triple", e);
      }

      return new WrappedStatement(stmt);
    }

    @Override
    public boolean hasNext() throws IOException {
      try{
        return result.hasNext();
      } catch (RepositoryException e) {
        throw new IOException("Error while accessing repository result", e);
      }
    }

    @Override
    public void close() throws IOException {
      try {
        result.close();
      } catch (RepositoryException e) {
        FileUtil.closeSilently(conn);
        throw new IOException("Couldn't close repository result", e);
      }

      try {
        conn.close();
      } catch (RepositoryException e) {
        throw new IOException("Couldn't close repository connection", e);
      }
    }
  }
}