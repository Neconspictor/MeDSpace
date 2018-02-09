package de.unipassau.medspace.wrapper.pdf_wrapper;

import de.unipassau.medspace.common.indexing.Index;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.rdf.TripleIndexManager;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.common.wrapper.Wrapper;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.PdfFile;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.PdfFileCollectorStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO
 */
public class PdfWrapper<DocType> implements Wrapper {

  /**
   * Logger
   */
  private static Logger log = LoggerFactory.getLogger(PdfWrapper.class);

  /**
   * Used to index data and used for doing keyword searches.
   */
  private final TripleIndexManager<DocType, PdfFile> indexManager;

  /**
   * Allows accessing namespaces by their prefixes.
   */
  private final Map<String, Namespace> namespaces;


  /**
   * TODO
   */
  private final File root;

  /**
   * TODO
   * @param indexManager
   * @param namespaces
   * @param root
   */
  public PdfWrapper(TripleIndexManager<DocType, PdfFile> indexManager,
                    Map<String, Namespace> namespaces,
                    File root) {

    this.indexManager = indexManager;
    this.namespaces = namespaces;
    this.root = root;
  }

  @Override
  public KeywordSearcher<Triple> createKeywordSearcher() throws IOException {
    try {
        return indexManager.createTripleKeywordSearcher();
    } catch (IOException e) {
      throw new IOException("Error while trying to create a keyword searcher", e);
    }
  }

  @Override
  public Index getIndex() {
    return indexManager.getIndex();
  }

  @Override
  public Set<Namespace> getNamespaces() {
    return namespaces.values().stream().collect(Collectors.toSet());
  }

  @Override
  public void reindexData() throws IOException {
    long before = System.currentTimeMillis();

    Index<DocType> index = indexManager.getIndex();

    Stream<PdfFile> stream = new PdfFileCollectorStream(root);
    Stream<DocType> docStream = indexManager.convert(stream);

    try {
      index.close();
      index.open();
      index.reindex(docStream);

    } catch (IOException e) {
      throw new IOException("Error while reindexing", e);
    } finally {
      FileUtil.closeSilently(docStream, true);
    }

    long now = System.currentTimeMillis();
    log.debug("Needed time: " + (now - before)/1000.0f + " seconds");
  }

  @Override
  public boolean existsIndex() {
    return indexManager.getIndex().exists();
  }

  @Override
  public boolean isIndexUsed() {
    return true;
  }

  @Override
  public void close() throws IOException {
    FileUtil.closeSilently(indexManager, true);
  }
}