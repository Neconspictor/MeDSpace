package de.unipassau.medspace.register;

import de.unipassau.medspace.register.common.Datasource;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by David Goeth on 24.09.2017.
 */
public class BlockingRegister {

  private final Map<String, Datasource> datasources;
  private final ReadWriteLock readWriteLock;
  private static final int COOL_DOWN_TIME = 10000;

  public BlockingRegister() {
    this.datasources = new TreeMap<>();
    this.readWriteLock = new ReentrantReadWriteLock();
  }

  public Results.Add addDatasource(Datasource.MutableDatasource mutable) {

    if (mutable == null) {
      return Results.Add.NULL_NOT_VALID;
    }

    Lock lock = readWriteLock.writeLock();
    Datasource newValue = Datasource.createFromMutable(mutable);
    try {
      lock.lock();
      Datasource oldValue = datasources.get(newValue.getUrl().toExternalForm());
      if (oldValue == null) {
        datasources.put(newValue.getUrl().toExternalForm(), newValue);
        return Results.Add.SUCCESS;
      }

      if (oldValue.getTimeStamp().before(newValue.getTimeStamp())) {
        datasources.put(newValue.getUrl().toExternalForm(), newValue);
        return  Results.Add.SUCCESS;
      }
      return  Results.Add.NO_SUCCESS_NEWER_VERSION_EXISTS;

    } finally {
      lock.unlock();
    }
  }

  public Results.NoResponse datasourceNoRespond(Datasource datasource) {
    if (datasource == null) return Results.NoResponse.NULL_NOT_VALID;
    // For now just remove the datasource
    Lock lock = readWriteLock.readLock();
    Datasource toDelete;
    try {
      lock.lock();
      toDelete = datasources.get(datasource.getUrl().toExternalForm());
    } finally {
      lock.unlock();
    }

    // datasource isn't registered?
    if (toDelete == null) return Results.NoResponse.DATASOURCE_NOT_FOUND;

    long datasourceMillis = datasource.getTimeStamp().getTime();
    long toDeleteMillis = toDelete.getTimeStamp().getTime();
    if (datasourceMillis > toDeleteMillis + COOL_DOWN_TIME) {
      removeDatasource(datasource);
      return Results.NoResponse.REMOVED_DATASOURCE;
    }
    return Results.NoResponse.COOL_DOWN_ACTIVE;
  }

  public Map<String, Datasource> getDatasources() {
    Lock lock = readWriteLock.readLock();
    try {
      lock.lock();

      //Create a shallow copy, as this object has to remain thread-safe
      // NOTE: A wrapper class like Collections.unmodifiableList isn't enough
      // as write operations to datasources would also change the content of the shallow copy!
      // As Datasource is an immutable class, no copying has to be done.
      Map<String, Datasource> copy = new TreeMap<>(datasources);
      return copy;
    } finally {
      lock.unlock();
    }
  }

  public Results.Remove removeDatasource(Datasource datasource) {

    if (datasource == null) return Results.Remove.NULL_NOT_VALID;

    Lock lock = readWriteLock.writeLock();
    try {
      lock.lock();
      if (datasources.get(datasource.getUrl().toExternalForm()) != null) {
        datasources.remove(datasource.getUrl().toExternalForm());
        return Results.Remove.SUCCESS;
      }
      return Results.Remove.DATASOURCE_NOT_FOUND;
    } finally {
      lock.unlock();
    }
  }

}