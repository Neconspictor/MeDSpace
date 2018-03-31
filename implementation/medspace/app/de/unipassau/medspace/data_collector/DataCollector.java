package de.unipassau.medspace.data_collector;

import de.unipassau.medspace.common.exception.NoValidArgumentException;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Set;

/**
 * Defines and implements base functionality of the Data Collector module.
 */
public abstract class DataCollector {

  private static final Logger log = LoggerFactory.getLogger(DataCollector.class);


  /**
   * A counter for query result IDs
   */
  protected BigInteger nextID = BigInteger.ZERO;

  /**
   * Lock for synchronizing access to 'nexID'
   */
  protected final Object nextIdLock = new Object();


  /**
   * Creates a new DataCollector object.
   */
  public DataCollector() { }

  /**
   * Creates a new query result ID.
   * @return The new query result ID.
   * @throws IOException If an IO error occurs.
   */
  public BigInteger createQueryResult() throws IOException {
    synchronized (nextIdLock) {
      log.debug("Old value of nextID: " + nextID);
      nextID = nextID.add(BigInteger.ONE);
      log.debug("New value of nextID: " + nextID);
      return nextID;
    }
  }

  /**
   * Adds a partial query result to an existing (query result) repository.
   * @param resultID The ID of the repository.
   * @param rdfData The RDF data
   * @param rdfFormat The RDF language format of the RDF data.
   * @param baseURI The base URI used in the RDF data.
   * @throws NoValidArgumentException If one of the arguments is not valid.
   * @throws IOException If an IO error occurs.
   */
  public abstract void addPartialQueryResult(BigInteger resultID, InputStream rdfData, String rdfFormat, String baseURI)
      throws NoValidArgumentException, IOException;

  /**
   * Deletes a query result repository.
   * @param resultID The ID of the repository.
   * @return true if the repository was successfully removed.
   * @throws NoValidArgumentException If the resultID cannot be matched to a repository.
   * @throws IOException If an IO error occurs.
   */
  public abstract boolean deleteQueryResult(BigInteger resultID) throws NoValidArgumentException, IOException;

  /**
   * Provides the namespaces used in a query result repository.
   * @param resultID The ID of the repository.
   * @return the namespaces used in a query result repository.
   * @throws IOException If an IO error occurs.
   */
  public abstract Set<Namespace> getNamespaces(BigInteger resultID) throws IOException;

  /**
   * Provides the content of a query result repository.
   * @param resultID The ID of the repository.
   * @param rdfFormat The RDF language format to use for the RDF data.
   * @return the content of a query result repository.
   * @throws IOException If an IO error occurs.
   */
  public abstract Stream<Triple> queryResult(BigInteger resultID, String rdfFormat) throws IOException;
}