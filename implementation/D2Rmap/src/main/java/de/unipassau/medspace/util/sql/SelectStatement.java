package de.unipassau.medspace.util.sql;

import java.util.Vector;

/**
 * Created by David Goeth on 08.06.2017.
 */
public class SelectStatement {
  SelectSpecifier selectSpecifier;
  Vector<String> selectList;
  Vector<String> fromTableReferenceList;
  Vector<String> whereConditionList;
  Vector<String> groupByList;
  Vector<String> havingList;
  UnionSpecifier unionSpecifier;
  Vector<SelectStatement> unionList;
  Vector<String> OrderByList;


  /**
   * The select specifier class is intended to specify if the select statement should return
   * all or only distinct values.
   */
  private static class SelectSpecifier {

    // specifies to select only distinct values
    private boolean distinct;


    public SelectSpecifier(boolean distinct) {
      this.distinct = distinct;
    }

    public String toString() {
      if (distinct) return "DISTINCT ";
      return "ALL ";
    }
  }

  private static class UnionSpecifier {
    private boolean all;

    public UnionSpecifier(boolean all) {
      this.all = all;
    }

    public String toString() {
      if (all) return "ALL ";
      return "";
    }
  }
}