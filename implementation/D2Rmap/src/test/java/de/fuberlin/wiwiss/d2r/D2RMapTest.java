package de.fuberlin.wiwiss.d2r;

import de.fuberlin.wiwiss.d2r.exception.D2RException;
import de.unipassau.medsapce.SQL.SQLQueryResultStream;
import de.unipassau.medsapce.SQL.SQLResultTuple;
import org.apache.log4j.Logger;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by David Goeth on 07.06.2017.
 */
public class D2RMapTest {

  /** log4j logger used for this class */
  private static Logger log = Logger.getLogger(D2RMapTest.class);

  @Test
  public void testAddConditionStatements() {

    String query = "SELECT * FROM languages\nORDER BY languages.name;";
    query = query.toUpperCase();
    List<String> conditionList = new ArrayList<>(Arrays.asList("LANGUAGES.ID = 12", "LANGUAGES.NAME LIKE 'ENGLISH'"));
    String queryResult = D2RMap.addConditionStatements(query, conditionList);

    String expectedResult = "SELECT * FROM LANGUAGES WHERE LANGUAGES.ID = 12 AND LANGUAGES.NAME LIKE 'ENGLISH' ORDER BY LANGUAGES.NAME;";

    assert queryResult.equals(expectedResult);

  }

  @Test
  public void getAllDataTest() throws SQLException, IOException, D2RException {
    D2RMap map = createTestMap();
    Configuration config = createConfig();
    DataSourceManager manager = new DataSourceManager(config);
    DataSource dataSource = manager.getDataSource();
    map.init(dataSource);

    try (SQLQueryResultStream stream = map.getAllData(dataSource)) {
      for (SQLResultTuple tuple : stream) {
        System.out.println(tuple.toString());
      }
    }

  }

  private Configuration createConfig() {
    Configuration config = new Configuration();
    config.setJdbc("jdbc:mysql://localhost:3306/medspace?useSSL=false");
    config.setDatabaseUsername("medspace_client");
    config.setDatabasePassword("k4N!rT");
    config.setMaxConnections(10);

    // datasource properties
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    config.addDataSourceProperty("useServerPrepStmts", "true");
    config.addDataSourceProperty("useLocalSessionState", "true");
    config.addDataSourceProperty("useLocalTransactionState", "true");
    config.addDataSourceProperty("rewriteBatchedStatements", "true");
    config.addDataSourceProperty("cacheResultSetMetadata", "true");
    config.addDataSourceProperty("cacheServerConfiguration", "true");
    config.addDataSourceProperty("elideSetAutoCommits", "true");
    config.addDataSourceProperty("maintainTimeStats", "false");

    return config;
  }

  private D2RMap createTestMap() {
    D2RMap map = new D2RMap();
    map.setId("language");
    map.setSql("SELECT * FROM language");
    map.addResourceIdColumns("language.name");

    // rdf type
    ObjectPropertyBridge typeBridge = new ObjectPropertyBridge();
    typeBridge.setProperty("rdf:type");
    typeBridge.setPattern("test:language");
    map.addBridge(typeBridge);

    // base uri
    map.setBaseURI("http://localhost/patient_test_namespace/languages#");

    // datatype property
    DatatypePropertyBridge bridge = new DatatypePropertyBridge();
    bridge.setProperty("test:languageName");
    bridge.setPattern("language.name");
    bridge.setDataType("rdf:langString");
    bridge.setXmlLang("en");
    map.addBridge(bridge);

    return map;
  }
}
