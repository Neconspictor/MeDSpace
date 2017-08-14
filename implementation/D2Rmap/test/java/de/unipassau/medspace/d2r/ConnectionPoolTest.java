package de.unipassau.medspace.d2r;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by David Goeth on 12.06.2017.
 */
public class ConnectionPoolTest {

  @Test
  public void testConnectionPool() throws SQLException {

   /* DataSourceProperty dataSource;
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:mysql://localhost:3306/medspace?useSSL=false");
    config.setUsername("medspace_client");
    config.setPassword("k4N!rT");

    config.setMaximumPoolSize(10);
    config.setAutoCommit(false);
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    dataSource = new HikariDataSource(config);

    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      PreparedStatement stmt = connection.prepareStatement("Select * from language");
      ResultSet set = stmt.executeQuery();

      while(set.next()) {
        System.out.println(set.getString("name"));
      }
    } catch (SQLException e) {
      if (connection != null)
        connection.rollback();
      throw e;
    }*/
  }
}
