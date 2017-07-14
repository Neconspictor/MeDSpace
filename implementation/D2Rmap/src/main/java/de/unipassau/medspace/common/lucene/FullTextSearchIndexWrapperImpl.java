package de.unipassau.medspace.common.lucene;

import de.unipassau.medspace.common.indexing.FullTextSearchIndexWrapper;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.util.FileUtil;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by David Goeth on 07.07.2017.
 */
public class FullTextSearchIndexWrapperImpl implements FullTextSearchIndexWrapper<Document> {

  private List<String> fields;
  private Path indexDirectoryPath;
  private FSDirectory index;
  private volatile boolean isOpen;

  private static Logger log = Logger.getLogger(FullTextSearchIndexWrapper.class);

  protected FullTextSearchIndexWrapperImpl(Path directory) {
    indexDirectoryPath = directory;
    index = null;
    isOpen = false;
  }

  public static FullTextSearchIndexWrapperImpl create(String directory) throws IOException {
    Path path = null;
    try {
      path = FileUtil.createDirectory(directory);
    } catch (IOException e) {
      throw new IOException("Couldn't create index directory");
    }
    FullTextSearchIndexWrapperImpl result = new FullTextSearchIndexWrapperImpl(path);

    return result;
  }

  public void clearIndex() throws IOException {
    if (!isOpen) throw new IOException("Indexer is not open!");
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

  @Override
  public KeywordSearcher<Document> createKeywordSearcher() throws IOException {
    return new LuceneKeywordSearcher(fields, this);
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

  public List<String> getSearchableFields() {
    return fields;
  }

  public void index(Iterable<Document> data) throws IOException {
    StandardAnalyzer analyzer = new StandardAnalyzer();
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.APPEND);

    try(IndexWriter w = new IndexWriter(index, config)) {
      w.addDocuments(data);
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
  public void reindex(Iterable<Document> data) throws IOException {
    if (!isOpen) throw new IOException("Indexer is not open!");
    clearIndex();
    index(data);
  }

  public void setSearchableFields(List<String> fields) {
    this.fields = fields;
  }
}