package de.unipassau.medspace.data_collector.rdf4j;

import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO
 */
public class LocalTestRepositoryManager {

  /**
   * TODO
   */
  private File root;

  private Map<String, Repository> createdRepos;

  /**
   * TODO
   * @param rootDir
   * @throws IOException
   */
  public LocalTestRepositoryManager(String rootDir) throws IOException {
    root = createAndCleanDirectory(rootDir);
    this.createdRepos = new ConcurrentHashMap<>();
  }

  public Repository open(String subPath) {
    Repository repo = createdRepos.get(subPath);
    if (repo == null) return null;
    if (!repo.isInitialized()) {
      repo.initialize();
    }

    return repo;
  }

  public void close(String subPath) {
    Repository repo = createdRepos.get(subPath);
    if (repo == null) return;
    if (repo.isInitialized()) {
      repo.shutDown();
    }


    if (createdRepos.size() > 10) {
      int size = createdRepos.size() - 10;
      Iterator<Map.Entry<String, Repository>> it = createdRepos.entrySet().iterator();
      while(it.hasNext()) {
        Map.Entry<String, Repository> current = it.next();
        if (!current.getValue().isInitialized()) {
          try {
            it.remove();
            remove(current.getKey());
            //--size;
          } catch (IOException e) {
          }
        }
      }
    }
  }


  /**
   * TODO
   * @param subPath
   * @return
   * @throws IOException
   */
  public Repository get(String subPath) throws IOException {
    return createdRepos.get(subPath);
  }

  public void create(String subPath) throws IOException {

    File subFolder = getSubFolder(root, subPath);
    if (subFolder == null) {
      subFolder = createSubFolder(root, subPath);
    }

    Repository repo =  createRepository(subFolder);
    createdRepos.put(subPath, repo);
  }

  /**
   * TODO
   * @param subPath
   * @throws IOException
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


  /**
   * TODO
   * @param dataDir
   * @return
   */
  private Repository createRepository(File dataDir) {
    Repository repo = new SailRepository(new NativeStore(dataDir));
    return repo;
  }

  /**
   * TODO
   * @param path
   * @return
   * @throws IOException
   */
  private File createAndCleanDirectory(String path) throws IOException {
    File directory  = new File(path);
    directory.mkdirs();
    if (!directory.isDirectory()) {
      throw new IOException("Couldn't create directory: " + directory.getPath());
    }

    FileUtils.cleanDirectory(directory);
    return directory;
  }

  /**
   * TODO
   * @param root
   * @param name
   * @return
   */
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

  /**
   * TODO
   * @param rootDir
   * @param name
   * @return
   * @throws IOException
   */
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