package de.fuberlin.wiwiss.d2r;

import de.unipassau.medspace.util.XmlUtil;
import org.apache.log4j.Logger;
import org.apache.xerces.dom.ElementImpl;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * Created by David Goeth on 06.06.2017.
 */
public class ConfigurationReaderTest {

  /** log4j logger used for this class */
  private static Logger log = Logger.getLogger(D2rProcessor.class);

  @Test
  public void testReadComplexTypeNamespace() throws SAXException {
    Configuration config = new Configuration();
    config.setNamespaces(new HashMap<>());

    Document doc = XmlUtil.parseFromString("<Namespace prefix=\"test\" namespace=\"http://testnamespace.com\"></Namespace>", null);

    Element elem = (Element) doc.getElementsByTagName("Namespace").item(0);

    ConfigurationReader.readComplexTypeNamespace(config, elem);
  }
}
