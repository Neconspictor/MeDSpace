package de.unipassau.medspace.d2r;

import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.indexing.Index;
import de.unipassau.medspace.common.rdf.*;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.common.wrapper.AbstractWrapper;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.d2r.query.D2rKeywordSearcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * A D2rWrapper is a wrapper for a sql database that uses D2r mapping for exporting data to rdf triples.
 * @param <DocType> Specifies the class type of the documents used by the index if thi wrapper should use an index.
 */
public class D2rWrapper<DocType> extends AbstractWrapper<DocType, MappedSqlTuple> {

  /**
   * Logger
   */
  private static Logger log = LoggerFactory.getLogger(D2rWrapper.class);

  /**
   * Used to get data from the datasource.
   */
  private D2rProxy proxy;

  /**
   * The list of used D2rMaps.
   */
  private List<D2rMap> maps;

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
   * @param indexManager The index manager to use.
   * @param maps The list of D2rMaps that this wrapper should use.
   * @param namespaces A mapping of namespaces this wrapper should use.
   * @param useIndex If the wrapper should uses an index.
   * @throws IOException If the wrapper couldn't be created successfully.
   */
  public D2rWrapper(ConnectionPool connectionPool,
                    List<D2rMap> maps,
                    Map<String, Namespace> namespaces,
                    TripleIndexManager<DocType, MappedSqlTuple> indexManager,
                    boolean useIndex) throws IOException {
    super(indexManager, namespaces);
    this.proxy = new D2rProxy(connectionPool);

    // We won't change the list; being cautious we make it unmodifiable.
    this.maps = Collections.unmodifiableList(maps);
    this.connectionPool = connectionPool;
    indexUsed = useIndex;

    if (!indexUsed)
      indexManager.getIndex().close();
  }


  @Override
  public void close() throws IOException {
    FileUtil.closeSilently(indexManager, true);
  }

  @Override
  public KeywordSearcher<Triple> createKeywordSearcher(KeywordSearcher.Operator operator) throws IOException {

    KeywordSearcher<Triple> searcher;

    try {
      if (indexUsed) {
        searcher = indexManager.createTripleKeywordSearcher(operator);
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
    return !indexUsed ? false : super.existsIndex();
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
}