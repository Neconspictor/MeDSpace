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
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by David Goeth on 07.11.2017.
 */
public class RDF4J_DataCollector extends DataCollector {

  private final RepositoryManagerWrapper manager;

  private final static String NAMED_GRAPH_BASE_IRI = "http://medspace.com/data_collector_ids/#";

  @Inject
  public RDF4J_DataCollector(RepositoryManager manager) {
    this.manager = new RepositoryManagerWrapper(manager);
  }


  @Override
  public void addPartialQueryResult(BigInteger resultID, InputStream rdfData, String rdfFormat, String baseURI) throws
      NoValidArgumentException, IOException {

    String repoName = resultID.toString();
    Repository repo = null;

    try {
      repo = manager.getRepository(repoName);
    } catch (RepositoryException e) {}

    if (repo == null) {
      throw new NoValidArgumentException("resultID isn't a registered query result. To create a new query result " +
          "invoke the 'createQueryResult' service");
    }

    RDFFormat format = RDF4JLanguageFormats.getFormatFromString(rdfFormat);

    Lock lock = manager.getLock(repo);

    lock.lock();

    try (RepositoryConnection conn = repo.getConnection()) {
      conn.add(rdfData, baseURI, format);
    } catch (IllegalStateException | RepositoryException e) {
      throw new IOException("Couldn't add rdf data to the query result repository.", e);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public BigInteger createQueryResult() {
    BigInteger id = super.createQueryResult();
    manager.addResultID(id);

    return id;
  }

  @Override
  public boolean deleteQueryResult(BigInteger resultID) throws NoValidArgumentException, IOException{


    String repoName = resultID.toString();
    Repository repo = manager.getRepository(repoName);
    if (repo == null) {
      return false;
    }

    manager.removeRepository(repoName);
    return true;
  }

  @Override
  public Stream<Triple> queryResult(String rdfFormat, BigInteger resultID) throws IOException {

    Resource namedGraph = getNamedGraph(resultID);

    Repository repo = manager.getRepository(resultID.toString());

    try {
      RepositoryConnection conn = repo.getConnection();
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

  //TODO proper synchronization
  private static class RepositoryManagerWrapper {

    private final RepositoryManager manager;
    private Map<Repository, ReadWriteLock> repoLockMap;

    public  RepositoryManagerWrapper(RepositoryManager manager) {
      this.manager = manager;
      repoLockMap = new ConcurrentHashMap<>();
    }

    public Repository getRepository(String identity) {
      Repository repo = manager.getRepository(identity);
      if (repo != null)
        repoLockMap.putIfAbsent(repo, new ReentrantReadWriteLock());

      return repo;
    }

    public Lock getLock(Repository repo) {
        ReadWriteLock readWriteLock = repoLockMap.get(repo);

        if (readWriteLock == null) {
          throw new IllegalArgumentException("No lock found!");
        }

        return readWriteLock.readLock();
    }

    public synchronized void addResultID(BigInteger resultID) {

      String repoName = resultID.toString();
      Repository repo = null;

      try {
        repo = manager.getRepository(repoName);
      } catch (RepositoryException e) {}

      // Already added?
      if (repo != null) {
        return;
      }

      RepositoryConfig config = new RepositoryConfig(resultID.toString());
      config.setRepositoryImplConfig(new SailRepositoryConfig(new NativeStoreConfig()));
      manager.addRepositoryConfig(config);
    }


    public void removeRepository(String repoName) throws IOException {

      Lock lock;

      Repository repo = manager.getRepository(repoName);

      if (repo == null) {
        throw new IOException("Repository with name '" + repoName + "' not registered.");
      }

      lock = repoLockMap.get(repo).writeLock();

      lock.lock();
        try {
          repoLockMap.remove(repo);
          manager.removeRepository(repoName);
        } catch (IllegalStateException | RepositoryException e) {
          throw new IOException("Couldn't remove repository '" + repoName + "'", e);
        } finally {
          lock.unlock();
        }
    }
  }
}