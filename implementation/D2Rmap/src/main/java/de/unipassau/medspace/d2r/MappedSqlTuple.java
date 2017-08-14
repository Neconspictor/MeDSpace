package de.unipassau.medspace.d2r;

import de.unipassau.medspace.common.SQL.SQLResultTuple;

/**
 * A mapped sql tuple assigns a {@link SQLResultTuple} to a {@link D2rMap}.
 * This mapping can be used to specify that the sql tuple can be converted
 * to rdf triples using the assigned D2rMap.
 */
public class MappedSqlTuple {

  /**
   * The sql tuple.
   */
  private SQLResultTuple source;

  /**
   * The assigned D2rMap.
   */
  private D2rMap map;

  /**
   * Creates a new MappedSqlTuple
   * @param source The sql tuple
   * @param map The D2rMap to assign to the sql tuple.
   */
  public MappedSqlTuple(SQLResultTuple source, D2rMap map) {
    this.source = source;
    this.map = map;
  }

  /**
   * Provides the D2rMap the sql tuple is assigned to.
   * @return
   */
  public D2rMap getMap() {
    return map;
  }

  /**
   * Provides the sql tuple that is assigned to the D2rMap.
   * @return
   */
  public SQLResultTuple getSource() {
    return source;
  }
}