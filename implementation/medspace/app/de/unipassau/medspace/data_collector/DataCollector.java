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
 * TODO
 */
public abstract class DataCollector {

  /**
   * Logger instance of this class.
   */
  private static final Logger log = LoggerFactory.getLogger(DataCollector.class);


  /**
   * TODO
   */
  protected BigInteger nextID = BigInteger.ZERO;

  /**
   * TODO
   */
  protected final Object nextIdLock = new Object();


  /**
   * TODO
   */
  public DataCollector() { }

  /**
   * TODO
   * @return
   * @throws IOException
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
   * TODO
   * @param resultID
   * @param rdfData
   * @param rdfFormat
   * @param baseURI
   * @throws NoValidArgumentException
   * @throws IOException
   */
  public abstract void addPartialQueryResult(BigInteger resultID, InputStream rdfData, String rdfFormat, String baseURI)
      throws NoValidArgumentException, IOException;

  /**
   * TODO
   * @param resultID
   * @return
   * @throws NoValidArgumentException
   * @throws IOException
   */
  public abstract boolean deleteQueryResult(BigInteger resultID) throws NoValidArgumentException, IOException;

  /**
   * TODO
   * @param resultID
   * @return
   * @throws IOException
   */
  public abstract Set<Namespace> getNamespaces(BigInteger resultID) throws IOException;

  /**
   * TODO
   * @param rdfFormat
   * @param resultID
   * @return
   * @throws IOException
   */
  public abstract Stream<Triple> queryResult(String rdfFormat, BigInteger resultID) throws IOException;

  //TODO open and close repos to each query result
}