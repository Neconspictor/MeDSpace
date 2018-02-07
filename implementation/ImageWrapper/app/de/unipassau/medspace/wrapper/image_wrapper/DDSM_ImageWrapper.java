package de.unipassau.medspace.wrapper.image_wrapper;

import de.unipassau.medspace.common.indexing.Index;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.rdf.TripleIndexManager;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.common.wrapper.Wrapper;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.IcsFile;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.IcsFileCollectorStream;
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
public class DDSM_ImageWrapper<DocType> implements Wrapper {

  /**
   * Logger
   */
  private static Logger log = LoggerFactory.getLogger(DDSM_ImageWrapper.class);

  /**
   * Used to index data and used for doing keyword searches.
   */
  private final TripleIndexManager<DocType, IcsFile> indexManager;

  /**
   * TODO
   */
  private final String imageFileEnding;

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
   * @param imageFileEnding
   * @param namespaces
   * @param root
   */
  public DDSM_ImageWrapper(TripleIndexManager<DocType, IcsFile> indexManager,
                           String imageFileEnding,
                           Map<String, Namespace> namespaces,
                           File root) {

    this.indexManager = indexManager;
    this.imageFileEnding = imageFileEnding;
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

    Stream<IcsFile> stream = new IcsFileCollectorStream(root, imageFileEnding);
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