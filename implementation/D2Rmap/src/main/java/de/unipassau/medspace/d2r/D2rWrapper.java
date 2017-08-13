package de.unipassau.medspace.d2r;

import de.unipassau.medspace.common.SQL.ConnectionPool;
import de.unipassau.medspace.common.indexing.Index;
import de.unipassau.medspace.common.rdf.TripleIndexFactory;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.TripleCacheStream;
import de.unipassau.medspace.common.rdf.TripleIndexManager;
import de.unipassau.medspace.common.stream.Stream;
import de.unipassau.medspace.common.util.Converter;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.common.wrapper.Wrapper;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.d2r.query.D2rKeywordSearcher;
import de.unipassau.medspace.d2r.lucene.SqlToDocStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * TODO
 */
public class D2rWrapper<DocType> implements Wrapper {

  /**
   * Logger
   */
  private static Logger log = LoggerFactory.getLogger(D2rWrapper.class);

  /**
   * TODO
   */
  private D2rProxy proxy;

  /**
   * TODO
   */
  private TripleIndexManager<DocType, MappedSqlTuple> indexManager;

  /**
   * TODO
   */
  private HashMap<String, D2rMap> idToMap;

  /**
   * TODO
   */
  private List<D2rMap> maps;

  /**
   * TODO
   */
  private QNameNormalizer normalizer;

  /**
   * TODO
   */
  private HashMap<String, Namespace> namespaces;

  /**
   * TODO
   */
  private PrefixMapping namespacePrefixMapper;

  /**
   * TODO
   */
  private ConnectionPool connectionPool;

  private boolean indexUsed;

  /**
   * TODO
   * @param connectionPool
   * @param maps
   * @param namespaces
   * @param indexDirectory
   * @throws IOException
   * @throws D2RException
   */
  public D2rWrapper(ConnectionPool connectionPool,
                    List<D2rMap> maps,
                    HashMap<String, Namespace> namespaces,
                    Path indexDirectory) throws IOException, D2RException
  {
    this.proxy = new D2rProxy(connectionPool);

    // We won't change the list; being cautious we make it unmodifiable.
    // TODO make the list elements itself immutable after they are initialized
    // TODO to assure that no concurrency problems can occur
    this.maps = Collections.unmodifiableList(maps);
    normalizer = qName -> getNormalizedURI(qName);
    idToMap = new HashMap<>();

    for (D2rMap map : maps) {
      idToMap.put(map.getId(), map);
      map.setNormalizer(normalizer);
      map.init(connectionPool.getDataSource());
    }

    this.namespaces = namespaces;

    namespacePrefixMapper = new PrefixMappingImpl();

    for (Namespace namespace : namespaces.values()) {
      namespacePrefixMapper.setNsPrefix(namespace.getPrefix(), namespace.getFullURI());
    }

    this.connectionPool = connectionPool;

    indexUsed = false;
  }

  /**
   * TODO
   * @throws IOException
   */
  @Override
  public void close() throws IOException {
    FileUtil.closeSilently(indexManager, true);
  }

  /**
   * TODO
   * @return
   * @throws IOException
   */
  @Override
  public KeywordSearcher<Triple> createKeywordSearcher() throws IOException {

    KeywordSearcher<Triple> searcher = null;

    try {
      if (indexUsed) {
        searcher = indexManager.createTripleKeywordSearcher();
      } else {
        searcher = new D2rKeywordSearcher(this);
      }
    } catch (IOException e) {
      throw new IOException("Error while trying to createDoc a keyword searcher", e);
    }

    return searcher;
  }

  /**
   * TODO
   * @return
   * @throws IOException
   */
  public Stream<MappedSqlTuple> getAllSourceData() throws IOException {
    return proxy.getAllData(maps);
  }

  /**
   * TODO
   * @return
   */
  public Index getIndex() {
    return indexManager.getIndex();
  }

  /**
   * TODO
   * @param id
   * @return
   */
  public D2rMap getMapById(String id) {
    return idToMap.get(id);
  }

  /**
   * Translates a qName to an URI using the namespace mapping of the D2R map.
   * @param qName Qualified name to be translated. See <a href="https://www.w3.org/TR/REC-xml-names/#dt-qualname">
   *              https://www.w3.org/TR/REC-xml-names/#dt-qualname</a> for a detailed description
   * @return the URI of the qualified name.
   */
  @SuppressWarnings("SpellCheckingInspection")
  public String getNormalizedURI(String qName) {
    String prefix = D2rUtil.getNamespacePrefix(qName);
    Namespace namespace = namespaces.get(prefix);
    if (namespace != null) {
      String localName = D2rUtil.getLocalName(qName);
      return namespace.getFullURI() + localName;
    }
    else {
      return qName;
    }
  }

  /**
   * TODO
   * @return
   */
  public PrefixMapping getNamespacePrefixMapper() {
    return namespacePrefixMapper;
  }

  /**
   * TODO
   * @throws IOException
   */
  @Override
  public void reindexData() throws IOException {

    if (!indexUsed) throw new IOException("Cannot reindex data as no index is used!");

    Index<DocType> index = indexManager.getIndex();
    Converter<MappedSqlTuple, DocType> converter =  indexManager.getConverterToDoc();
    SqlToDocStream<DocType> docStream = null;

    try {
      index.close();
      index.open();
      docStream = new SqlToDocStream<DocType>(getAllSourceData(), converter);
      index.reindex(docStream);

    } catch (IOException e) {
      throw new IOException("Error while reindexing", e);
    } finally {
      FileUtil.closeSilently(docStream, true);
    }
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
   * TODO
   * @return
   */
  public HashMap<String, Namespace> getNamespaces() {
    return namespaces;
  }

  /**
   * Provides read access to the list of D2rMaps
   * @return An unmodifiable list of D2rMaps
   */
  public List<D2rMap> getMaps() {
    return maps;
  }

  /**
   * TODO
   * @return
   */
  public ConnectionPool getConnectionPool() {
    return connectionPool;
  }


  /**
   * TODO
   * @param indexDirectory
   * @throws IOException
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