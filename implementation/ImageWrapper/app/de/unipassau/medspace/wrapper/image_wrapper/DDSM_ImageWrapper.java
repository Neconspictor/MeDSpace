package de.unipassau.medspace.wrapper.image_wrapper;

import de.unipassau.medspace.common.indexing.Index;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.TripleIndexManager;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.common.wrapper.AbstractWrapper;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.IcsFile;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.IcsFileCollectorStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * TODO
 */
public class DDSM_ImageWrapper<DocType> extends AbstractWrapper<DocType, IcsFile> {

  /**
   * Logger
   */
  private static Logger log = LoggerFactory.getLogger(DDSM_ImageWrapper.class);

  /**
   * TODO
   */
  private final String imageFileEnding;


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
                           File root) throws IOException {

    super(indexManager, namespaces);
    this.imageFileEnding = imageFileEnding;
    this.root = root;
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
  public void close() throws IOException {
    FileUtil.closeSilently(indexManager, true);
  }
}