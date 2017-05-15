package de.fuberlin.wiwiss.d2r;

import de.unipassau.medspace.util.XmlUtil;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.*;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URISyntaxException;
/**
 * Created by necon on 09.05.2017.
 */
public class D2RProcessorTest {

  /** log4j logger used for this class */
  private static Logger log = Logger.getLogger(D2rProcessor.class);

  private D2rProcessor processor = new D2rProcessor();

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

  public static Document parseFromFile(String filename, boolean useHelper) throws SAXException, ParserConfigurationException, URISyntaxException, IOException {
    String[] schemaFiles = createTestSchemas(useHelper);
    return XmlUtil.parseFromFile(filename, schemaFiles);
  }

  public static Document parseFromString(String content, boolean useHelper) throws SAXException, ParserConfigurationException, URISyntaxException, IOException {
    String[] schemaFiles = createTestSchemas(useHelper);
    return XmlUtil.parseFromString(content, schemaFiles);
  }
}