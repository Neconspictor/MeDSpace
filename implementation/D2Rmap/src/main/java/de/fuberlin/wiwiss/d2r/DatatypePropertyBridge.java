package de.fuberlin.wiwiss.d2r;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.log4j.Logger;

import java.util.HashMap;

/**
 * D2R bridge for DataProperties (Literals).
 * <BR>History: 01-15-2003   : Initial version of this class.
 * <BR>History: 09-25-2003   : Changed for Jena2.
 * @author Chris Bizer chris@bizer.de
 * @version V0.2
 */
public class DatatypePropertyBridge extends Bridge {

  @Override
  protected RDFNode getReferredNode(D2rProcessor processor, Model model, ResultInstance tuple) {
    // Generate property value
    String value = null;
    Literal literal = null;
    if (getColumn() != null) {
      value = tuple.getValueByColmnName(getColumn());
      // translate value
      if (getTranslation() != null) {
        HashMap<String, TranslationTable> tables = processor.getTranslationTables();
        // Warnung wenn Table nicht gefunden!!!! TODO: Maybe log a warning?
        TranslationTable table = tables.get(getTranslation());
        value = table.get(value);
      }
    }
    else if (getPattern() != null) {
      // pattern
      value = D2rUtil.parsePattern(getPattern(),
          D2R.DELIMINATOR, tuple);
    }
    else {
      value = getValue();
    }

    if ((value != null) && (getXmlLang() != null)) {
        literal = model.createLiteral(value, getXmlLang());
    }
    else {
      literal = model.createLiteral(value);
    }
      return literal;
  }
}