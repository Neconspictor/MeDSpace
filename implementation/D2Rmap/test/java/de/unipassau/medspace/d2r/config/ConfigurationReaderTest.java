package de.unipassau.medspace.d2r.config;

import de.unipassau.medspace.d2r.D2rProxy;
import de.unipassau.medspace.common.util.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static Logger log = LoggerFactory.getLogger(D2rProxy.class);

  @Test
  public void testReadComplexTypeNamespace() throws SAXException {
    Configuration config = new Configuration();
    config.setNamespaces(new HashMap<>());

    Document doc = XmlUtil.parseFromString("<Namespace prefix=\"test\" namespace=\"http://testnamespace.com\"></Namespace>", null);

    Element elem = (Element) doc.getElementsByTagName("Namespace").item(0);

    ConfigurationReader.readComplexTypeNamespace(config, elem);
  }
}
