package de.unipassau.medsapce.SQL;

import de.fuberlin.wiwiss.d2r.URINormalizer;
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
 * Represents a Stream of SQL tuples created from a SQL query.
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
   * Creates a new query result stream to a sql datasource.
   * @param params The parameter for initializing the sql query stream.
   * @throws IOException thrown if an error occurs while quering the datasource
   */
  public SQLQueryResultStream(QueryParams params) throws SQLException {
    statement =null;
    resultSet = null;
    int fetchSize = params.fetchSize;
    int maxRowSize = params.maxRowSize;
    if (fetchSize > maxRowSize && (maxRowSize != 0))
      fetchSize = maxRowSize;

    // stream isn't yet initialized
    closed = true;

    try {
      connection = params.dataSource.getConnection();
      connection.setReadOnly(true);
      statement = connection.createStatement();
      resultSet = statement.executeQuery(params.query);
      statement.setMaxRows(maxRowSize);
      statement.setFetchSize(fetchSize);
      numColumns = resultSet.getMetaData().getColumnCount();
    }catch (SQLException e) {
      FileUtil.closeSilently(statement, true);
      FileUtil.closeSilently(resultSet, true);
      FileUtil.closeSilently(connection, true);
      throw new SQLException("Error while quering the datasource", e);
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

  public static QueryParams createDefault(DataSource dataSource, String query) {
    return new QueryParams(dataSource, 10, 0, query);
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

  /**
   * QueryParams holds all params that can be given to a SQLQueryResultStream object.
   *
   * Currently it holds the following parameters: <br>
   * The datasource: See {@link QueryParams#dataSource} <br>
   * The fetch size: See {@link QueryParams#fetchSize} <br>
   * The max row size: See {@link QueryParams#maxRowSize} <br>
   * The query: See {@link QueryParams#query} <br>
   *
   */
  public static class QueryParams {

    /**The sql database to query*/
    private DataSource dataSource;

    /**
     * Specifies how much tuples should be buffered before requestioning the datasource for more
     * result tuples. A greater fetch size performs faster at the expense of memory consumption
     */
    private int fetchSize;

    /**
     * The maximum of tuples that should be provided by the stream. Set it to 0 for unlimiting it / getting
     * all result tuples
     */
    private int maxRowSize;

    /**
     * The query that should be executed on the datasource
     */
    private String query;

    /**
     * Creates a new QueryParams object from a given datasource and a query.
     * The fetch size is automatically set to 10,
     * maxRowSize is automatically set to 0 (means no row limit)
     * @param dataSource The sql database to query
     * @param query The query that should be executed on the datasource
     */
    public QueryParams(DataSource dataSource, String query) {
      this(dataSource, 10, 0, query);
    }

    /**
     * Creates a new QueryParams object from a given datasource and a query
     * The fetch size and max row size get default values
     * @param dataSource The sql {@link QueryParams#dataSource}
     * @param fetchSize The fetch size {@link QueryParams#fetchSize}
     * @param maxRowSize The max row size {@link QueryParams#maxRowSize}
     * @param query The query {@link QueryParams#query}
     */
    public QueryParams(DataSource dataSource, int fetchSize, int maxRowSize, String query) {
      setDataSource(dataSource);
      setFetchSize(fetchSize);
      setMaxRowSize(maxRowSize);
      setQuery(query);
    }

    /**
     * Creates a copy of the given QueryParams object. All members are deep copied, except {@link QueryParams#dataSource}
     * which is shallow copied.
     */
    public QueryParams(QueryParams other) {
      dataSource = other.dataSource;
      fetchSize = other.fetchSize;
      maxRowSize = other.maxRowSize;
      query = other.query;
    }

    /**
     * Setter for the datasource
     * @param dataSource The new {@link QueryParams#dataSource}
     */
    public void setDataSource(DataSource dataSource) {
      assert dataSource != null;
      this.dataSource = dataSource;
    }

    /**
     * Setter for the fetch size
     * @param fetchSize The new fetch size. See {@link QueryParams#fetchSize}
     */
    public void setFetchSize(int fetchSize) {
      if (fetchSize <= 0)
        throw new IllegalArgumentException("fetchSize has to be > 0");
      this.fetchSize = fetchSize;
    }

    /**
     * Setter for the max row size
     * @param maxRowSize The max row size {@link QueryParams#maxRowSize}
     * @throws IllegalArgumentException If the max row size size is smaller zero
     */
    public void setMaxRowSize(int maxRowSize) {
      if (maxRowSize < 0)
        throw new IllegalArgumentException("maxRowSize has to be >= 0");
      this.maxRowSize = maxRowSize;
    }

    /**
     * Setter for the query
     * @param query The query {@link QueryParams#query}
     */
    public void setQuery(String query) {
      assert query != null;
      this.query = query;
    }
  }
}