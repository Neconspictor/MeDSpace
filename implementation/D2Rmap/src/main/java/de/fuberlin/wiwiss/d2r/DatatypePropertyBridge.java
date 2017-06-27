package de.fuberlin.wiwiss.d2r;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

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
  protected RDFNode getValue(ResultResource tuple, URINormalizer normalizer) {
    // Generate property value
    Literal literal = null;
    String value = D2rUtil.parsePattern(getPattern(),
        D2R.DELIMINATOR, tuple);

    // The lang tag specifies indirectly the dataType (rdf:langeString)
    // Thus the lang tag has a higher priority than the dataType tag
    if ((value != null) && (getXmlLang() != null)) {
      literal = ResourceFactory.createLangLiteral((String) value, getXmlLang());
    } else if ((value != null) && (getDataType() != null)) {
      // if no lang tag is set but the dataType tag create a typed literal
      String dataType = normalizer.normalize(getDataType());
      RDFDatatype rdfType = TypeMapper.getInstance().getSafeTypeByName(dataType);
      literal = ResourceFactory.createTypedLiteral(value, rdfType);
    }  else {
      // no lang tag and dataType set; assume xsd:string is the data type
      literal = ResourceFactory.createTypedLiteral(value);
    }
    return literal;
  }
}