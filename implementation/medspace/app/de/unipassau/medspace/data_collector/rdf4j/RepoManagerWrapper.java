package de.unipassau.medspace.data_collector.rdf4j;

import de.unipassau.medspace.common.util.CounterLock;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO
 */
public class RepoManagerWrapper {

  /**
   * TODO
   */
  private LocalRepositoryManager manager;

  /**
   * TODO
   */
  private ConcurrentHashMap<String, CounterLock> repoLocks;

  /**
   * TODO
   */
  private Lock createLock;

  /**
   * TODO
   * @param repositoryManager
   */
  public RepoManagerWrapper(LocalRepositoryManager repositoryManager) {
    manager = repositoryManager;
    repoLocks = new ConcurrentHashMap<>();
    createLock = new ReentrantLock();
  }

  /**
   * TODO
   * @param id
   */
  public void createRepository(String id) throws IOException {

    //no create method should be called!
    createLock.lock();

    try{
      // each repo id is only one time allowed to be created!
      if (repoLocks.get(id) != null) {
        throw new IllegalArgumentException("Tried to create a repo id multiple times: " + id);
      }

      manager.create(id);

      repoLocks.put(id, new CounterLock());

    } finally {
      createLock.unlock();
    }
  }

  /**
   * TODO
   * @param id
   * @return
   * @throws IOException
   */
  public RepositoryConnection getConnection(String id) throws IOException {
    CounterLock repoLock = repoLocks.get(id);
    if (repoLock == null) {
      throw new IOException("Repo not found: " + id);
    }

    Runnable onUnlockCallback = ()->closeRepository(id);
    Runnable callback = ()->releaseReadWriteAccess(id, onUnlockCallback);

    RepositoryConnection conn;

    try {
      //the repo lock has to be acquired when the repoLocks lock is still active!
      repoLock.incrementLock();


      //Now the repo counter lock is active; calls to  createRepository and removeRepository which
      //don't affect the repo assigned to this counter lock, can proceed, the others are blocked!

      //get should not block other get calls, unless they are called with the same id!
      Repository repo = manager.open(id);
      conn = repo.getConnection();

    } catch (RepositoryException | IOException e) {
      callback.run();
      throw new IOException("Couldn't get connection to repository with id = " + id + "; Cause:", e);
    }

    return new ConnectionForwarder(conn, callback);
  }


  /**
   * TODO
   * @param id
   * @throws InterruptedException
   * @throws IOException
   */
  public void removeRepository(String id) throws InterruptedException, IOException {

    //get lock for repo and unregister the repo lock
    CounterLock repoLock = repoLocks.remove(id);
    if (repoLock == null) {
      throw new IllegalArgumentException("No lock found for repo id: " + id);
    }

    //now the repo lock isn't accessible anymore for others!

    //wait till the repo lock count is zero
    repoLock.waitTillUnlocked();

    //remove should only block get if it is called with the same id!
    manager.remove(id);
  }

  /**
   * TODO
   * @param id
   * @throws InterruptedException
   */
  private void closeRepository(String id) {

    //get lock for the repo
    CounterLock repoLock = repoLocks.get(id);
    if (repoLock == null) {
      throw new IllegalArgumentException("No lock found for repo id: " + id);
    }

    //wait till the repo lock count is zero
    repoLock.waitTillUnlocked();

    //close should only block get if it is called with the same id!
    manager.close(id);
  }

  /**
   * TODO this function is only allowed to be called from a Repository -/Connection Wrapper
   * @param id
   */
  private void releaseReadWriteAccess(String id, Runnable onUnlock) {
    CounterLock repoLock = repoLocks.get(id);
    if (repoLock == null) {
      throw new IllegalArgumentException("No lock found for repo id: " + id);
    }

    //the repo lock has to be acquired when the repoLocks lock is still active!
    repoLock.decrementLock(onUnlock);
  }
}