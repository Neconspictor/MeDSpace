package de.unipassau.medspace.d2r;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import de.unipassau.medspace.common.SQL.DataSourceManager;
import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.SQL.SqlStream;
import de.unipassau.medspace.common.indexing.IndexImpl;
import de.unipassau.medspace.common.rdf.URINormalizer;
import de.unipassau.medspace.common.stream.StreamFactory;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.d2r.exception.FactoryException;
import de.unipassau.medspace.common.indexing.Index;
import de.unipassau.medspace.common.stream.StreamCollection;
import de.unipassau.medspace.d2r.indexing.SqlToDocumentStream;
import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.common.SQL.SelectStatement;
import org.apache.log4j.Logger;


import org.apache.lucene.document.Document;

import javax.sql.DataSource;

/**
 * D2R processor exports data from a RDBMS into an RDF model using a D2R MAP.
 * D2R MAP is a declarative, XML-based language to describe mappings between the relational
 * database model and the graph-based RDF data model. The resulting model can be serialized as RDF, N3, N-TRIPLES or exported
 * directly as Jena model. The processors is compliant with all relational databases offering JDBC or ODBC access.
 * The processor can be used in a servlet environment to dynamically publish XHTML pages
 * containing RDF, as a database connector in applications working with Jena models or as a command line tool.
 * The D2R Map language specification and usage examples are found at
 * http://www.wiwiss.fu-berlin.de/suhl/bizer/d2rmap/D2Rmap.htm.
 *
 * <BR><BR>History:
 * <BR>18-05-2017   : Updated for Java 8; removed unsafe operations; all-embracing refactoring
 * <BR>07-21-2004   : Process map methods added.
 * <BR>07-21-2004   : Connection and driver accessors added. 
 * <BR>07-21-2004   : Error handling changed to Log4J.
 * <BR>09-25-2003   : Changed for Jena2.
 * <BR>01-15-2003   : Initial version of this class.
 *
 * @author Chris Bizer chris@bizer.de / David Goeth goeth@fim.uni-passau.de
 * @version V0.3.1
 */
public class D2rProcessor {
  private List<D2rMap> maps;
  private Index index;
  private HashMap<String, String> namespaces;
  private URINormalizer normalizer;
  private HashMap<String, D2rMap> idToMap;

  /** log4j logger used for this class */
  private static Logger log = Logger.getLogger(D2rProcessor.class);

  private DataSourceManager dataSourceManager;


  public D2rProcessor(Configuration config, DataSourceManager dataSourceManager) throws D2RException {
    assert config != null;
    assert dataSourceManager != null;

    maps = config.getMaps();
    namespaces = config.getNamespaces();

    // we don't want others to change the state of the processor
    config.setMaps(null);
    config.setNamespaces(null);

    this.dataSourceManager = dataSourceManager;

    index = null;
    if (config.isIndexUsed()) {
     try {
       String directory = config.getIndexDirectory().toString();
       index = IndexImpl.create(directory);
       index.open();
     } catch (IOException e) {
       log.error(e);
       throw new D2RException("Couldn't create index!");
     }
    }

    for (D2rMap map : maps) {
      map.init(dataSourceManager.getDataSource(), maps);
    }

    normalizer = URI -> getNormalizedURI(URI);

    idToMap = new HashMap<>();
    for (D2rMap map : maps) {
      idToMap.put(map.getId(), map);
    }
  }

  /**
   * TODO
   * @param map
   * @param dataSource
   * @param conditionList
   * @return
   * @throws D2RException
   */
  public StreamFactory<Document> createLuceneDocStreamFactory(D2rMap map, DataSource dataSource,
                                                              List<String> conditionList) throws D2RException {

    SelectStatement statement = map.getQuery();
    for (String condition : conditionList) {
      statement.addTemporaryCondition(condition);
    }

    String query = statement.toString();
    statement.reset();
    String ucQuery = query.toUpperCase();
    if (ucQuery.contains("UNION"))
      throw new D2RException("SQL statement should not contain UNION: " + query);


    SqlStream.QueryParams queryParams = new SqlStream.QueryParams(dataSource, query);
    StreamFactory<SQLResultTuple> factory = () -> {
      try {
        return new SqlStream(queryParams);
      } catch (SQLException e) {
        throw new IOException("Couldn't create stream to the sql datasource", e);
      }
    };

    //generate resources using the Connection
    return () -> new SqlToDocumentStream(factory, map);
  }


  public StreamCollection<Document> getAllAsLuceneDocs() throws D2RException {
    StreamCollection<Document> result = new StreamCollection();
    for (D2rMap map : maps) {
      result.add(createLuceneDocStreamFactory(map, dataSourceManager.getDataSource(), new ArrayList<>()));
    }

    return result;
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
    String uriPrefix = namespaces.get(prefix);
    if (uriPrefix != null) {
      String localName = D2rUtil.getLocalName(qName);
      return uriPrefix + localName;
    }
    else {
      return qName;
    }
  }

  public D2rMap getMapById(String id) {
    return idToMap.get(id);
  }

  public URINormalizer getNormalizer() {
    return normalizer;
  }

  public void reindex() throws D2RException {
    StreamCollection<Document> docStream = getAllAsLuceneDocs();

    try {
      index.open();
      docStream.start();
      index.reindex(docStream);
      // for (Document doc : docStream) {System.out.println(doc.toString());}


    } catch (IOException e) {
      throw new D2RException("Error while reindexing", e);
    } finally {
      FileUtil.closeSilently(docStream, true);
    }

  }

  public void shutdown() {
    FileUtil.closeSilently(index, true);
  }


  private void clear() throws FactoryException {
    // clear maps
    for (D2rMap map : maps)
      map.clear();
  }

  public List<D2rMap> getMaps() {
    return maps;
  }

  public Index getIndex() {
    return index;
  }

  public HashMap<String, String> getNamespaces() {
    return namespaces;
  }

  public DataSourceManager getDataSourceManager() {
    return dataSourceManager;
  }
}