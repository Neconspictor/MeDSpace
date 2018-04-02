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
 * A wrapper for a local repository manager.
 * This class provides synchronized access to the repository manager.
 */
public class RepoManagerWrapper {

  private LocalRepositoryManager manager;

  private ConcurrentHashMap<String, CounterLock> repoLocks;

  private Lock createLock;


  /**
   * Creates a new RepoManagerWrapper object.
   * @param repositoryManager The repository manager.
   */
  public RepoManagerWrapper(LocalRepositoryManager repositoryManager) {
    manager = repositoryManager;
    repoLocks = new ConcurrentHashMap<>();
    createLock = new ReentrantLock();
  }

  /**
   * Creates a new repository.
   * @param id The id for the new repository.
   * @throws IOException If an IO error occurs.
   * @throws IllegalArgumentException If the id is already assigned to another repository.
   */
  public void createRepository(String id) throws IOException, IllegalArgumentException {

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
   * Provides a connection to a repository.
   * @param id The id of the repsoitory.
   * @return a connection to the repository.
   * @throws IOException If an IO error occurs.
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
   * Deletes a repository.
   *
   * @param id The id of the repsoitory.
   * @throws IOException If an IO error occurs.
   * @throws IllegalArgumentException If the id cannot be matched to a repository.
   */
  public void removeRepository(String id) throws IOException, IllegalArgumentException {

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


  private void releaseReadWriteAccess(String id, Runnable onUnlock) {
    CounterLock repoLock = repoLocks.get(id);
    if (repoLock == null) {
      throw new IllegalArgumentException("No lock found for repo id: " + id);
    }

    //the repo lock has to be acquired when the repoLocks lock is still active!
    repoLock.decrementLock(onUnlock);
  }
}