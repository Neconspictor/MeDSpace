package de.unipassau.medspace.wrapper.sqlwrapper;

import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.indexing.IndexManager;
import de.unipassau.medspace.common.play.ShutdownService;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.TripleIndexFactory;
import de.unipassau.medspace.common.rdf.TripleIndexManager;
import de.unipassau.medspace.common.util.RdfUtil;
import de.unipassau.medspace.d2r.D2rMap;
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
import java.util.List;
import java.util.Map;

/**
 * A provider for the SQL wrapper.
 */
public class WrapperProvider implements Provider<D2rWrapper<?>> {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(WrapperProvider.class);

  private D2rWrapper<?> wrapper;

  /**
   * Creates a new WrapperProvider object.
   * @param configProvider The configuration provider.
   * @param connectionPool The SQL connection pool.
   * @param shutdownService The shutdown service.
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


  private void init(Configuration d2rConfig,
                    GeneralWrapperConfig generalConfig,
                    ConnectionPool connectionPool) throws D2RException, IOException {

    Path indexPath = generalConfig.getIndexDirectory();

    // namespaces can be defined inside both wrapper configuration files
    // Therefore put them all together
    Map<String, Namespace> namespaces = new HashMap<>(generalConfig.getNamespaces());
    namespaces.putAll(d2rConfig.getNamespaces());

    // Initialize d2r maps
    QNameNormalizer normalizer = qName -> RdfUtil.getNormalizedURI(namespaces, qName);
    List<D2rMap> maps = d2rConfig.getMaps();
    for (D2rMap map : maps) {
      map.setNormalizer(normalizer);
      map.init(connectionPool.getDataSource());
    }

    TripleIndexFactory<Document, MappedSqlTuple> indexFactory =
        new LuceneIndexFactory(maps, indexPath.toString());

    TripleIndexManager<Document, MappedSqlTuple> indexManager =
        indexFactory.createIndexManager();

    wrapper = new D2rWrapper<Document>(connectionPool,
        maps,
        namespaces,
        indexManager,
        generalConfig.isIndexUsed());

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