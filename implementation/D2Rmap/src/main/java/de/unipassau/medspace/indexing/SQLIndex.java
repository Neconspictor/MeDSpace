package de.unipassau.medspace.indexing;

import de.fuberlin.wiwiss.d2r.exception.D2RException;
import de.unipassau.medspace.util.FileUtil;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by David Goeth on 13.06.2017.
 */
public class SQLIndex implements Closeable {
  private Path indexDirectoryPath;
  private FSDirectory index;
  private volatile boolean isOpen;

  private static Logger log = Logger.getLogger(SQLIndex.class);

  protected SQLIndex(Path directory) {
    indexDirectoryPath = directory;
    index = null;
    isOpen = false;
  }

  public static SQLIndex create(String directory) throws IOException {
    Path path = null;
    try {
      path = FileUtil.createDirectory(directory);
    } catch (IOException e) {
      throw new IOException("Couldn't create index directory");
    }
    SQLIndex result = new SQLIndex(path);

    return result;
  }

  public void clearIndex() throws D2RException {
    if (!isOpen) throw new D2RException("Indexer is not open!");
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

  public IndexReader createReader() throws IOException {
    return DirectoryReader.open(index);
  }

  public void index(Iterable<Document> data) throws IOException {
    StandardAnalyzer analyzer = new StandardAnalyzer();
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.APPEND);

    try(IndexWriter w = new IndexWriter(index, config)) {
      w.addDocuments(data);
      /*for (Document doc : data) { // TODO test performance and memory consumption!!!
        w.addDocument(doc);
        w.flush();
      }*/
      w.flush();
      w.commit();
    }
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

  /**
   * Clears the index and indexes the sql data.
   */
  public void reindex(Iterable<Document> data) throws D2RException, IOException {
    if (!isOpen) throw new D2RException("Indexer is not open!");
    clearIndex();
    index(data);
  }
}