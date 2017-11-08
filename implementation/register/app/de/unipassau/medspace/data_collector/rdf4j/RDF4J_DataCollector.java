package de.unipassau.medspace.data_collector.rdf4j;

import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.rdf.rdf4j.RDF4JLanguageFormats;
import de.unipassau.medspace.common.rdf.rdf4j.WrappedStatement;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.data_collector.DataCollector;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

/**
 * Created by David Goeth on 07.11.2017.
 */
public class RDF4J_DataCollector extends DataCollector {

  private final Repository db;

  private final static String NAMED_GRAPH_BASE_IRI = "http://medspace.com/data_collector_ids/#";

  @Inject
  public RDF4J_DataCollector(Repository db) {
    this.db = db;
  }


  @Override
  public void addPartialQueryResult(BigInteger resultID, InputStream rdfData, String rdfFormat, String baseURI) throws
      NoValidArgumentException, IOException {

    RDFFormat format = RDF4JLanguageFormats.getFormatFromString(rdfFormat);
    Resource namedGraph = getNamedGraph(resultID);

    try (RepositoryConnection conn = db.getConnection()) {
      conn.add(rdfData, baseURI, format, new Resource[]{namedGraph});
    }
  }

  @Override
  public void deleteQueryResult(BigInteger resultID) throws NoValidArgumentException, IOException{

    Resource namedGraph = getNamedGraph(resultID);
    try (RepositoryConnection conn = db.getConnection()) {
      conn.clear(namedGraph);
    } catch (RepositoryException e) {
      throw new IOException("Error while trying to delete query result with id=" + resultID, e);
    }
  }

  @Override
  public Stream<Triple> queryResult(String rdfFormat, BigInteger resultID) throws IOException {

    Resource namedGraph = getNamedGraph(resultID);

    try {
      RepositoryConnection conn = db.getConnection();
      RepositoryResult<Statement> result = conn.getStatements(null, null, null, namedGraph);
      return new RepositoryResultStream(result, conn);
    } catch (RepositoryException e) {
     throw new IOException("Couldn't retrieve query result from repository",e);
    }
  }

  private Resource getNamedGraph(BigInteger resultID) {
    return SimpleValueFactory.getInstance().createIRI(NAMED_GRAPH_BASE_IRI + resultID);
  }


  private class RepositoryResultStream implements Stream<Triple> {

    private final RepositoryResult<Statement> result;

    private final RepositoryConnection conn;

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