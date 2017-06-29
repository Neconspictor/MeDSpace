package de.unipassau.medspace.util.sql;

import de.fuberlin.wiwiss.d2r.exception.D2RException;
import de.unipassau.medspace.SQL.SQLQueryResultStream;
import de.unipassau.medspace.util.FileUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import static de.unipassau.medspace.util.sql.SelectStatement.Clause.*;


/**
 * Created by David Goeth on 08.06.2017.
 */
public class SelectStatement {
  private String beforeWhereConditionStatement;
  private ArrayList<String> columnList;
  private List<String> staticConditionList;
  private List<String> temporaryConditionList;
  private String afterWhereConditionStatement;
  private List<String> orderByList;

  private static ArrayList<Clause> querySelectStatementOrder = new ArrayList(Arrays.asList(Clause.SELECT, FROM,
      WHERE, Clause.GROUP_BY, Clause.HAVING, Clause.UNION, Clause.ORDER_BY));

  public SelectStatement(String query, DataSource dataSource) throws SQLException, D2RException {
    beforeWhereConditionStatement = "";
    columnList = new ArrayList<>();
    staticConditionList = new ArrayList<>();
    temporaryConditionList = new ArrayList<>();
    afterWhereConditionStatement = "";
    orderByList = new ArrayList<>();
    parse(query, dataSource);
  }

  public SQLQueryResultStream execute(DataSource dataSource) throws SQLException {
    SQLQueryResultStream result = null;
    try {
      String query = toString();
      result = new SQLQueryResultStream(new SQLQueryResultStream.QueryParams(dataSource, query));
    } catch (SQLException e) {
      FileUtil.closeSilently(result);
      throw e;
    }
    return result;
  }

  public String toString() {
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

  private void wrapWithSpaces(StringBuilder builder, String str) {
    builder.append(" ");
    builder.append(str);
    builder.append(" ");
  }

  private void parse(String query, DataSource dataSource) throws D2RException, SQLException {
    if (query.contains(Clause.UNION.toString())) {
      throw new D2RException("UNION clause is not supported by D2rMapper for the select statement!");
    }
    ;
    String whereStr = WHERE.toString();
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

  private static Vector<String> parseOrderBy(String query, int orderByIndex) {
    String orderByString = query.substring(orderByIndex, query.length());
    orderByString = orderByString.replaceFirst(ORDER_BY.toString(), "");
    orderByString = orderByString.trim();
    if (orderByString.endsWith(";"))
      orderByString = orderByString.substring(0, orderByString.length() - 1);
    StringTokenizer tokenizer = new StringTokenizer(orderByString, ",");

    Vector<String> result = new Vector<>();
    while(tokenizer.hasMoreTokens()) {
      result.add(tokenizer.nextToken().trim());
    }
    return result;
  }

  private static ArrayList<String> parseColumns(String query, DataSource dataSource) throws D2RException, SQLException {
    if (!query.contains(SELECT.toString())) {
      throw new D2RException("Query doesn't contain a SELECT clause: " + query);
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

  private static ArrayList<String> fetchColumnNames(String query, DataSource dataSource) throws SQLException {
    ArrayList<String> result = new ArrayList<>();
    try(Connection con = dataSource.getConnection()) {
      con.setReadOnly(true);
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setMaxRows(1); // we want fetch only meta data, so we don't bother about the content
      ResultSet set = stmt.executeQuery();
      ResultSetMetaData metaData = set.getMetaData();
      for (int i = 1; i <= metaData.getColumnCount(); ++i) {
        String column = metaData.getColumnName(i);
        column = column.replaceFirst(" .*", "");
        result.add(column.toUpperCase());
      }
      //con.commit();
    }

    return result;
  }

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

  public void addTemporaryCondition(String condition) {
    temporaryConditionList.add(condition);
  }

  public void reset() {
    temporaryConditionList.clear();
  }

  /**
   * Provides a unmodifiable list of the columns used by this select statement.
   * @return A unmodifiable list of the columns
   */
  public List<String> getColumns() {
    return Collections.unmodifiableList(columnList);
  }

  protected enum Clause {
    SELECT ("SELECT"), FROM("FROM"), WHERE("WHERE"), GROUP_BY("GROUP BY"), HAVING("HAVING"), UNION("UNION"),
    ORDER_BY("ORDER BY");

    private String description;

    Clause(String description) {
      this.description = description;
    }

    public String toString() {
      return description;
    }
  }
}