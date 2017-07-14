package de.unipassau.medspace.common.sql;

import de.unipassau.medspace.common.SQL.SelectStatement;
import de.unipassau.medspace.d2r.config.Configuration;
import de.unipassau.medspace.common.SQL.HikariDataSourceManager;
import de.unipassau.medspace.d2r.exception.D2RException;
import de.unipassau.medspace.common.SQL.SqlStream;
import de.unipassau.medspace.common.SQL.SQLResultTuple;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

/**
 * Created by David Goeth on 11.06.2017.
 */
public class SelectStatementTest {

  @Test
  public void testParse() throws D2RException, SQLException, IOException, URISyntaxException {

    /*String query = "SELECT * FROM LANGUAGE WHERE LANGUAGE.name LIKE '%' ORDER BY LANGUAGE.NAME;";
    URI jdbcURI = new URI("jdbc:mysql://localhost:3306/medspace?useSSL=false");

    DataSource dataSource = new HikariDataSourceManager(
        jdbcURI,
        "medspace_client",
        "k4N!rT",
        10,
        null).getDataSource();

    SelectStatement stmt = new SelectStatement(query, dataSource);

    try (SqlStream result = stmt.execute(dataSource)){
      printQueryResult(result);
    } catch(IOException e) {
      throw e;
    }*/
  }

  private void printQueryResult(SqlStream result) throws SQLException, IOException {
    for (SQLResultTuple tuple : result) {
      for (int i = 0; i < tuple.getColumnCount(); ++i) {
        System.out.print(tuple.getValue(i) + " ");
      }
    }

    System.out.println();
  }
}