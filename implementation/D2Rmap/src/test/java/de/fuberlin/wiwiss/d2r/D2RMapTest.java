package de.fuberlin.wiwiss.d2r;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by David Goeth on 07.06.2017.
 */
public class D2RMapTest {

  /** log4j logger used for this class */
  private static Logger log = Logger.getLogger(D2RMapTest.class);

  @Test
  public void testAddConditionStatements() {

    String query = "SELECT * FROM languages\nORDER BY languages.name;";
    query = query.toUpperCase();
    List<String> conditionList = new ArrayList<>(Arrays.asList("LANGUAGES.ID = 12", "LANGUAGES.NAME LIKE 'ENGLISH'"));
    String queryResult = D2RMap.addConditionStatements(query, conditionList);

    String expectedResult = "SELECT * FROM LANGUAGES WHERE LANGUAGES.ID = 12 AND LANGUAGES.NAME LIKE 'ENGLISH' ORDER BY LANGUAGES.NAME;";

    assert queryResult.equals(expectedResult);

  }
}
