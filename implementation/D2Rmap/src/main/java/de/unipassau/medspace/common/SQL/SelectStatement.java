package de.unipassau.medspace.common.SQL;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import static de.unipassau.medspace.common.SQL.SelectStatement.Clause.*;


/**
 * A SelectStatement represents a sql select statement of a jdbc query and provides the possibility to construct sql
 * queries with additional WHERE conditions.
 */
public class SelectStatement {
  private String beforeWhereConditionStatement;
  private ArrayList<String> columnList;
  private List<String> staticConditionList;
  private String afterWhereConditionStatement;
  private List<String> orderByList;

  /**
   * Defines the order of clauses as they have to occur in a sql select statement.
   */
  private static ArrayList<Clause> querySelectStatementOrder = new ArrayList<>(Arrays.asList(Clause.SELECT, FROM,
      WHERE, Clause.GROUP_BY, Clause.HAVING, Clause.UNION, Clause.ORDER_BY));

  /**
   * Creates a new SelectStatement and validates it on a given datasource.
   * @param query The select statement to create a SelectStatement from.
   * @param dataSource the datasource to test the query on.
   * @throws SQLException thrown if the query is erroneous, isn't a select query or an error occurs while testing
   * the query on the database.
   */
  public SelectStatement(String query, DataSource dataSource) throws SQLException {
    beforeWhereConditionStatement = "";
    columnList = new ArrayList<>();
    staticConditionList = new ArrayList<>();
    afterWhereConditionStatement = "";
    orderByList = new ArrayList<>();
    parse(query, dataSource);
  }

  /**
   * Creates a sql query string representing this select statement.
   * Note, that this SelectStatement won't be affected by the call of this method.
   * @param temporaryConditionList additional WHERE conditions which sould be added to the query.
   * @return The constructed sql select query.
   */
  public String getSqlQuery(List<String> temporaryConditionList) {
    StringBuilder builder = new StringBuilder(beforeWhereConditionStatement);
    final String and = " AND ";
    boolean available = (staticConditionList.size() > 0) || (temporaryConditionList.size() > 0);

    if (available)
      wrapWithSpaces(builder, WHERE.toString());


    for (String condition : staticConditionList) {
      builder.append("(");
      builder.append(condition);
      builder.append(")");
      builder.append(and);
    }

    for (String condition : temporaryConditionList) {
      builder.append("(");
      builder.append(condition);
      builder.append(")");
      builder.append(and);
    }

    if (available) {
      // delete the last and
      builder.delete(builder.length() - and.length(), builder.length());
    }

    builder.append(" ");
    builder.append(afterWhereConditionStatement);

    available = orderByList.size() > 0;

    if (available) {
      wrapWithSpaces(builder, ORDER_BY.toString());
      final String semi = ", ";
      for (String elem : orderByList) {
        builder.append(elem);
        builder.append(semi);
      }
      // delete the last semi
      builder.delete(builder.length() - semi.length(), builder.length());
    }

    builder.append(";");

    return builder.toString();
  }

  @Override
  public String toString() {
    return getSqlQuery(new ArrayList<>());
  }

  /**
   * Provides a substring that begins with the stated split index.
   * @param string The string to split.
   * @param index The split index. Defines the point at that the string should be split after.
   * @return The substring string beginning at the split index.
   */
  private static String getAfter(String string, int index) {
    if ((index <= -1)
        || (index >= string.length())) {
      return "";
    }
    return string.substring(index, string.length());
  }

  /**
   * Provides a substring that begins at index 0 and ends at the given end index. The character at the end index
   * isn't included.
   * @param query The string to split.
   * @param endIndex Defines the end of the substring.
   * @return a substring in the index range [0, endIndex)
   */
  private static String getBefore(String query, int endIndex) {
    if (endIndex == -1) {
      return query;
    } else {
      if (endIndex > query.length())
        endIndex = query.length();
      return query.substring(0, endIndex);
    }
  }

  /**
   * Provides the string index from a sql select statement that specify the end just before a certain {@link Clause}
   * begins. The clause is specified by the given index and the list of Clause arguments.
   * @param query The query to get an index from.
   * @param querySelectStatementOrder Defines the order clauses inside the select statement query.
   * @param index Specifies the position of the Clause inside the list of clauses.
   * @return The index inside the query string just before the specified clause begins.
   */
  private static int getBeforeIndex(String query, List<Clause> querySelectStatementOrder, int index) {
    assert index != -1;
    int splitIndex = -1;
    for (int currentIndex = index; currentIndex != querySelectStatementOrder.size(); ++currentIndex) {
      Clause clause = querySelectStatementOrder.get(currentIndex);
      splitIndex = query.indexOf(clause.toString());
      if (splitIndex != -1) break;
    }
    return splitIndex;
  }

