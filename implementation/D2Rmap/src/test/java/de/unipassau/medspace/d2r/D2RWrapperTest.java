package de.unipassau.medspace.d2r;

import de.unipassau.medspace.common.util.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.*;

import javax.xml.validation.Schema;
import java.io.IOException;

/**
 * Created by necon on 09.05.2017.
 */
public class D2RWrapperTest {

  /** log4j logger used for this class */
  private static Logger log = LoggerFactory.getLogger(D2rProxy.class);

  @Test//(expected=D2RException.class)
  public void testNeededTagDBConnection() throws Exception {
    Document doc = parseFromFile("/testSchema.xml", true);
  }

  public static String[] createTestSchemas(boolean useHelper) {
    if (useHelper)
      return new String[] {"/Medspace_D2Rmap.xsd", "/helper.xsd"};
    else
      return new String[] {"/Medspace_D2Rmap.xsd"};
  }

  public static Document parseFromFile(String filename, boolean useHelper) throws SAXException, IOException {
    String[] schemaFiles = createTestSchemas(useHelper);
    Schema schema = XmlUtil.createSchema(schemaFiles);
    return XmlUtil.parseFromFile(filename, schema);
  }

  public static Document parseFromString(String content, boolean useHelper) throws SAXException, IOException {
    String[] schemaFiles = createTestSchemas(useHelper);
    Schema schema = XmlUtil.createSchema(schemaFiles);
    return XmlUtil.parseFromString(content, schema);
  }
}