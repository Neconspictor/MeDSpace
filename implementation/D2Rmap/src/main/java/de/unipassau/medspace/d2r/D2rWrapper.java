package de.unipassau.medspace.d2r;

import de.unipassau.medspace.common.SQL.DataSourceManager;
import de.unipassau.medspace.common.indexing.DataSourceIndex;
import de.unipassau.medspace.common.indexing.IndexFactory;
import de.unipassau.medspace.common.lucene.ResultFactory;
import de.unipassau.medspace.common.query.KeywordSearcher;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.rdf.TripleCacheStream;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.common.wrapper.Wrapper;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.d2r.query.D2rKeywordSearcher;
import de.unipassau.medspace.d2r.lucene.LuceneIndexFactory;
import de.unipassau.medspace.d2r.lucene.SqlToDocStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.lucene.document.Document;
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
  private DataSourceIndex<DocType, MappedSqlTuple> index;

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
  private DataSourceManager dataSourceManager;

  private boolean indexUsed;

  /**
   * TODO
   * @param dataSourceManager
   * @param maps
   * @param namespaces
   * @param indexDirectory
   * @throws IOException
   * @throws D2RException
   */
  public D2rWrapper(DataSourceManager dataSourceManager,
                    List<D2rMap> maps,
                    HashMap<String, Namespace> namespaces,
                    Path indexDirectory) throws IOException, D2RException
  {
    this.proxy = new D2rProxy(dataSourceManager);

    // We won't change the list; being cautious we make it unmodifiable.
    // TODO make the list elements itself immutable after they are initialized
    // TODO to assure that no concurrency problems can occur
    this.maps = Collections.unmodifiableList(maps);
    normalizer = qName -> getNormalizedURI(qName);
    idToMap = new HashMap<>();

    for (D2rMap map : maps) {
      idToMap.put(map.getId(), map);
      map.setNormalizer(normalizer);
      map.init(dataSourceManager.getDataSource());
    }

    this.namespaces = namespaces;

    namespacePrefixMapper = new PrefixMappingImpl();

    for (Namespace namespace : namespaces.values()) {
      namespacePrefixMapper.setNsPrefix(namespace.getPrefix(), namespace.getFullURI());
    }

    this.dataSourceManager = dataSourceManager;

    indexUsed = false;
  }

  /**
   * TODO
   * @throws IOException
   */
  @Override
  public void close() throws IOException {
    FileUtil.closeSilently(index, true);
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
        searcher = index.createKeywordSearcher();
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
  public DataSourceStream<MappedSqlTuple> getAllSourceData() throws IOException {
    return proxy.getAllData(maps);
  }

  /**
   * TODO
   * @return
   */
  public DataSourceIndex getIndex() {
    return index;
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


    SqlToDocStream<DocType> docStream = null;

    try {
      docStream = new SqlToDocStream<DocType>(getAllSourceData(), index.getResultFactory());
      index.open();
      index.reindex(docStream);

    } catch (IOException e) {
      throw new IOException("Error while reindexing", e);
    } finally {
      FileUtil.closeSilently(docStream, true);
    }
  }

  @Override
  public boolean existsIndex() {
    return !indexUsed ? false : index.exists();
  }

  @Override
  public boolean isIndexUsed() {
    return indexUsed;
  }

  @Override
  public DataSourceStream<Triple> getAllData() throws IOException {
    DataSourceStream<MappedSqlTuple> source = getAllSourceData();
    return new TripleCacheStream<MappedSqlTuple>(source){
      @Override
      protected List<Triple> createTriples(MappedSqlTuple elem) {
        return elem.getMap().createTriples(elem.getSource());
      }
    };
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
  public DataSourceManager getDataSourceManager() {
    return dataSourceManager;
  }


  /**
   * TODO
   * @param indexDirectory
   * @throws IOException
   */
  public void init(Path indexDirectory, IndexFactory<DocType, MappedSqlTuple> indexFactory) throws IOException {

    indexUsed = indexDirectory != null;

    // Should no index be used? -> early exit
    if (!indexUsed) return;


    try {
      try {
        index = indexFactory.createIndex();
        index.open();
      } catch (IOException e) {
        throw new IOException("Error while trying to createDoc index: ", e);
      }
    } catch (IOException e) {
      throw new IOException("Couldn't createDoc Index", e);
    }
  }
}