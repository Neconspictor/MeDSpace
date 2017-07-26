package de.unipassau.medspace.common.lucene;

import de.unipassau.medspace.common.indexing.FullTextSearchIndex;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.util.FileUtil;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import java.util.List;

/**
 * Created by David Goeth on 07.07.2017.
 */
public class FullTextSearchIndexImpl implements FullTextSearchIndex<Document> {

  private List<String> fields;
  private Path indexDirectoryPath;
  private FSDirectory index;
  private volatile boolean isOpen;

  private static Logger log = LoggerFactory.getLogger(FullTextSearchIndex.class);

  protected FullTextSearchIndexImpl(Path directory) {
    indexDirectoryPath = directory;
    index = null;
    isOpen = false;
  }

  public static FullTextSearchIndexImpl create(String directory) throws IOException {
    Path path = null;
    try {
      path = FileUtil.createDirectory(directory);
    } catch (IOException e) {
      throw new IOException("Couldn't create index directory");
    }
    FullTextSearchIndexImpl result = new FullTextSearchIndexImpl(path);

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
      log.error("Error while clearing the index", e);
    } finally {
      FileUtil.closeSilently(w, true);
    }
  }

  @Override
  public KeywordSearcher<Document> createKeywordSearcher() throws IOException {
    return new LuceneKeywordSearcher(fields, this);
  }

  @Override
  public void close() {
    if (!isOpen) return;

    FileUtil.closeSilently(index, true);
    index = null;
    isOpen = false;
  }

  /**
   * Creates a new IndexReader.
   * @return A reader alloing to query this index
   * @throws IOException Will be thrown if the FSDirectory couldn't be opened, doesn't exists or another low level I/O
   * Error occurs
   */
  public IndexReader createReader() throws IOException {
    return DirectoryReader.open(index);
  }

  public List<String> getSearchableFields() {
    return fields;
  }

  @Override
  public boolean hasIndexedData() throws IOException {
    if (!isOpen) throw new IOException("Index has to be successfully opened before calling this function!");

    try {
      if (!DirectoryReader.indexExists(index)) {
          // early exit as trying to create an index reader of a non existing
          // index Directory would rise an IOException
          return false;
      }

      IndexReader reader = createReader();
      return reader.numDocs() > 0;
    } catch (IOException e) {
      log.error("Error while retrieving document count: ", e);
      throw new IOException("Couldn't query indexed data");
    }
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
      throw new IOException("Couldn't open index in directory path: " + indexDirectoryPath, e);
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