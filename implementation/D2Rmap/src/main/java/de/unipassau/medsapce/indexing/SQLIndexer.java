package de.unipassau.medsapce.indexing;

import de.fuberlin.wiwiss.d2r.exception.D2RException;
import de.unipassau.medspace.util.FileUtil;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by David Goeth on 13.06.2017.
 */
public class SQLIndexer implements Closeable {
  private Path indexDirectoryPath;
  private FSDirectory index;
  private volatile boolean isOpen;

  private static Logger log = Logger.getLogger(SQLIndexer.class);

  protected SQLIndexer(Path directory) {
    indexDirectoryPath = directory;
    index = null;
    isOpen = false;
  }

  public static SQLIndexer create(String directory) throws IOException {
    Path path = null;
    try {
      path = FileUtil.createDirectory(directory);
    } catch (IOException e) {
      throw new IOException("Couldn't create index directory");
    }
    SQLIndexer result = new SQLIndexer(path);

    return result;
  }

  public void clearIndex() throws D2RException {
    if (!isOpen) throw new D2RException("Indexer is already closed!");
    StandardAnalyzer analyzer = new StandardAnalyzer();
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    IndexWriter w = null;

    try {
      w = new IndexWriter(index, config);
      w.deleteAll();
    } catch (IOException e) {
      log.error(e);
    } finally {
      FileUtil.closeSilently(w, true);
    }
  }

  public void close() {
    if (!isOpen) return;

    FileUtil.closeSilently(index, true);
    index = null;
    isOpen = false;
  }

  /**
   * Clears the index and indexes the sql data.
   */
  public void reindex() throws D2RException {
    if (!isOpen) throw new D2RException("Indexer is not open!");
    clearIndex();
  }

  public Directory getIndex() {
    if (!isOpen) return null;
    return index;
  }

  public boolean isOpen() {
    return isOpen;
  }

  public void open() throws IOException {
    assert indexDirectoryPath != null;
    close();
    try {
      index = FSDirectory.open(indexDirectoryPath);
    } catch (IOException e) {
      log.error(e);
      throw new IOException("Couldn't open index in directory: " + indexDirectoryPath);
    }

    isOpen = true;
  }
}