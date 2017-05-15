package de.fuberlin.wiwiss.d2r;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Created by necon on 09.05.2017.
 */
public class D2RProcessorTest {

  /** log4j logger used for this class */
  private static Logger log = Logger.getLogger(D2rProcessor.class);

  private D2rProcessor processor = new D2rProcessor();

  @Test//(expected=D2RException.class)
  public void testNeededTagDBConnection() throws Exception {
    //Document doc = loadXMLFromString("<?xml version=\"1.0\"?>" +
    //    "<!DOCTYPE something SYSTEM \"test_dtd.dtd\">" +
    //    "<test>test</test>");
    //processor.readMap(doc);

    /*SchemaFactory schemaFactory = SchemaFactory
        .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    URL resource = D2RProcessorTest.class.getResource("/Medspace_D2Rmap.xsd");
    final String xsd = Paths.get(resource.toURI()).toString();
    Schema schema = schemaFactory.newSchema(new File(xsd));
    Validator validator = schema.newValidator();
    validator.validate(new StreamSource("./examples/medspace/medspace.d2r.xml"));*/

    //Document doc = parseFromFile("./examples/medspace/medspace.d2r.xml", true);
    Document doc = parseFromFile("/testSchema.xml", true);
  }

  private static Document parseFromFile(String filename, boolean useHelper) throws IOException, SAXException, ParserConfigurationException, URISyntaxException {
    InputSource is = getFromFile(filename);
    return parseFromSource(is, useHelper);
  }

  private static Document parseFromSource(InputSource source, boolean useHelper) throws SAXException, ParserConfigurationException, URISyntaxException, IOException {
    String[] schemaFiles;
    if (useHelper)
      schemaFiles = new String[] {"/Medspace_D2Rmap.xsd", "/helper.xsd"};
    else
      schemaFiles = new String[] {"/Medspace_D2Rmap.xsd"};

    DocumentBuilder builder = createDocBuilder(schemaFiles);
    return builder.parse(source);
  }

  private static Document parseFromString(String content, boolean useHelper) throws SAXException, ParserConfigurationException, IOException, URISyntaxException {
    InputSource is = getFromString(content);
    return parseFromSource(is, useHelper);
  }




  // helper functions

  private static DocumentBuilder createDocBuilder(String[] schemaFilenames) throws URISyntaxException, SAXException, ParserConfigurationException {
    final Schema schema = createSchema(schemaFilenames);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(false); // validation is only done for DTDs, but we use only XSDs. So we have to disable it.
    factory.setNamespaceAware(true);
    factory.setSchema(schema);
    DocumentBuilder builder = factory.newDocumentBuilder();

    // error handler for not well defined xml files
    builder.setErrorHandler(new ErrorHandler() {
      @Override
      public void warning(SAXParseException exception) throws SAXException {
        log.warn(exception);
      }

      @Override
      public void error(SAXParseException exception) throws SAXException {
        throw new SAXException(exception);
      }

      @Override
      public void fatalError(SAXParseException exception) throws SAXException {
        throw new SAXException(exception);
      }
    });

    return builder;
  }

  private static Schema createSchema(String[] schemaFilenames) throws SAXException, URISyntaxException {
    final Source[] schemaSources = new Source[schemaFilenames.length];

    // create for each schema file name a source
    for (int i = 0; i < schemaFilenames.length; ++i) {
      String filename = schemaFilenames[i];
      if (isResource(filename)) {
        URL resource = D2RProcessorTest.class.getResource(filename);
        filename = Paths.get(resource.toURI()).toString();
      }
      schemaSources[i] = new StreamSource(filename);
    }

    // Init schema factory; Note  - we only use XML schemas (XSD)
    final SchemaFactory schemaFactory = SchemaFactory
        .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    // create the schema as the compound of the schema sources
    return schemaFactory.newSchema(schemaSources);
  }

  private static boolean isResource(String filename) {
    return filename.startsWith("/") ? true : false;
  }

  private static InputSource getFromFile(String filename) throws FileNotFoundException, URISyntaxException {
    if (isResource(filename)) {
      URL resource = D2RProcessorTest.class.getResource(filename);
      filename = Paths.get(resource.toURI()).toString();
    }
    return new InputSource(new FileReader(filename));
  }

  private static InputSource getFromString(String content) {
    return new InputSource(new StringReader(content));
  }
}