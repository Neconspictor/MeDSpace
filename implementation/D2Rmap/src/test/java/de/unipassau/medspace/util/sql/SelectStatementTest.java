package de.unipassau.medspace.util.sql;

import de.fuberlin.wiwiss.d2r.Configuration;
import de.fuberlin.wiwiss.d2r.DataSourceManager;
import de.fuberlin.wiwiss.d2r.exception.D2RException;
import de.unipassau.medspace.util.SqlUtil;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by David Goeth on 11.06.2017.
 */
public class SelectStatementTest {

  @Test
  public void testParse() throws D2RException, SQLException {

    String query = "SELECT * FROM LANGUAGE WHERE LANGUAGE.name LIKE '%' ORDER BY LANGUAGE.NAME;";
    Configuration config = new Configuration();
    config.setJdbc("jdbc:mysql://localhost:3306/medspace?useSSL=false");
    config.setDatabaseUsername("medspace_client");
    config.setDatabasePassword("k4N!rT");
    config.setMaxConnections(10);

    DataSource dataSource = new DataSourceManager(config).getDataSource();
    SelectStatement stmt = new SelectStatement(query, dataSource);

    SqlUtil.SQLQueryResult result = stmt.execute(dataSource);
    try {
      printQueryResult(result);
      result.close();
      result = stmt.execute(dataSource);
      printQueryResult(result);
    } catch(SQLException e) {
      throw e;
    } finally {
      result.close();
    }
  }

  private void printQueryResult(SqlUtil.SQLQueryResult result) throws SQLException {
    ResultSet set = result.getResultSet();
    while(set.next()) {
      for (int i = 1; i <= set.getMetaData().getColumnCount(); ++i) {
        System.out.print(set.getString(i) + " ");
      }
      System.out.println();
    }
  }
}