package de.unipassau.medspace.wrapper.pdf_wrapper.play;

import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.config.ServerConfig;
import de.unipassau.medspace.common.play.ShutdownService;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.RDFFactory;
import de.unipassau.medspace.common.rdf.TripleIndexManager;
import de.unipassau.medspace.common.rdf.mapping.NamespaceMapping;
import de.unipassau.medspace.common.rdf.rdf4j.RDF4J_RDFProvider;
import de.unipassau.medspace.common.util.RdfUtil;
import de.unipassau.medspace.common.wrapper.Wrapper;
import de.unipassau.medspace.wrapper.pdf_wrapper.PdfWrapper;
import de.unipassau.medspace.wrapper.pdf_wrapper.config.mapping.RootMapping;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.PdfFile;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.LuceneIndexFactory;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.adapter.PdfFileAdapter;
import de.unipassau.medspace.wrapper.pdf_wrapper.pdf.lucene.adapter.Pdf_AdapterFactory;
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
 * A provider for the PDF wrapper.
 */
public class WrapperProvider implements Provider<Wrapper> {

  private static Logger log = LoggerFactory.getLogger(WrapperProvider.class);


  private Wrapper wrapper;

  /**
   * Creates a new WrapperProvider object.
   * @param pdfConfig the PDF wrapper configuration.
   * @param generalWrapperConfig the general wrapper configuration
   * @param serverConfig the server configuration
   * @param shutdownService The shutdown service.
   */
  @Inject
  public WrapperProvider(RootMapping pdfConfig,
                         GeneralWrapperConfig generalWrapperConfig,
                         ServerConfig serverConfig,
                         ShutdownService shutdownService)  {

    try {
      init(pdfConfig, generalWrapperConfig, serverConfig);
    }catch (Exception e) {
      log.error("Error while initializing the wrapper");
      log.info("Shutdown application...");
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }
  }

  private void init(RootMapping pdfConfig,
                    GeneralWrapperConfig generalWrapperConfig,
                    ServerConfig serverConfig) throws IOException {

    String host = serverConfig.getServerURL().toString();
    if (!host.endsWith("/")) {
      host += "/";
    }

    String downloadService = host + "get-file?relativePath=";

    Pdf_AdapterFactory adapterFactory = new Pdf_AdapterFactory(pdfConfig, downloadService);
    List<PdfFileAdapter> adapters = adapterFactory.createAdapters();

    Map<String, Namespace> namespaces = new HashMap<>();

    for (NamespaceMapping parsedNamespace :  pdfConfig.getNamespace()) {
      String prefix = parsedNamespace.getPrefix().trim();
      String fullURI = parsedNamespace.getNamespace().trim();
      Namespace namespace = new Namespace(prefix, fullURI);
      namespaces.put(prefix, namespace);
    }

    QNameNormalizer normalizer = qName -> RdfUtil.getNormalizedURI(namespaces, qName);;
    RDFFactory factory = new RDF4J_RDFProvider().getFactory();

    Path indexDirectory = generalWrapperConfig.getIndexDirectory();

    LuceneIndexFactory indexFactory = new LuceneIndexFactory(indexDirectory.toString(),
        adapters,
        factory,
        normalizer);

    TripleIndexManager<Document, PdfFile> tripleIndexManager = indexFactory.createIndexManager();

    wrapper = new PdfWrapper<>(tripleIndexManager,
        namespaces,
        new File(pdfConfig.getPdfRootDirectory()));

    wrapper.getIndex().open();
    boolean shouldReindex = !wrapper.existsIndex() && wrapper.isIndexUsed();

    if (generalWrapperConfig.getForceReindex()) {
      shouldReindex = true;
    }


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
