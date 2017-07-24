package de.unipassau.medspace.d2r;

import de.unipassau.medspace.common.SQL.SQLResultTuple;

/**
 * Created by David Goeth on 24.07.2017.
 */
public class MappedSqlTuple {

  private SQLResultTuple source;
  private D2rMap map;

  public MappedSqlTuple(SQLResultTuple source, D2rMap map) {
    this.source = source;
    this.map = map;
  }

  public D2rMap getMap() {
    return map;
  }

  public SQLResultTuple getSource() {
    return source;
  }
}