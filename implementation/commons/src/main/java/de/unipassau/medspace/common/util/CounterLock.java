package de.unipassau.medspace.common.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO
 */
public class CounterLock {

  /**
   * TODO
   */
  private ReentrantLock lock;

  /**
   * TODO
   */
  private long counter;

  /**
   * TODO
   */
  private Condition condition;

  /**
   * TODO
   */
  public CounterLock() {
    lock = new ReentrantLock();
    condition = lock.newCondition();
    counter = 0;
  }

  /**
   * TODO
   */
  public void decrementLock() {
    decrementLock(null);
  }

  /**
   * TODO
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
   * TODO
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
   * TODO
   * @return
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
   * TODO
   * @throws InterruptedException
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