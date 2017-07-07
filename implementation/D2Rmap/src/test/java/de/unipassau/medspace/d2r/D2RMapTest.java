package de.unipassau.medspace.d2r;

import com.mockrunner.jdbc.BasicJDBCTestCaseAdapter;
import com.mockrunner.jdbc.StatementResultSetHandler;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockResultSet;
import de.unipassau.medspace.common.SQL.SelectStatement;
import de.unipassau.medspace.d2r.bridge.DatatypePropertyBridge;
import de.unipassau.medspace.d2r.bridge.ObjectPropertyBridge;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.common.SQL.SqlStream;
import de.unipassau.medspace.common.SQL.SQLResultTuple;
import org.apache.log4j.Logger;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by David Goeth on 07.06.2017.
 */
public class D2RMapTest extends BasicJDBCTestCaseAdapter {

  /** log4j logger used for this class */
  private static Logger log = Logger.getLogger(D2RMapTest.class);

  @Test
  public void testAddConditionStatements() {

    String query = "SELECT * FROM languages\nORDER BY languages.name;";
    query = query.toUpperCase();
    List<String> conditionList = new ArrayList<>(Arrays.asList("LANGUAGES.ID = 12", "LANGUAGES.NAME LIKE 'ENGLISH'"));
    String queryResult = SelectStatement.addConditionStatements(query, conditionList);

    String expectedResult = "SELECT * FROM LANGUAGES WHERE LANGUAGES.ID = 12 AND LANGUAGES.NAME LIKE 'ENGLISH' ORDER BY LANGUAGES.NAME;";

    assert queryResult.equals(expectedResult);

  }

  @Test
  public void getAllDataTest() throws SQLException, IOException, D2RException {
    D2rMap map = createTestMap();
    DataSource dataSource = createDataSource();
    map.init(dataSource, Arrays.asList(map));

    SelectStatement statement = map.getQuery();

    try (SqlStream stream = statement.execute(dataSource)) {
      for (SQLResultTuple tuple : stream) {
        System.out.println(tuple.toString());
      }
    }

  }

  private DataSource createDataSource() {
    MockConnection connection = getJDBCMockObjectFactory().getMockConnection();
    StatementResultSetHandler statementHandler =
        connection.getStatementResultSetHandler();
    MockResultSet result = statementHandler.createResultSet();
    result.addColumn("name");
    List<Object> values = Arrays.asList("ENGLISH", "GERMAN", "FRENCH");
    for (Object obj : values) {
      result.addRow(Arrays.asList(obj));
    }
    //statementHandler.prepareGlobalResultSet(result);
    statementHandler.prepareResultSet("SELECT * from language", result);
    return new DataSourceMock(connection);
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

  private D2rMap createTestMap() {
    D2rMap map = new D2rMap();
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

  private static class DataSourceMock implements DataSource {

    private Connection connection;

    public DataSourceMock(Connection connection) {
      this.connection = connection;
    }

    @Override
    public Connection getConnection() throws SQLException {
      return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
      return connection;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
      return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
      return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
      return 0;
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return null;
    }
  }
}