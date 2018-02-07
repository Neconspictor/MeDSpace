package de.unipassau.medspace.d2r;

import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.indexing.Index;
import de.unipassau.medspace.common.rdf.*;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.Converter;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.common.util.RdfUtil;
import de.unipassau.medspace.common.wrapper.Wrapper;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.d2r.query.D2rKeywordSearcher;
import de.unipassau.medspace.common.stream.StreamConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A D2rWrapper is a wrapper for a sql database that uses D2r mapping for exporting data to rdf triples.
 * @param <DocType> Specifies the class type of the documents used by the index if thi wrapper should use an index.
 */
public class D2rWrapper<DocType> implements Wrapper {

  /**
   * Logger
   */
  private static Logger log = LoggerFactory.getLogger(D2rWrapper.class);

  /**
   * Used to get data from the datasource.
   */
  private D2rProxy proxy;

  /**
   * Used to index data and used for doing keyword searches.
   */
  private TripleIndexManager<DocType, MappedSqlTuple> indexManager;

  /**
   * Allows accessing D2rMaps by their id.
   */
  private HashMap<String, D2rMap> idToMap;

  /**
   * The list of used D2rMaps.
   */
  private List<D2rMap> maps;

  /**
   * Allows accessing namespaces by their prefixes.
   */
  private Map<String, Namespace> namespaces;

  /**
   * Used to get a connection to the datasource. Primarily used to create the proxy to the datasource.
   */
  private ConnectionPool connectionPool;

  /**
   * Used to specify whether an index is used.
   */
  private boolean indexUsed;

  /**
   * Creates a new D2rWrapper.
   * @param connectionPool The connction pool to use to get a connection to the datasource.
   * @param maps The list of D2rMaps that this wrapper should use.
   * @param namespaces A mapping of namespaces this wrapper should use.
   * @throws D2RException If the wrapper couldn't be created successfully.
   */
  public D2rWrapper(ConnectionPool connectionPool,
                    List<D2rMap> maps,
                    Map<String, Namespace> namespaces) throws D2RException
  {
    this.proxy = new D2rProxy(connectionPool);

    // We won't change the list; being cautious we make it unmodifiable.
    // TODO make the list elements itself immutable after they are initialized
    // TODO to assure that no concurrency problems can occur
    this.maps = Collections.unmodifiableList(maps);
    QNameNormalizer normalizer = qName -> RdfUtil.getNormalizedURI(namespaces, qName);
    idToMap = new HashMap<>();

    for (D2rMap map : maps) {
      idToMap.put(map.getId(), map);
      map.setNormalizer(normalizer);
      map.init(connectionPool.getDataSource());
    }

    this.namespaces = namespaces;

    this.connectionPool = connectionPool;

    indexUsed = false;
  }


  @Override
  public void close() throws IOException {
    FileUtil.closeSilently(indexManager, true);
  }

  @Override
  public KeywordSearcher<Triple> createKeywordSearcher() throws IOException {

    KeywordSearcher<Triple> searcher;

    try {
      if (indexUsed) {
        searcher = indexManager.createTripleKeywordSearcher();
      } else {
        searcher = new D2rKeywordSearcher(this);
      }
    } catch (IOException e) {
      throw new IOException("Error while trying to create a keyword searcher", e);
    }

    return searcher;
  }

  /**
   * Provides all the data of the datasource that are mapped by the D2rMaps of this class.
   * @return A stream of mapped sql tuples. Represents all data from the datasource that can be accessed by this
   * wrapper.
   * @throws IOException If an IO-Error occurs.
   */
  public Stream<MappedSqlTuple> getAllSourceData() throws IOException {
    return proxy.getAllData(maps);
  }

  @Override
  public Index getIndex() {
    if (indexManager == null) return null;
      return indexManager.getIndex();
  }

  /**
   * Provides a D2rMap by its id.
   * @param id The id for searching the D2rMap.
   * @return The D2rMap that has the specified id or null, if no D2rMap was found having the specified id.
   */
  public D2rMap getMapById(String id) {
    return idToMap.get(id);
  }

  @Override
  public Set<Namespace> getNamespaces() {
    return namespaces.values().stream().collect(Collectors.toSet());
  }


  @Override
  public void reindexData() throws IOException {

    if (!indexUsed) throw new IOException("Cannot reindex data as no index is used!");

    long before = System.currentTimeMillis();

    Index<DocType> index = indexManager.getIndex();
    //Converter<MappedSqlTuple, DocType> converter =  indexManager.getConverterToDoc();
    Stream<DocType> docStream = null;

    try {
      index.close();
      index.open();
      docStream = indexManager.convert(getAllSourceData());
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
    return !indexUsed ? false : indexManager.getIndex().exists();
  }

  @Override
  public boolean isIndexUsed() {
    return indexUsed;
  }

  /**
   * Provides read access to the list of D2rMaps
   * @return An unmodifiable list of D2rMaps
   */
  public List<D2rMap> getMaps() {
    return maps;
  }

  /**
   * Provides the connection pool os this wrapper.
   * @return The connection pool of this wrapper.
   */
  public ConnectionPool getConnectionPool() {
    return connectionPool;
  }


  /**
   * Inits the wrapper.
   * @param indexDirectory The directory to store indexed data to or null, if the wrapper shouldn't use an index.
   * @param indexFactory A factory that is used to create the index for this wrapper.
   * @throws IOException If an IO-Error occurs.
   */
  public void init(Path indexDirectory, TripleIndexFactory<DocType, MappedSqlTuple> indexFactory) throws IOException {

    indexUsed = indexDirectory != null;

    // Should no index be used? -> early exit
    if (!indexUsed) return;


    try {
      try {
        indexManager = indexFactory.createIndexManager();
        Index<DocType> index = indexManager.getIndex();
        index.open();
      } catch (IOException e) {
        throw new IOException("Error while trying to createDoc index: ", e);
      }
    } catch (IOException e) {
      throw new IOException("Couldn't create Index", e);
    }
  }
}