package de.unipassau.medspace.register;

import de.unipassau.medspace.common.register.Datasource;
import de.unipassau.medspace.common.register.DatasourceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * In a dataspace the register is responsible for managing the datasources.
 * Thus, the register allows to add and remove datasources. If a datasource doesn't respond anymore, it is possible to
 * inform the register. The register then decides if it keeps the not responding datasource or removes it.
 * <br>
 * <strong>NOTE:</strong> This class is threadsafe.
 */
public class Register {

  /**
   * Logger instance for this class.
   */
  private static final Logger log = LoggerFactory.getLogger(RegisterLifecycle.class);

  /**
   * A map, that assigns each registered datasource a timestamp,
   * which records he time the datasource was added to the register.
   */
  private final Map<Datasource, DatasourceState> datasources;

  private boolean isClosed = false;

  /**
   * Used to allow multiple readers to access the register's methods non-blocking.
   * Only write processes should lock other writers and readers.
   */
  private final ReadWriteLock readWriteLock;

  /**
   * A constant that specifies how long (in milliseconds) a newly added or updated datasource should not be removed
   * if the method {@link #datasourceNoRespond} is called.
   * <br>
   * <strong>Explanation:</strong>
   * The reason is the issue, when a datasource didn't respond for a time (e.g. due to network issues).
   * After that period it registers itself (so the datasource is updated in the register) but
   * shortly after that, it could happen, that another thread informs the register that the datasource doesn't respond
   * (which in fact is an outdated information). This (multithreading) issue cannot be prevented if multiple threads
   * are used (unless the threads are synchronized which would slow down performance...).
   * But through a 'Cool down' phase such outdated actions can be reduced if not even completely excluded.
   */
  private static final int COOL_DOWN_TIME = 10000;

  /**
   * TODO
   */
  private static final int IO_ERROR_LIMIT = 5;

  /**
   * Creates a new Register.
   */
  public Register(Map<Datasource, DatasourceState> datasources) {
    if (datasources == null) datasources = new HashMap<>();
    this.datasources = new TreeMap<>(datasources);
    this.readWriteLock = new ReentrantReadWriteLock();
  }

  /**
   * Adds a Datasource to this object.
   * @param datasource The datasource to add to this register.
   * @return true if the datasource was successfully added; false otherwise.
   */
  public boolean addDatasource(Datasource datasource) {

    if (datasource == null) throw new IllegalArgumentException("datasource mustn't be null!");

    Timestamp newUpdate = new Timestamp(System.currentTimeMillis());

    Lock lock = readWriteLock.writeLock();
    try {
      lock.lock();
      if (isClosed) return false;
      DatasourceState old = datasources.get(datasource);

      // Datasource wasn't registered before?
      if (old == null) {
        datasources.put(datasource, new DatasourceState(newUpdate));
        return true;
      }

      // only update if no newer update exists
      if (old.getTimestamp().before(newUpdate)) {

        // Datasource objects are considered equal, if their URLs are equal.
        // As a side effect, other information like the services member are not considered.
        // Generally this is an intended side effect for that class.
        // But in this case, Map.put don't replaces an existing key, only its value!
        // Thus, removing the datasource first is necessary, as the old value could contain outdated information
        // and should be removed.
        datasources.remove(datasource);

        datasources.put(datasource, new DatasourceState(newUpdate,
                                                        old.getIoErrors()));
        return  true;
      }

      //The Datasource was updated and is newer; No replacing should be performed!
      return  false;

    } finally {
      lock.unlock();
    }
  }

  public void datasourceIOError(Datasource datasource) {
    if (datasource == null) throw new IllegalArgumentException("datasource mustn't be null!");
    if (isClosed) return;

    // increase the io error counter
    Lock lock = readWriteLock.writeLock();

    try {
      lock.lock();
      DatasourceState state = datasources.get(datasource);

      // Datasource isn't registered?
      if (state == null) return;

      state = state.createIncrement();
      datasources.put(datasource, state);

      // If the io error count exceeds a limit remove the data source if its cool down isn't active
      if (state.getIoErrors() > IO_ERROR_LIMIT ) {
        datasourceNoRespond(datasource);
      }

    } finally {
      lock.unlock();
    }

  }

