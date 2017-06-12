package de.unipassau.medspace.util.sql;

import de.fuberlin.wiwiss.d2r.exception.D2RException;
import de.unipassau.medspace.util.SqlUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;

import static de.unipassau.medspace.util.sql.SelectStatement.Clause.*;


/**
 * Created by David Goeth on 08.06.2017.
 */
public class SelectStatement {
  String beforeWhereConditionStatement;
  Vector<String> columnList;
  Vector<String> whereConditionList;
  String afterWhereConditionStatement;
  Vector<String> orderByList;

  private static Vector<Clause> querySelectStatementOrder = new Vector(Arrays.asList(Clause.SELECT, FROM,
      WHERE, Clause.GROUP_BY, Clause.HAVING, Clause.UNION, Clause.ORDER_BY));

  public SelectStatement(String query, DataSource dataSource) throws SQLException, D2RException {
    beforeWhereConditionStatement = "";
    columnList = new Vector<>();
    whereConditionList = new Vector<>();
    afterWhereConditionStatement = "";
    orderByList = new Vector<>();
    parse(query, dataSource);
  }

  public SqlUtil.SQLQueryResult execute(DataSource dataSource) throws SQLException {
    SqlUtil.SQLQueryResult result = null;
    try {
      String query = toString();
      result = SqlUtil.executeQuery(dataSource, query, 0, 10);
    } catch (SQLException e) {
      if (result != null) result.close();
      throw e;
    }
    return result;
  }

  private void parse(String query, DataSource dataSource) throws D2RException, SQLException {
    if (query.contains(Clause.UNION.toString())) {
      throw new D2RException("UNION clause is not supported by D2RMap for the select statement!");
    }
    ;
    String whereStr = WHERE.toString();
    Vector<Clause> q = querySelectStatementOrder;

    // At first replace all whitespaces by one space token for easier processing
    query = query.replaceAll("\\s+", " ").toUpperCase();


    int beforeWhereIndex = getClauseRangeSplitIndex(query, 0, q.indexOf(GROUP_BY), false);
    int afterWhereIndex = getClauseRangeSplitIndex(query, q.indexOf(GROUP_BY), q.size(), true);
    int orderByIndex = getClauseRangeSplitIndex(query, q.indexOf(ORDER_BY), q.size(), true);

    // if afterWhereIndex equals -1 when orderByIndex is -1, too
    if (orderByIndex == -1) {
      orderByIndex = query.length();
      afterWhereIndex = query.length();
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
    if (whereCondition != null) {
      whereConditionList.add(whereCondition);
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

  private static Vector<String> parseColumns(String query, DataSource dataSource) throws D2RException, SQLException {
    if (!query.contains(SELECT.toString())) {
      throw new D2RException("Query doesn't contain a SELECT clause: " + query);
    }

    Vector<Clause> q = querySelectStatementOrder;
    int beforeSelectIndex = getClauseRangeSplitIndex(query, 0, q.indexOf(FROM), false);
    int afterSelectIndex = getClauseRangeSplitIndex(query, q.indexOf(FROM), q.size(), true);

    assert beforeSelectIndex != -1;
    assert afterSelectIndex != -1;

    String selectString = query.substring(beforeSelectIndex, afterSelectIndex);
    selectString = selectString.replaceFirst(SELECT.toString(), "");

    StringTokenizer tokenizer = new StringTokenizer(selectString, ",");

    Vector<String> result = new Vector<>();
    String firstToken = tokenizer.nextToken().trim();
    if (firstToken.equals("*")) {
      return fetchColumnNames(query, dataSource);
    } else {
      result.add(firstToken);
    }

    while(tokenizer.hasMoreTokens()) {
      result.add(tokenizer.nextToken().trim());
    }
    return result;

  }

  private static Vector<String> fetchColumnNames(String query, DataSource dataSource) throws SQLException {
    Vector<String> result = new Vector<>();
    try(Connection con = dataSource.getConnection()) {
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setMaxRows(1); // we want fetch only meta data, so we don't bother about the content
      ResultSet set = stmt.executeQuery();
      ResultSetMetaData metaData = set.getMetaData();
      for (int i = 1; i <= metaData.getColumnCount(); ++i) {
        String column = metaData.getColumnName(i);
        result.add(column.toUpperCase());
      }
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
      splitIndex = query.indexOf(clause);
      if (splitIndex != -1 && getFirst) break;
    }
    return splitIndex;
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