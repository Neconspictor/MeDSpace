package de.unipassau.medspace.util;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Created by David Goeth on 15.05.2017.
 */
public class XmlUtil {

  private static Logger log = Logger.getLogger(XmlUtil.class.getName());

  public static InputSource getFromFile(String filename) throws FileNotFoundException {
    if (FileUtil.isResource(filename)) {
      URL resource = Class.class.getResource(filename);
      filename = resource.getFile();
    }
    return new InputSource(new FileReader(filename));
  }

  public static InputSource getFromString(String content) {
    return new InputSource(new StringReader(content));
  }

  public static Document parseFromFile(String filename, String[] schemaFiles) throws IOException, SAXException, ParserConfigurationException, URISyntaxException {
    InputSource is = getFromFile(filename);
    return parseFromSource(is, schemaFiles);
  }

  public static Document parseFromSource(InputSource source, String[] schemaFiles) throws SAXException, ParserConfigurationException, URISyntaxException, IOException {
    DocumentBuilder builder = createDocBuilder(schemaFiles);
    return builder.parse(source);
  }

  public static Document parseFromString(String content, String[] schemaFiles) throws SAXException, ParserConfigurationException, IOException, URISyntaxException {
    InputSource is = getFromString(content);
    return parseFromSource(is, schemaFiles);
  }

  public static DocumentBuilder createDocBuilder(String[] schemaFilenames) throws URISyntaxException, SAXException, ParserConfigurationException {
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

  public static Schema createSchema(String[] schemaFilenames) throws SAXException, URISyntaxException {
    final Source[] schemaSources = new Source[schemaFilenames.length];

    // create for each schema file name a source
    for (int i = 0; i < schemaFilenames.length; ++i) {
      String filename = schemaFilenames[i];
      if (FileUtil.isResource(filename)) {
        URL resource = Class.class.getResource(filename);
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
}