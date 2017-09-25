package de.unipassau.medspace.register;

import de.unipassau.medspace.register.common.Datasource;

import java.util.Collections;
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

  public BlockingRegister() {
    this.datasources = new TreeMap<>();
    this.readWriteLock = new ReentrantReadWriteLock();
  }

  public boolean addDatasource(Datasource datasource) throws InterruptedException {
    Lock lock = readWriteLock.writeLock();
    try {
      lock.lock();
      if (datasources.get(datasource.getUri()) == null) {
        datasources.put(datasource.getUri(), datasource);
        return true;
      }
      return false;
    } finally {
      lock.unlock();
    }
  }

  public void datasourceNoRespond(Datasource datasource) {
    // For now just remove the datasource
    removeDatasource(datasource);
  }

  public Map<String, Datasource> getDatasources() {
    Lock lock = readWriteLock.readLock();
    try {
      lock.lock();
      return Collections.unmodifiableMap(datasources);
    } finally {
      lock.unlock();
    }
  }

  public boolean removeDatasource(Datasource datasource) {
    Lock lock = readWriteLock.writeLock();
    try {
      lock.lock();
      if (datasources.get(datasource.getUri()) != null) {
        datasources.remove(datasource.getUri());
        return true;
      }
      return false;
    } finally {
      lock.unlock();
    }
  }
}