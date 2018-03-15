package de.unipassau.medspace.wrapper.pdf_wrapper;

import de.unipassau.medspace.common.indexing.Index;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.Triple;
import de.unipassau.medspace.common.rdf.TripleIndexManager;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.common.wrapper.AbstractWrapper;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.PdfFile;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.PdfFileCollectorStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * TODO
 */
public class PdfWrapper<DocType> extends AbstractWrapper<DocType, PdfFile> {

  /**
   * Logger
   */
  private static Logger log = LoggerFactory.getLogger(PdfWrapper.class);


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
                    File root) throws IOException {
    super(indexManager, namespaces);
    this.root = root;
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
  public void close() throws IOException {
    FileUtil.closeSilently(indexManager, true);
  }
}