package de.unipassau.medspace.wrapper.image_wrapper.play;

import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.RDFFactory;
import de.unipassau.medspace.common.rdf.TripleIndexManager;
import de.unipassau.medspace.common.rdf.rdf4j.RDF4J_RDFProvider;
import de.unipassau.medspace.common.util.RdfUtil;
import de.unipassau.medspace.common.wrapper.Wrapper;
import de.unipassau.medspace.wrapper.image_wrapper.DDSM_ImageWrapper;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.NamespaceParsing;
import de.unipassau.medspace.wrapper.image_wrapper.config.parsing.RootParsing;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.IcsFile;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.LuceneIndexFactory;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter.DDSM_AdapterFactory;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter.LuceneDocAdapter;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TODO
 */
public class WrapperProvider implements Provider<Wrapper> {

  /**
   * TODO
   */
  public static String IMAGE_FILE_ENDING = "png";

  /**
   * TODO
   */
  public static File root = new File("F:\\bachelorThesis\\DDSM\\WinSCP-Test\\cases\\cases");

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(WrapperProvider.class);

  /**
   * TODO
   */
  private Wrapper wrapper;

  /**
   * TODO
   */
  @Inject
  public WrapperProvider(ConfigProvider configProvider,
                         ShutdownService shutdownService) throws IOException {

    RootParsing rootParsing = configProvider.getDdsmConfig();

    DDSM_AdapterFactory adapterFactory = new DDSM_AdapterFactory(rootParsing);
    List<LuceneDocAdapter<?>> adapters = adapterFactory.createAdapters();

    Map<String, Namespace> namespaces = new HashMap<>();

    for (NamespaceParsing parsedNamespace :  rootParsing.getNamespace()) {
      String prefix = parsedNamespace.getPrefix().trim();
      String fullURI = parsedNamespace.getNamespace().trim();
      Namespace namespace = new Namespace(prefix, fullURI);
      namespaces.put(prefix, namespace);
    }

    QNameNormalizer normalizer = qName -> RdfUtil.getNormalizedURI(namespaces, qName);;
    RDFFactory factory = new RDF4J_RDFProvider().getFactory();

    Path indexDirectory = configProvider.getGeneralWrapperConfig().getIndexDirectory();

    LuceneIndexFactory indexFactory = new LuceneIndexFactory(indexDirectory.toString(),
        adapters,
        factory,
        normalizer);

    TripleIndexManager<Document, IcsFile> tripleIndexManager = indexFactory.createIndexManager();

    wrapper = new DDSM_ImageWrapper<>(tripleIndexManager,
        IMAGE_FILE_ENDING,
        namespaces,
        root);

    wrapper.getIndex().open();
    boolean shouldReindex = !wrapper.existsIndex() && wrapper.isIndexUsed();

    if (shouldReindex) {
      log.info("Indexing data...");
      wrapper.reindexData();
      log.info("Indexing done.");
    }
  }

  @Override
  public Wrapper get() {
    return wrapper;
  }
}
