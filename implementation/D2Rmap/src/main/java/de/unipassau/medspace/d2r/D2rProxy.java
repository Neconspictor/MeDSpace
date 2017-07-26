package de.unipassau.medspace.d2r;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import de.unipassau.medspace.common.SQL.DataSourceManager;
import de.unipassau.medspace.common.SQL.SQLResultTuple;
import de.unipassau.medspace.common.SQL.SqlStream;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.rdf.QNameNormalizer;
import de.unipassau.medspace.common.stream.DataSourceStream;
import de.unipassau.medspace.common.stream.StreamFactory;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.common.stream.StreamCollection;
import de.unipassau.medspace.common.SQL.SelectStatement;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
public class D2rProxy {
  private List<D2rMap> maps;
  private HashMap<String, Namespace> namespaces;
  private PrefixMapping namespacePrefixMapper;
  private QNameNormalizer normalizer;
  private HashMap<String, D2rMap> idToMap;

  /** log4j logger used for this class */
  private static Logger log = LoggerFactory.getLogger(D2rProxy.class);

  private DataSourceManager dataSourceManager;


  public D2rProxy(Configuration config, DataSourceManager dataSourceManager) throws D2RException {
    assert config != null;
    assert dataSourceManager != null;

    maps = config.getMaps();
    namespaces = config.getNamespaces();

    // we don't want others to change the state of the processor
    config.setMaps(null);
    config.setNamespaces(null);

    this.dataSourceManager = dataSourceManager;
    normalizer = qName -> getNormalizedURI(qName);
    idToMap = new HashMap<>();

    for (D2rMap map : maps) {
      idToMap.put(map.getId(), map);
      map.setNormalizer(normalizer);
      map.init(dataSourceManager.getDataSource());
    }

    namespacePrefixMapper = new PrefixMappingImpl();

    for (Namespace namespace : namespaces.values()) {
      namespacePrefixMapper.setNsPrefix(namespace.getPrefix(), namespace.getFullURI());
    }
  }

  /**
   * TODO
   * @param map TODO
   * @param dataSource TODO
   * @param conditionList TODO
   * @return TODO
   * @throws IOException TODO
   */
  public StreamFactory<MappedSqlTuple> createStreamFactory(D2rMap map, DataSource dataSource,
                                                           List<String> conditionList) throws IOException {

    SelectStatement statement = map.getQuery();
    for (String condition : conditionList) {
      statement.addTemporaryCondition(condition);
    }

    String query = statement.toString();

    SqlStream.QueryParams queryParams = new SqlStream.QueryParams(dataSource, query);
    StreamFactory<SQLResultTuple> resultTupleFactory = () -> {
      try {
        return new SqlStream(queryParams);
      } catch (SQLException e) {
        throw new IOException("Couldn't create stream to the sql datasource", e);
      }
    };

    return () -> new DataSourceStream<MappedSqlTuple>() {
      private DataSourceStream<SQLResultTuple> source = resultTupleFactory.create();
      @Override
      public void close() throws IOException {
        source.close();
      }

      @Override
      public boolean hasNext() {
        return source.hasNext();
      }

      @Override
      public MappedSqlTuple next() {
        return new MappedSqlTuple(source.next(), map);
      }
    };

    //generate resources using the Connection
    //return () -> new SqlToDocStream(factory, resultFactory, map);
  }


  public DataSourceStream<MappedSqlTuple> getAllData() throws IOException {
    StreamCollection<MappedSqlTuple> result = new StreamCollection<>();
    for (D2rMap map : maps) {
      result.add(createStreamFactory(map, dataSourceManager.getDataSource(), new ArrayList<>()));
    }
    result.start();
    return result;
  }

  public DataSourceManager getDataSourceManager() {
    return dataSourceManager;
  }

  public D2rMap getMapById(String id) {
    return idToMap.get(id);
  }

  public List<D2rMap> getMaps() {
    return maps;
  }

  public PrefixMapping getNamespacePrefixMapper() {
    return namespacePrefixMapper;
  }

  public HashMap<String, Namespace> getNamespaces() {
    return namespaces;
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
}