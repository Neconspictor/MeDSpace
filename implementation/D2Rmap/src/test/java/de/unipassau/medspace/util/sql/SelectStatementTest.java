package de.unipassau.medspace.util.sql;

import de.fuberlin.wiwiss.d2r.exception.D2RException;
import org.junit.Test;

/**
 * Created by David Goeth on 11.06.2017.
 */
public class SelectStatementTest {

  @Test
  public void testParse() throws D2RException {

    String query = "SELECT * FROM LANGUAGES WHERE LANGUAGES.ID < 100 ORDER BY LANGUAGES.ID, LANGUAGES.NAME, ANOTHER_COLUMN   ;     ";
    SelectStatement stmt = SelectStatement.parse(query);
  }
}
