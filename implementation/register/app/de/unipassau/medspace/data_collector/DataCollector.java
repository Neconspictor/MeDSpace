package de.unipassau.medspace.data_collector;

import com.google.inject.Inject;
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
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by David Goeth on 07.11.2017.
 */
public abstract class DataCollector {

  private static final Logger log = LoggerFactory.getLogger(DataCollector.class);

  //protected final AtomicBigInteger nextID = new AtomicBigInteger(BigInteger.ZERO);
  protected BigInteger nextID = BigInteger.ZERO;
  protected final Object nextIdLock = new Object();


  @Inject
  public DataCollector() {
  }

  public BigInteger createQueryResult() {
    synchronized (nextIdLock) {
      log.debug("Old value of nextID: " + nextID);
      nextID = nextID.add(BigInteger.ONE);
      log.debug("New value of nextID: " + nextID);
      return nextID;
    }
  }

  public abstract void addPartialQueryResult(BigInteger resultID, InputStream rdfData, String rdfFormat, String baseURI)
      throws NoValidArgumentException, IOException;

  public abstract boolean deleteQueryResult(BigInteger resultID) throws NoValidArgumentException, IOException;

  public abstract Set<Namespace> getNamespaces(BigInteger resultID) throws IOException;

  public abstract Stream<Triple> queryResult(String rdfFormat, BigInteger resultID) throws IOException;

  //TODO open and close repos to each query result

  protected final class AtomicBigInteger {

    private final AtomicReference<BigInteger> valueHolder = new AtomicReference<>();

    public AtomicBigInteger(BigInteger bigInteger) {
      valueHolder.set(bigInteger);
    }

    public BigInteger incrementAndGet() {
      for (; ; ) {
        BigInteger current = valueHolder.get();
        BigInteger next = current.add(BigInteger.ONE);
        if (valueHolder.compareAndSet(current, next)) {
          return next;
        }
      }
    }
  }
}