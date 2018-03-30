package de.unipassau.medspace.common.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A lock for a counter.
 */
public class CounterLock {

  private ReentrantLock lock;

  private long counter;

  private Condition condition;

  /**
   * Creates a new CounterLock object.
   */
  public CounterLock() {
    lock = new ReentrantLock();
    condition = lock.newCondition();
    counter = 0;
  }

  /**
   * Decrrements the lock counter.
   */
  public void decrementLock() {
    decrementLock(null);
  }

  /**
   * Decrements the lock and executes a callback method if the counter has reached zero.
   */
  public void decrementLock(Runnable onUnlockCallback) {
    try {
      lock.lock();
      if (counter <= 0) throw new IllegalStateException("counter is already zero!");
      --counter;
      if (counter == 0) {
        if (onUnlockCallback != null)
          onUnlockCallback.run();
        condition.signal();
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Increments the lock counter.
   */
  public void incrementLock() {
    try{
      lock.lock();
      ++counter;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Provides the counter.
   * @return the counter.
   */
  public long getCounter() {
    try {
      lock.lock();
      return counter;
    } finally {
      lock.unlock();
    }
  }

  /**
   * The current thread is suspended until the lock is unlocked.
   * @throws InterruptedException If the thread is interrupted while waiting for the unlock event.
   */
  public void waitTillUnlocked() {
    try {
      lock.lock();

      //early return if no lock is held...
      if (counter == 0) return;

      // await signal...
      try {
        condition.await();
      } catch (InterruptedException e) {
        throw new IllegalMonitorStateException("Interruption while awaiting unlocking!");
      }
    } finally {
      lock.unlock();
    }
  }
}