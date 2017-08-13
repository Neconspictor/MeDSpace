package de.unipassau.medspace.common.lucene;

import de.unipassau.medspace.common.indexing.Index;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.FileUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * {@link LuceneIndex} is an {@link Index} for the lucene search engine.
 */
public class LuceneIndex implements Index<Document> {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(Index.class);

  /**
   * Specifies the directory to store indexed data files.
   */
  private Path indexDirectoryPath;

  /**
   * The lucene file (stored on the hard disk) index.
   */
  private FSDirectory index;

  /**
   * Flag to specify if the index is currently open.
   */
  private volatile boolean isOpen;

  /**
   * An analyzer builder to retrieve an {@link Analyzer}.
   * The analyzer is used to analyze the data before indexing it.
   * E.g. stop words can be defined by the analyzer.
   */
  private AnalyzerBuilder builder;

  /**
   * Creates a new lucene index.
   * @param directory Specifies the index directory for storing indexed data.
   * @param builder Used to build an analyzer for data to be indexed.
   */
  protected LuceneIndex(Path directory, AnalyzerBuilder builder) {
    indexDirectoryPath = directory;
    this.builder = builder;
    index = null;
    isOpen = false;
  }

  /**
   * Creates a new lucene index.
   * @param directory Specifies the index directory for storing indexed data.
   * @param builder Used to build an analyzer for data to be indexed.
   * @return a new lucene index
   * @throws IOException if directory doesn't specify a valid directory
   */
  public static LuceneIndex create(String directory, AnalyzerBuilder builder) throws IOException {
    Path path = null;
    try {
      path = FileUtil.createDirectory(directory);
    } catch (IOException e) {
      throw new IOException("Couldn't create index directory", e);
    }
    LuceneIndex result = new LuceneIndex(path, builder);

    return result;
  }

  @Override
  public void clearIndex() throws IOException {
    if (!isOpen) throw new IOException("Indexer is not open!");
    Analyzer analyzer = builder.build();
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    IndexWriter w = null;

    config.setCommitOnClose(true);

    try {
      w = new IndexWriter(index, config);
      w.deleteAll();
      w.flush();
      w.commit();
    } catch (IOException e) {
      log.error("Error while clearing the index", e);
    } finally {
      FileUtil.closeSilently(w, true);
    }
  }

  @Override
  public boolean exists() {
    try {
      return hasIndexedData();
    } catch (IOException e) {
      log.error("Error while trying to query indexed data: ", e);
      return false;
    }
  }

  @Override
  public void close() {
    if (!isOpen) return;

    FileUtil.closeSilently(index, true);
    index = null;
    isOpen = false;
  }

  /**
   * Creates a new {@link IndexReader}.
   * @return A reader alloing to query this index
   * @throws IOException Will be thrown if the FSDirectory couldn't be opened, doesn't exists or another low level I/O
   * Error occurs
   * IMPORTANT: The resulting IndexReader has to be closed, if not needed anymore!!!
   */
  public IndexReader createReader() throws IOException {
    return DirectoryReader.open(index);
  }

  @Override
  public boolean hasIndexedData() throws IOException {
    if (!isOpen) throw new IOException("Index has to be successfully opened before calling this function!");

    try {
      if (!DirectoryReader.indexExists(index)) {
          // early exit as trying to createDoc an index reader of a non existing
          // index Directory would rise an IOException
          return false;
      }

      try(IndexReader reader = createReader()) {
        return reader.numDocs() > 0;
      }

    } catch (IOException e) {
      log.error("Error while retrieving document count: ", e);
      throw new IOException("Couldn't query indexed data");
    }
  }

  @Override
  public void index(Stream<Document> data) throws IOException {
    Analyzer analyzer = builder.build();
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    config.setCommitOnClose(true);

    try(IndexWriter w = new IndexWriter(index, config)) {
      while (data.hasNext()) {
        Document doc = data.next();
        w.addDocument(doc);
        w.flush();
        w.commit();
      }
      w.flush();
      w.commit();
    }
  }

  @Override
  public boolean isOpen() {
    return isOpen;
  }

  @Override
  public void open() throws IOException {
    assert indexDirectoryPath != null;
    close();
    try {
      index = FSDirectory.open(indexDirectoryPath);
    } catch (IOException e) {
      throw new IOException("Couldn't open index in directory path: " + indexDirectoryPath, e);
    }

    isOpen = true;
  }

  @Override
  public void reindex(Stream<Document> data) throws IOException {
    if (!isOpen) throw new IOException("Indexer is not open!");
    clearIndex();
    index(data);
  }
}