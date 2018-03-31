package de.unipassau.medspace.data_collector.rdf4j;

import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A manager fpr repositories that are stored locally on disk.
 */
public class LocalRepositoryManager {

  private File root;
  private ConcurrentHashMap<String, Repository> createdRepos;

  /**
   * Creates a new LocalRepositoryManager object.
   * @param rootDir The root folder for storing the repositories.
   * @throws IOException If an IO error occurs.
   */
  public LocalRepositoryManager(String rootDir) throws IOException {
    root = createAndCleanDirectory(rootDir);
    this.createdRepos = new ConcurrentHashMap<>();
  }
  
  /**
   * Opens a registered repository.
   * @param subPath The sub folder path the repository is stored.
   * @return The opened repository.
   * @throws IOException If an IO error occurs or if the repository doesn't exist.
   */
  public Repository open(String subPath) throws IOException {
    Repository repo = createdRepos.get(subPath);
    if (repo == null) throw new IOException("No repository with id " + subPath + " is registered!");
    if (!repo.isInitialized()) {
      repo.initialize();
    }

    return repo;
  }

  /**
   * Closes a repository.
   * @param subPath The path for the repository.
   */
  public void close(String subPath) {
    Repository repo = createdRepos.get(subPath);
    if (repo == null) return;
    if (repo.isInitialized()) {
      repo.shutDown();
    }
  }


  /**
   * Provides a repository.
   * @param subPath The sub folder where the repository is stored.
   * @return A repository identified by its sbu folder path.
   * @throws IOException
   */
  public Repository get(String subPath) throws IOException {
    return createdRepos.get(subPath);
  }

  /**
   * Creates a new repository.
   * @param subPath The sub folder path for the repository.
   * @throws IOException If an IO error occurs.
   */
  public void create(String subPath) throws IOException {

    File subFolder = getSubFolder(root, subPath);
    if (subFolder == null) {
      subFolder = createSubFolder(root, subPath);
    }

    Repository repo =  createRepository(subFolder);
    createdRepos.put(subPath, repo);
  }

  /**
   * Removes a repository.
   * @param subPath The sub folder path for the repository.
   * @throws IOException If an IO error occurs.
   */
  public void remove(String subPath) throws IOException {

    Repository repo = createdRepos.get(subPath);
    if (repo != null) {
      close(subPath);
    }


    File subFolder = getSubFolder(root, subPath);
    FileUtils.cleanDirectory(subFolder);
    if (!subFolder.delete()) {
      throw new IOException("Couldn't delete repo folder with id=" + subPath);
    }
  }


  private Repository createRepository(File dataDir) {
    Repository repo = new SailRepository(new NativeStore(dataDir));
    return repo;
  }

  private File createAndCleanDirectory(String path) throws IOException {
    File directory  = new File(path);
    directory.mkdirs();
    if (!directory.isDirectory()) {
      throw new IOException("Couldn't create directory: " + directory.getPath());
    }

    FileUtils.cleanDirectory(directory);
    return directory;
  }

  private File getSubFolder(File root, String name) {
    File target = new File(root + "/" + name);
    if (!target.isDirectory()) return null;

    // We have still to check, if the target folder is a subfolder of root!
    File parent = target.getParentFile();

    if (parent == null || parent.compareTo(root) != 0) {
      return null;
    }

    return target;
  }

  private File createSubFolder(File rootDir, String name) throws IOException {
    String path = rootDir.getPath() + "/" + name;
    File subFolder = new File(path);
    subFolder.mkdir();
    if (!subFolder.isDirectory()) {
      throw new IOException("Couldn't create subfolder; root: " + rootDir + "; subfolder name: " + name);
    }

    return subFolder;
  }
}