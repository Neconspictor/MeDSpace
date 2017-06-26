package de.unipassau.medsapce.SQL;

import de.unipassau.medspace.util.FileUtil;
import de.unipassau.medspace.util.LookaheadIterator;
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
public class SQLQueryResultStream implements Closeable, Iterable<SQLResultTuple> {
  private ResultSet resultSet;
  private LookaheadIterator<SQLResultTuple> resultSetIterator;
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

    resultSetIterator = new LookaheadIterator<SQLResultTuple>() {
      @Override
      protected SQLResultTuple loadNext() {
        try {
          if (!resultSet.next()) {
            return null;
          }
          return SQLResultTuple.create(resultSet);
        } catch (SQLException e) {
          throw new IllegalStateException("Error reading from datasource", e);
        }
      }
    };

    // now the stream is successfully initialized
    closed = false;

    if (log.isDebugEnabled())
      log.debug("Opened SQLQueryResultStream.");
  }

  public void close() throws IOException {
    if (closed) throw new IOException("SQLQueryResultStream already closed!");
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

  public int getColumnCount() {
    return numColumns;
  }


  @Override
  public Iterator<SQLResultTuple> iterator() {
    return resultSetIterator;
  }

  @Override
  public void forEach(Consumer<? super SQLResultTuple> action) {
    action.accept(resultSetIterator.next());
  }

  /**
   * Not implemented
   * @throws
   */
  @Override
  public Spliterator<SQLResultTuple> spliterator() {
    throw new UnsupportedOperationException("This method isn#T supported!");
  }
}