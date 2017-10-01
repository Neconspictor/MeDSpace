package de.unipassau.medspace.register;

import de.unipassau.medspace.register.common.Datasource;

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
   * A map, that assigns each registered datasource a timestamp,
   * which records he time the datasource was added to the register.
   */
  private final Map<Datasource, Timestamp> datasources;

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
   * Creates a new Register.
   */
  public Register() {
    this.datasources = new TreeMap<>();
    this.readWriteLock = new ReentrantReadWriteLock();
  }

  /**
   * Adds a Datasource to this object.
   * @param newValue The datasource to add to this register.
   * @return A response object, that reports about what was done.
   */
  public Results.Add addDatasource(Datasource newValue) {

    if (newValue == null) {
      return Results.Add.NULL_NOT_VALID;
    }

    Timestamp newUpdate = new Timestamp(System.currentTimeMillis());

    Lock lock = readWriteLock.writeLock();
    try {
      lock.lock();
      Timestamp old = datasources.get(newValue);

      // Datasource wasn't registered before?
      if (old == null) {
        datasources.put(newValue, newUpdate);
        return Results.Add.SUCCESS;
      }

      // only update if no newer update exists
      if (old.before(newUpdate)) {

        // Datasource objects are considered equal, if their URLs are equal.
        // As a side effect, other information like the services member are not considered.
        // Generally this is an intended side effect for that class.
        // But in this case, Map.put don't replaces an existing key, only its value!
        // Thus, removing the datasource first is necessary, as the old value could contain outdated information
        // and should be removed.
        datasources.remove(newValue);

        datasources.put(newValue, newUpdate);
        return  Results.Add.SUCCESS;
      }
      return  Results.Add.NO_SUCCESS_NEWER_VERSION_EXISTS;

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
  public Results.NoResponse datasourceNoRespond(Datasource datasource) {
    if (datasource == null) return Results.NoResponse.NULL_NOT_VALID;
    // For now just remove the datasource
    Lock lock = readWriteLock.readLock();
    Timestamp timestamp;
    try {
      lock.lock();
      timestamp = datasources.get(datasource);
    } finally {
      lock.unlock();
    }

    // datasource isn't registered?
    if (timestamp == null) return Results.NoResponse.DATASOURCE_NOT_FOUND;

    Timestamp cooldownTime  = new Timestamp(timestamp.getTime() + COOL_DOWN_TIME);
    Timestamp currentTime = new Timestamp(System.currentTimeMillis());

    // Cooldown still active?
    if (currentTime.before(cooldownTime)) {
      return Results.NoResponse.COOL_DOWN_ACTIVE;
    }
    removeDatasource(datasource);
    return Results.NoResponse.REMOVED_DATASOURCE;
  }

  /**
   * Provides a copy of the datasources along with their time stamps on that they were added to the register.
   * @return A copy of the datasources the register contains.
   */
  public Map<Datasource, Timestamp> getDatasources() {
    Lock lock = readWriteLock.readLock();
    try {
      lock.lock();

      //Create a shallow copy, as this object has to remain thread-safe
      // NOTE: A wrapper class like Collections.unmodifiableList isn't enough
      // as write operations to datasources would also change the content of the shallow copy!
      // As Datasource is an immutable class, no copying has to be done.
      Map<Datasource, Timestamp> copy = new TreeMap<>(datasources);
      return copy;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Removes a given Datasource from the register.
   * @param datasource The Datasource that should be removed.
   * @return A response object, that reports about what was done.
   */
  public Results.Remove removeDatasource(Datasource datasource) {

    if (datasource == null) return Results.Remove.NULL_NOT_VALID;

    Lock lock = readWriteLock.writeLock();
    try {
      lock.lock();
      if (datasources.get(datasource) != null) {
        datasources.remove(datasource);
        return Results.Remove.SUCCESS;
      }
      return Results.Remove.DATASOURCE_NOT_FOUND;
    } finally {
      lock.unlock();
    }
  }
}