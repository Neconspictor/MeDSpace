package de.unipassau.medspace.util.sql;

import de.fuberlin.wiwiss.d2r.Configuration;
import de.fuberlin.wiwiss.d2r.DataSourceManager;
import de.fuberlin.wiwiss.d2r.exception.D2RException;
import de.unipassau.medsapce.SQL.SQLQueryResultStream;
import de.unipassau.medsapce.SQL.SQLResultTuple;
import de.unipassau.medspace.util.FileUtil;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by David Goeth on 11.06.2017.
 */
public class SelectStatementTest {

  @Test
  public void testParse() throws D2RException, SQLException, IOException {

    String query = "SELECT * FROM LANGUAGE WHERE LANGUAGE.name LIKE '%' ORDER BY LANGUAGE.NAME;";
    Configuration config = new Configuration();
    config.setJdbc("jdbc:mysql://localhost:3306/medspace?useSSL=false");
    config.setDatabaseUsername("medspace_client");
    config.setDatabasePassword("k4N!rT");
    config.setMaxConnections(10);

    DataSource dataSource = new DataSourceManager(config).getDataSource();
    SelectStatement stmt = new SelectStatement(query, dataSource);

    try (SQLQueryResultStream result = stmt.execute(dataSource)){
      printQueryResult(result);
    } catch(IOException e) {
      throw e;
    }
  }

  private void printQueryResult(SQLQueryResultStream result) throws SQLException, IOException {
    for (SQLResultTuple tuple : result) {
      for (int i = 0; i < tuple.getColumnCount(); ++i) {
        System.out.print(tuple.getValue(i) + " ");
      }
    }

    System.out.println();
  }
}
