package de.unipassau.medspace.data_collector.rdf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by David Goeth on 1/28/2018.
 */
public class CounterLock {

  private ReentrantLock lock;
  private long counter;
  private Condition condition;

  public CounterLock() {
    lock = new ReentrantLock();
    condition = lock.newCondition();
    counter = 0;
  }

  public void decrementLock() {
    try {
      lock.lock();
      if (counter <= 0) throw new IllegalStateException("counter is 0");
      --counter;
      if (counter == 0) {
        condition.signal();
      }
    } finally {
      lock.unlock();
    }
  }

  public void incrementLock() {
    try{
      lock.lock();
      ++counter;
    } finally {
      lock.unlock();
    }
  }

  public long getCounter() {
    try {
      lock.lock();
      return counter;
    } finally {
      lock.unlock();
    }
  }

  public void waitTillUnlocked() throws InterruptedException {
    try {
      lock.lock();
      condition.await();
    } finally {
      lock.unlock();
    }
  }
}