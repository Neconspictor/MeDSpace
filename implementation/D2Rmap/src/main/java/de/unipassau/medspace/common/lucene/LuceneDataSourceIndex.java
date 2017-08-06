package de.unipassau.medspace.common.lucene;

import de.unipassau.medspace.common.indexing.DataSourceIndex;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.util.FileUtil;
import org.apache.jena.graph.Triple;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
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
import java.util.Collections;
import java.util.List;

/**
 * TODO
 */
public class LuceneDataSourceIndex<ElemType> implements DataSourceIndex<Document> {

  private List<String> fields;
  private Path indexDirectoryPath;
  private FSDirectory index;
  private volatile boolean isOpen;
  private ResultFactory<ElemType, Document> resultFactory;

  private static Logger log = LoggerFactory.getLogger(DataSourceIndex.class);

  protected LuceneDataSourceIndex(Path directory, List<String> fields,
                                  ResultFactory<ElemType, Document> resultFactory) {
    indexDirectoryPath = directory;
    this.fields = fields;
    index = null;
    isOpen = false;
    this.resultFactory = resultFactory;
  }

  public static <ElemType> LuceneDataSourceIndex<ElemType> create(String directory, List<String> fields,
                                             ResultFactory<ElemType, Document> resultFactory) throws IOException {
    Path path = null;
    try {
      path = FileUtil.createDirectory(directory);
    } catch (IOException e) {
      throw new IOException("Couldn't createDoc index directory");
    }
    LuceneDataSourceIndex result = new LuceneDataSourceIndex(path,
        Collections.unmodifiableList(fields), resultFactory);

    return result;
  }

  public void clearIndex() throws IOException {
    if (!isOpen) throw new IOException("Indexer is not open!");
    Analyzer analyzer = buildAnalyzer();
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
  public KeywordSearcher<Document> createDocKeywordSearcher() throws IOException {
    return new LuceneKeywordSearcher(fields, () -> DirectoryReader.open(index), buildAnalyzer());
  }

  @Override
  public KeywordSearcher<Triple> convert(KeywordSearcher<Document> source) throws IOException {
    return keywords -> {
      DataSourceStream result =  source.searchForKeywords(keywords);
      return new DocToTripleStream(result, resultFactory);
    };
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
   * Creates a new IndexReader.
   * @return A reader alloing to query this index
   * @throws IOException Will be thrown if the FSDirectory couldn't be opened, doesn't exists or another low level I/O
   * Error occurs
   */
  private IndexReader createReader() throws IOException {
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

      IndexReader reader = createReader();
      return reader.numDocs() > 0;
    } catch (IOException e) {
      log.error("Error while retrieving document count: ", e);
      throw new IOException("Couldn't query indexed data");
    }
  }

  public void index(Iterable<Document> data) throws IOException {
    Analyzer analyzer = buildAnalyzer();
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

  /**
   * TODO
   * @return
   * @throws IOException
   */
  private Analyzer buildAnalyzer() throws IOException {
    return CustomAnalyzer.builder()
        .withTokenizer("whitespace")
        .addTokenFilter("lowercase")
        .addTokenFilter("standard")
        .build();
  }
}