  /**
   * Adds a string to a string builder. But before adding the string it is enclosed by some white spaces.
   * @param builder The string builder to add the string to.
   * @param str The string to wrap with white spaces and add to the string builder.
   */
  private void wrapWithSpaces(StringBuilder builder, String str) {
    builder.append(" ");
    builder.append(str);
    builder.append(" ");
  }

  /**
   * Initializes this SelectStatement with a given select query and validates the query on a given datasource.
   * @param query The query used to initialize this object.
   * @param dataSource The datasource to test the query on.
   * @throws SQLException thrown if the query is erroneous, isn't a select query or an error occurs while testing
   * the query on the database.
   */
  private void parse(String query, DataSource dataSource) throws  SQLException {
    List<Clause> q = querySelectStatementOrder;

    // At first replace all whitespaces by one space token for easier processing
    query = query.replaceAll("\\s+", " ").toUpperCase();


    int beforeWhereIndex = getClauseRangeSplitIndex(query, q.indexOf(WHERE), q.indexOf(GROUP_BY), false);
    int afterWhereIndex = getClauseRangeSplitIndex(query, q.indexOf(GROUP_BY), q.size(), true);
    int orderByIndex = getClauseRangeSplitIndex(query, q.indexOf(ORDER_BY), q.size(), true);

    // if afterWhereIndex equals -1 when orderByIndex is -1, too
    if (orderByIndex == -1) {
      orderByIndex = query.length();
      afterWhereIndex = query.length();
    }

    if (beforeWhereIndex == -1) {
      beforeWhereIndex = query.length();
    }

    String beforeWhereClause = query.substring(0, beforeWhereIndex);
    String whereCondition = query.substring(beforeWhereIndex, afterWhereIndex);
    String afterWhereClause = query.substring(afterWhereIndex, orderByIndex);

    if (!whereCondition.equals("")) {
      whereCondition = whereCondition.replaceFirst(WHERE.toString(), "");
    }

    beforeWhereClause = beforeWhereClause.trim();
    whereCondition = whereCondition.trim();
    afterWhereClause = afterWhereClause.trim();

    beforeWhereConditionStatement = beforeWhereClause;
    afterWhereConditionStatement = afterWhereClause;
    columnList = parseColumns(query, dataSource);
    orderByList = parseOrderBy(query, orderByIndex);


    // save where conditions
    if (!whereCondition.equals("")) {
      staticConditionList.add(whereCondition);
    }

    if (beforeWhereConditionStatement.endsWith(";")) {
      beforeWhereConditionStatement = beforeWhereConditionStatement.substring(0,
          beforeWhereConditionStatement.length() -1);
    }

    if (afterWhereConditionStatement.endsWith(";")) {
      afterWhereConditionStatement = afterWhereConditionStatement.substring(0,
          afterWhereConditionStatement.length() -1);
    }
  }

  /**
   * Parses a sql ORDER BY statement from a select statement query.
   * @param query The select statement query to get the ORDER BY statement from.
   * @param orderByIndex Specifies the beginning of the ORDER BY clause inside the query.
   * @return A list of ORDER BY statements.
   */
  private static List<String> parseOrderBy(String query, int orderByIndex) {
    String orderByString = query.substring(orderByIndex, query.length());
    orderByString = orderByString.replaceFirst(ORDER_BY.toString(), "");
    orderByString = orderByString.trim();
    if (orderByString.endsWith(";"))
      orderByString = orderByString.substring(0, orderByString.length() - 1);
    StringTokenizer tokenizer = new StringTokenizer(orderByString, ",");

    List<String> result = new ArrayList<>();
    while(tokenizer.hasMoreTokens()) {
      result.add(tokenizer.nextToken().trim());
    }
    return result;
  }