  /**
   * Informs the register, that a given Datasource isn't responding anymore.
   * The register decides if it keeps the Datasource or removes it. If the Datasource was added or updated recently,
   * its Cool Down Phase could be still active. Than the Datasource will be kept. Otherwise it is removed.
   *
   * @param datasource The datasource that isn't responding anymore.
   * @return A response object, that reports about what was done.
   */
  public NoResponse datasourceNoRespond(Datasource datasource) {
    if (datasource == null) throw new IllegalArgumentException("datasource mustn't be null!");
    // For now just remove the datasource
    Lock lock = readWriteLock.readLock();
    Timestamp timestamp = null;
    try {
      lock.lock();
      if (isClosed) return NoResponse.DATASOURCE_NOT_FOUND;
      DatasourceState state = datasources.get(datasource);
      if (state != null) timestamp = state.getTimestamp();
    } finally {
      lock.unlock();
    }

    // datasource isn't registered?
    if (timestamp == null) return NoResponse.DATASOURCE_NOT_FOUND;

    Timestamp cooldownTime  = new Timestamp(timestamp.getTime() + COOL_DOWN_TIME);
    Timestamp currentTime = new Timestamp(System.currentTimeMillis());

    // Cooldown still active?
    if (currentTime.before(cooldownTime)) {
      return NoResponse.COOL_DOWN_ACTIVE;
    }
    removeDatasource(datasource);
    return NoResponse.REMOVED_DATASOURCE;
  }

  /**
   * Provides a copy of the datasources along with their time stamps on that they were added to the register.
   * @return A copy of the datasources the register contains.
   */
  public Map<Datasource, DatasourceState> getDatasources() {
    Lock lock = readWriteLock.readLock();
    try {
      lock.lock();

      //Create a shallow copy, as this object has to remain thread-safe
      // NOTE: A wrapper class like Collections.unmodifiableList isn't enough
      // as write operations to datasources would also change the content of the shallow copy!
      // As Datasource is an immutable class, no copying has to be done.
      Map<Datasource, DatasourceState> copy = new TreeMap<>(datasources);
      return copy;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Removes a given Datasource from the register.
   * @param datasource The Datasource that should be removed.
   * @return true if the datasource could be removed or false if the datasource wasn't found.
   */
  public boolean removeDatasource(Datasource datasource) {
    if (datasource == null) throw new IllegalArgumentException("datasource mustn't be null!");

    Lock lock = readWriteLock.writeLock();
    try {
      lock.lock();
      if (isClosed) return false;
      if (datasources.get(datasource) != null) {
        datasources.remove(datasource);
        return true;
      }

      // Datasource isn't registered and thus couldn't be removed.
      return false;
    } finally {
      lock.unlock();
    }
  }

  /**
   * TODO
   */
  public boolean removeAllDatasources() {

    if (isClosed) return false;

    Lock lock = readWriteLock.writeLock();
    try{
      lock.lock();
      datasources.clear();
      return true;
    } finally {
      lock.unlock();
    }
  }

  public void close() {
    if (isClosed) {
      log.warn("Register already closed.");
      return;
    }
    Lock lock = readWriteLock.writeLock();
    try {
      lock.lock();
      isClosed = true;
    } finally {
      lock.unlock();
    }
  }

  /**
   * An enum that is used to store the result of {@link #datasourceNoRespond(Datasource)}
   */
  public enum NoResponse {
    REMOVED_DATASOURCE("REMOVED_DATASOURCE"),
    COOL_DOWN_ACTIVE("COOL_DOWN_ACTIVE"),
    DATASOURCE_NOT_FOUND("DATASOURCE_NOT_FOUND");

    /**
     * String representation of the result.
     */
    private final String name;

    /**
     * Creates a new NoResponse
     * @param name A string description of the result of a {@link #datasourceNoRespond(Datasource)} call
     */
    NoResponse(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return  name;
    }
  }
}