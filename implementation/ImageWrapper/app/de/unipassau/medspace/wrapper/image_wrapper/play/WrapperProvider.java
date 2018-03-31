package de.unipassau.medspace.wrapper.image_wrapper.play;

import de.unipassau.medspace.common.config.ServerConfig;
import de.unipassau.medspace.common.lucene.rdf.LuceneClassAdapter;
import de.unipassau.medspace.common.play.ServerConfigProvider;
import de.unipassau.medspace.common.play.ShutdownService;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.RDFFactory;
import de.unipassau.medspace.common.rdf.TripleIndexManager;
import de.unipassau.medspace.common.rdf.mapping.NamespaceMapping;
import de.unipassau.medspace.common.rdf.rdf4j.RDF4J_RDFProvider;
import de.unipassau.medspace.common.util.RdfUtil;
import de.unipassau.medspace.common.wrapper.Wrapper;
import de.unipassau.medspace.wrapper.image_wrapper.DDSM_ImageWrapper;
import de.unipassau.medspace.wrapper.image_wrapper.config.DDSMConfig;
import de.unipassau.medspace.wrapper.image_wrapper.config.mapping.RootMapping;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.IcsFile;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.LuceneIndexFactory;
import de.unipassau.medspace.wrapper.image_wrapper.ddsm.lucene.adapter.DDSM_AdapterFactory;
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

/**
 * A provider of the DDSM Image Wrapper.
 */
public class WrapperProvider implements Provider<Wrapper> {

  /**
   * The image file ending.
   */
  public static final String IMAGE_FILE_ENDING = "png";

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(WrapperProvider.class);

  /**
   * The root folder for the DDSM cases.
   */
  private File caseDirectory;


  /**
   * The created wrapper.
   */
  private Wrapper wrapper;

  /**
   * Creates a new WrapperProvider object.
   *
   * @param configProvider The provider for the DDSM configurations.
   * @param shutdownService The shutdown service.
   * @param serverConfigProvider the provider for the server configuration.
   * @throws IOException If an IO error occurs.
   */
  @Inject
  public WrapperProvider(DdsmConfigProvider configProvider,
                         ServerConfigProvider serverConfigProvider,
                         ShutdownService shutdownService) throws IOException {

    RootMapping rootParsing = configProvider.getDdsmMappingConfig();
    DDSMConfig ddsmConfig = configProvider.getDdsmConfig();

    caseDirectory = new File(ddsmConfig.getImageDirectory());

    if (!caseDirectory.isDirectory()) throw new IOException("The image directory is not a valid directory: " + caseDirectory);

    String downloadFileServiceURL = createGetFileServiceURL(configProvider, serverConfigProvider.getServerConfig());
    DDSM_AdapterFactory adapterFactory = new DDSM_AdapterFactory(caseDirectory, rootParsing,
        downloadFileServiceURL);
    List<LuceneClassAdapter<?>> adapters = adapterFactory.createAdapters();

    Map<String, Namespace> namespaces = new HashMap<>();

    for (NamespaceMapping parsedNamespace :  rootParsing.getNamespace()) {
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
        caseDirectory);

    wrapper.getIndex().open();
    boolean shouldReindex = !wrapper.existsIndex() && wrapper.isIndexUsed();

    if (shouldReindex) {
      log.info("Indexing data...");
      wrapper.reindexData();
      log.info("Indexing done.");
    }
  }


  private String createGetFileServiceURL(DdsmConfigProvider configProvider, ServerConfig serverConfig) {
    String testParam = "test";

    String host = serverConfig.getServerURL().toExternalForm();

    if (host.endsWith("/")) {
      host = host.substring(0, host.length() -1);
    }

    //create a relative url to the getFile service and with a test param
    String getFile = controllers.routes.ImageWrapperController.getFile(testParam).url();

    //delete the test param
    getFile = getFile.substring(0, getFile.length() - testParam.length());

    return host + getFile;
  }

  @Override
  public Wrapper get() {
    return wrapper;
  }
}