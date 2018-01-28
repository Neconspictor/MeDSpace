package de.unipassau.medspace.data_collector.rdf4j;

import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Created by David Goeth on 1/28/2018.
 */
public class RepoManagerWrapper {

  private LocalTestRepositoryManager repositoryManager;
  Map<String, CounterLock> repoLocks;
  Lock repoLocksLock;
  Lock createRemoveLock;

}
