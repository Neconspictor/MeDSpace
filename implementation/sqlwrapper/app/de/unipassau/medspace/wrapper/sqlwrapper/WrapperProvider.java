package de.unipassau.medspace.wrapper.sqlwrapper;

import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.TripleIndexFactory;
import de.unipassau.medspace.d2r.D2rWrapper;
import de.unipassau.medspace.d2r.MappedSqlTuple;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.d2r.lucene.LuceneIndexFactory;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 */
public class WrapperProvider implements Provider<D2rWrapper<?>> {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(WrapperProvider.class);

  /**
   * TODO
   */
  private D2rWrapper<?> wrapper;

  /**
   * TODO
   * @param configProvider
   * @param connectionPool
   * @param shutdownService
   */
  @Inject
  public WrapperProvider(ConfigProvider configProvider,
                         ConnectionPool connectionPool,
                         ShutdownService shutdownService) {

    try {
      init(configProvider.getD2rConfig(),
          configProvider.getGeneralWrapperConfig(),
          connectionPool);
    } catch (D2RException | IOException e) {
      log.error("Couldn't create D2rWrapper: ", e);
      shutdownService.gracefulShutdown(ShutdownService.EXIT_ERROR);
    }
  }

  /**
   * TODO
   * @param d2rConfig
   * @param generalConfig
   * @param connectionPool
   * @throws D2RException
   * @throws IOException
   */
  private void init(Configuration d2rConfig,
                    GeneralWrapperConfig generalConfig,
                    ConnectionPool connectionPool) throws D2RException, IOException {

    Path indexPath = generalConfig.getIndexDirectory();

    // namespaces can be defined inside both wrapper configuration files
    // Therefore put them all together
    Map<String, Namespace> namespaces = new HashMap<>(generalConfig.getNamespaces());
    namespaces.putAll(d2rConfig.getNamespaces());

    wrapper = new D2rWrapper<Document>(connectionPool, d2rConfig.getMaps(), namespaces);

    D2rWrapper<Document> specificWrapper = (D2rWrapper<Document>) wrapper;

    TripleIndexFactory<Document, MappedSqlTuple> indexFactory =
        new LuceneIndexFactory(specificWrapper, indexPath.toString());

    specificWrapper.init(indexPath, indexFactory);

    boolean shouldReindex = !wrapper.existsIndex() && wrapper.isIndexUsed();

    if (shouldReindex) {
      log.info("Indexing data...");
      wrapper.reindexData();
      log.info("Indexing done.");
    }
  }

  @Override
  public D2rWrapper<?> get() {
    return wrapper;
  }
}