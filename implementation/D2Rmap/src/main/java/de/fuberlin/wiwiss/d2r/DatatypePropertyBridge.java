package de.fuberlin.wiwiss.d2r;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import java.util.*;

/**
 * D2R bridge for DataProperties (Literals).
 * <BR>History: 01-15-2003   : Initial version of this class.
 * <BR>History: 09-25-2003   : Changed for Jena2.
 * @author Chris Bizer chris@bizer.de
 * @version V0.2
 */
public class DatatypePropertyBridge extends Bridge {

  @Override
  protected RDFNode getValue(D2rProcessor processor, ResultResource tuple) {
    // Generate property value
    Object value = null;
    Literal literal = null;
    Model model = processor.getModel();
    if (getColumn() != null) {
      value = tuple.getValueByColmnName(getColumn());
      // translate value
      if (getTranslation() != null) {
        HashMap<String, TranslationTable> tables = processor.getTranslationTables();
        // Warnung wenn Table nicht gefunden!!!! TODO: Maybe log a warning?
        TranslationTable table = tables.get(getTranslation());
        value = table.get(value);
      }
    } else if (getPattern() != null) {
      // pattern
      value = D2rUtil.parsePattern(getPattern(),
          D2R.DELIMINATOR, tuple);
    } else {
      value = getValue();
    }

    // The lang tag specifies indirectly the dataType (rdf:langeString)
    // Thus the lang tag has a higher priority than the dataType tag
    if ((value != null) && (getXmlLang() != null)) {
      literal = model.createLiteral((String) value, getXmlLang());
    } else if ((value != null) && (getDataType() != null)) {
      // if no lang tag is set but the dataType tag create a typed literal
      String dataType = processor.getNormalizedURI(getDataType());
      RDFDatatype rdfType = TypeMapper.getInstance().getSafeTypeByName(dataType);
      literal = model.createTypedLiteral(value, rdfType);
    }  else {
      // no lang tag and dataType set; assume xsd:string is the data type
      literal = model.createTypedLiteral(value);
    }
    return literal;
  }
}