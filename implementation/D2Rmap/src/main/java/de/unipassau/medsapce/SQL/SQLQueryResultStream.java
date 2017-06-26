package de.unipassau.medsapce.SQL;

import de.unipassau.medspace.util.FileUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Created by David Goeth on 26.06.2017.
 */
public class SQLQueryResultStream implements Closeable {
  private ResultSet resultSet;
  private ResultSetIterator resultSetIterator;
  private int numColumns;
  private Statement statement;
  private Connection connection;
  private static Logger log = LogManager.getLogger(SQLQueryResultStream.class);
  private volatile boolean closed;

  /**
   *
   * @param dataSource The sql database to query
   * @param query The query that should be executed on the datasource
   * @param maxRowSize the maximum of tuples that should be get by the stream. Set it to 0 for unlimiting it / getting
   *                   all result tuples
   * @param fetchSize specifies how much tuples should be buffered before requestiong the datasource for more
   *                  result tuples. A greater fetch size performs faster at the expense of memory consumption
   * @throws SQLException thrown if an error occurs while quering the datasource
   */
  public SQLQueryResultStream(DataSource dataSource, String query, int maxRowSize, int fetchSize) throws SQLException {
    assert fetchSize > 0;
    statement =null;
    resultSet = null;
    if (fetchSize > maxRowSize && (maxRowSize != 0))
      fetchSize = maxRowSize;

    // stream isn't yet initialized
    closed = true;

    try {
      connection = dataSource.getConnection();
      connection.setReadOnly(true);
      statement = connection.createStatement();
      resultSet = statement.executeQuery(query);
      statement.setMaxRows(maxRowSize);
      statement.setFetchSize(fetchSize);
      numColumns = resultSet.getMetaData().getColumnCount();
    }catch (SQLException e) {
      FileUtil.closeSilently(statement, true);
      FileUtil.closeSilently(resultSet, true);
      FileUtil.closeSilently(connection, true);
      throw e;
    }

    resultSetIterator = new ResultSetIterator(resultSet);

    // now the stream is successfully initialized
    closed = false;

    if (log.isDebugEnabled())
      log.debug("Opened SQLQueryResultStream.");
  }

  public void close() throws IOException {
    FileUtil.closeSilently(connection, true);
    FileUtil.closeSilently(statement, true);
    FileUtil.closeSilently(resultSet, true);

    // release not needed memory
    connection = null;
    resultSet = null;
    statement = null;

    // stream is now closed
    closed = true;

    if (log.isDebugEnabled())
      log.debug("Closed SQLQueryResultStream.");
  }

  /**
   * Provides the sql result of this sql result stream
   * @return The sql result resultSet of the sql query
   * @throws IllegalStateException thrown if this stream is already closed.
   */
  public ResultSet getResultSet() {
    if (closed) throw new IllegalStateException();
    return resultSet;
  }

  public int getColumnCount() {
    return numColumns;
  }

  public boolean next() {
    return resultSetIterator.next();
  }

  public SQLResultTuple get() {
    return  resultSetIterator.get();
  }

  private static class ResultSetIterator {

    private ResultSet resultSet;
    private boolean hasNext;
    private SQLResultTuple currentTuple;

    ResultSetIterator(ResultSet resultSet) {
      assert resultSet != null;
      this.resultSet = resultSet;
      hasNext = true;
    }

    public boolean next() {
      if (!hasNext) return false;
      try {
        if (resultSet.isClosed()) {
          currentTuple = null;
          return false;
        }
      } catch (SQLException e) {
        log.error(e);
        // TODO close result set
        currentTuple = null;
        hasNext = false;
      }

      try {
        hasNext = resultSet.next();
        if (hasNext)
          currentTuple = SQLResultTuple.create(resultSet);
      } catch (SQLException e) {
        log.error(e);

        // We assume that something is broken and cut the stream just in case
        // Additionally we assume there is no result tuple anymore and thus return null
        hasNext = false;
      }
      return hasNext;
    }

    public SQLResultTuple get() {
      if (!hasNext) currentTuple = null;
      return currentTuple;
    }
  }
}