  /**
   * Provides the column list before the FROM clause of a select statement query.
   * @param query The select statement query to get the columns before the FROM clause.
   * @param dataSource Used to fetch column names (e.g. if the * operator is used )
   * @return The list of columns
   * @throws SQLException thrown if the query is erroneous, isn't a select query or an error occurs on the datasource.
   */
  private static ArrayList<String> parseColumns(String query, DataSource dataSource) throws SQLException {
    if (!query.contains(SELECT.toString())) {
      throw new SQLException("Query doesn't contain a SELECT clause: " + query);
    }

    List<Clause> q = querySelectStatementOrder;
    int beforeSelectIndex = getClauseRangeSplitIndex(query, 0, q.indexOf(FROM), false);
    int afterSelectIndex = getClauseRangeSplitIndex(query, q.indexOf(FROM), q.size(), true);

    assert beforeSelectIndex != -1;
    assert afterSelectIndex != -1;

    String selectString = query.substring(beforeSelectIndex, afterSelectIndex);
    selectString = selectString.replaceFirst(SELECT.toString(), "");

    StringTokenizer tokenizer = new StringTokenizer(selectString, ",");

    ArrayList<String> result = new ArrayList<>();
    String firstToken = tokenizer.nextToken().trim();
    if (firstToken.equals("*")) {
      return fetchColumnNames(query, dataSource);
    }


    firstToken = firstToken.replaceFirst(" .*", "");
    result.add(firstToken);

    while(tokenizer.hasMoreTokens()) {
      String column = tokenizer.nextToken().trim();
      column = column.replaceFirst(" .*", "");
      result.add(column);
    }
    return result;
  }

  /**
   * Fetches the column names of a select query from a given datasource.
   * @param query The select query to use.
   * @param dataSource The datasource to execute the query on.
   * @return A list of column names used in the select query.
   * @throws SQLException thrown if the query is erroneous, isn't a select query or an error occurs on the datasource.
   */
  private static ArrayList<String> fetchColumnNames(String query, DataSource dataSource) throws SQLException {
    ArrayList<String> result = new ArrayList<>();
    try(Connection con = dataSource.getConnection()) {
      con.setReadOnly(true); // assure that we don't modify any data accidentally.
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setMaxRows(1); // we want fetch only meta data, so we don't bother about the content
      ResultSet set = stmt.executeQuery();
      ResultSetMetaData metaData = set.getMetaData();
      for (int i = 1; i <= metaData.getColumnCount(); ++i) {
        String column = metaData.getColumnName(i);
        column = column.replaceFirst(" .*", "");
        result.add(column.toUpperCase());
      }
    }

    return result;
  }

  /**
   * Provides the index inside a query string from a range of clauses, specified by a start and end index.
   * The getFirst parameter specifies, if the string index of the first occurence of any clause from the clause range
   * should be returned. If getFirst is set to false, than the last occurence of any clause of the clause range is
   * searched and returned will be the string index before this found clause begins.
   * @param query The Select statement query to get the index from.
   * @param startClauseIndex Specifies the starting index for the clause range.
   * @param endClauseIndex Specifies the ending index for the clause range.
   * @param getFirst if set to true, the first occurrence of any clause inside the clause range is searched. Otherwise
   *                 the last occurrence is searched.
   * @return
   */
  private static int getClauseRangeSplitIndex(String query, int startClauseIndex, int endClauseIndex, boolean getFirst) {
    assert startClauseIndex >= 0;
    assert endClauseIndex >= 0;
    assert endClauseIndex <= querySelectStatementOrder.size();

    int splitIndex = -1;
    for (int currentIndex = startClauseIndex; currentIndex != endClauseIndex; ++currentIndex) {
      String clause = querySelectStatementOrder.get(currentIndex).toString();
      int index = query.indexOf(clause);
      splitIndex =  index != -1 ? index : splitIndex ;
      if (splitIndex != -1 && getFirst) {
        break;
      }
    }
    return splitIndex;
  }

  /**
   * Provides an unmodifiable list of the columns used by this select statement.
   * @return An unmodifiable list of the columns
   */
  public List<String> getColumns() {
    return Collections.unmodifiableList(columnList);
  }


  /**
   * Defines all possible statements that can occur in a sql select statement and their occurrence order.
   */
  protected enum Clause {
    SELECT ("SELECT"), FROM("FROM"), WHERE("WHERE"), GROUP_BY("GROUP BY"), HAVING("HAVING"), UNION("UNION"),
    ORDER_BY("ORDER BY");

    /**
     * Specifies the name of the clause as you would use it in a sql query.
     */
    private String name;

    /**
     * Constructs a new Clause base on it's name.
     * @param name The name of the clause.
     */
    Clause(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}