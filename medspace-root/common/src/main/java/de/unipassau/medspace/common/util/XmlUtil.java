package de.unipassau.medspace.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing XML and XSD files.
 */
public class XmlUtil {

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(XmlUtil.class.getName());

  /**
   * Creates a document builder for parsing xml files using a given schema.
   * The builder is configured so that it will throw an SAXException if an error
   * occurs while parsing whereas warnings will be logged using the XmlUtil logger.
   * <p>Note: Is is assumed, that <b>schema</b> is a valid xml schema. It is recommened to use
   * {@link SchemaFactory} for creating the schema.
   * </p>
   * <p>Note: The schema is used for modern schema languages such as W3C Schema (XSD) or RELAX NG
   * and cannot be used for DTDs.
   * </p>
   * @param schema The Schema for XML schema validation. If this parameter is null, no validation
   *              will be done by the parser
   * @return A document builder for pasring xml files
   */
  public static DocumentBuilder createDocBuilder(Schema schema) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // Validation is only done for DTDs, but we use only XSDs. So we have to disable it.
    factory.setValidating(false);
    // We use namespaces - the builder should handle them
    factory.setNamespaceAware(true);
    // The builder validates the xml by this schema. If the schema is null, no schema will be used
    factory.setSchema(schema);

    DocumentBuilder builder;
    try {
      builder = factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("factory isn't properly configured. Fix that bug!", e);
    }

    // Now, builder should never be null; but to assure it.
    assert builder != null;

    // error handler for not well defined xml files
    // we want to pass warnings, but errrors should throw an exception
    builder.setErrorHandler(new ErrorHandler() {
      @Override
      public void warning(SAXParseException exception) throws SAXException {
        log.warn("", exception);
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

  /**
   * Creates a compound XSD schema from a set of specified XSD schema files.
   * @param schemaFilenames the file or resource names of the XSD schemas.
   * @return The compound schema. The returned schema will always be valid and not null.
   * @throws NullPointerException If <b>schemaFilenames</b> is <b>null</b>
   * @throws SAXException If no compound XSD schema could be created.
   * @throws IOException if any io error occurs.
   */
  public static Schema createSchema(String[] schemaFilenames) throws SAXException, IOException {

    if (schemaFilenames == null) throw new NullPointerException("schemaFilenames is null!");

    final Source[] schemaSources = new Source[schemaFilenames.length];

    List<TempFile> tempFiles = new ArrayList<>();

    // createDoc for each schema file name a source
    for (int i = 0; i < schemaFilenames.length; ++i) {
      String filename = schemaFilenames[i];
      if (FileUtil.isResource(filename)) {
        TempFile tempFile = FileUtil.createTempFileFromResource(filename,
                                                    filename + "TEMP");
        File file = tempFile.get();
        filename = file.getAbsolutePath();
        tempFiles.add(tempFile);
      }
      schemaSources[i] = new StreamSource(filename);
    }

    // createDoc the schema as the compound of the schema sources
    Schema schema = null;

    try {
      // Init schema factory; Note  - we only use XML schemas (XSD)
      final SchemaFactory schemaFactory = SchemaFactory
          .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);//);

      schema = schemaFactory.newSchema(schemaSources);
    } catch(SAXException e) {
      throw new SAXException("Couldn't createDoc compound schema!", e);
    } finally {
      tempFiles.stream().forEach((TempFile file)-> {
        FileUtil.closeSilently(file, true);
      });
    }

    return schema;
  }


  /**
   *  Creates a InputSource object from a file or resource.
   * @param filename the path of the file or resource
   * @return A InputSource object that wraps the content of the specified file
   * @throws FileNotFoundException if the file name is not a valid file or resource
   * @throws NullPointerException if the file name is null
   */
  public static InputSource getFromFile(String filename) throws FileNotFoundException {
    if (filename == null) throw new NullPointerException("filename mustn't be null!");
    if (FileUtil.isResource(filename)) {

      URL resource = FileUtil.getResource(filename);
      filename = resource.getFile();
    }
    return new InputSource(new FileReader(filename));
  }

  /**
   * Creates a InputSource object from a string.
   * @param content The string representing a text file
   * @return A InputSource objects that represents the delivered string
   *
   * @throws NullPointerException If the provided string is <b>null</b>
   */
  public static InputSource getFromString(String content) {
    if (content == null) throw new NullPointerException("Null is not a valid String object!");
    return new InputSource(new StringReader(content));
  }

  /**
   * Parses a xml document from a file using a specified xml schema.
   * @param filename The xml file to parse
   * @param schema The schema to validate the xml content from the xml file
   * @return A DOM representing the parsed xml content from the xml file
   * @throws SAXException If the file couldn't be parsed using the specified schema
   */
  public static Document parseFromFile(String filename, Schema schema) throws SAXException {
    if (filename == null) throw new NullPointerException("filename mustn't be null!");
    try {
      InputSource is = getFromFile(filename);
      return parseFromSource(is, schema);
    } catch (FileNotFoundException | SAXException e) {
      throw new SAXException("Couldn't parse the file: " + filename, e);
    }
  }

  /**
   * Parses a xml document from an input source using a xml schema.
   * @param source The input source to parse
   * @param schema The schema to validate the xml content from <b>source</b>
   * @return A DOM representing the parsed xml content from <b>source</b>
   * @throws SAXException If <b>source</b> couldn't be parsed using <b>schema</b>
   * @throws NullPointerException If the input source is null
   */
  public static Document parseFromSource(InputSource source, Schema schema) throws SAXException {
    if (source == null) throw new NullPointerException("source mustn't be null!");
    DocumentBuilder builder = createDocBuilder(schema);
    try {
      return builder.parse(source);
    } catch (IOException | SAXException | IllegalArgumentException e) {
      throw new SAXException("Couldn't parse the input source.", e);
    }
  }

  /**
   * Parses a xml document from a string.
   * @param content contains the xml content
   * @param schema  The schema to validate the xml content from the string
   * @return A DOM representing the parsed xml content from the string
   * @throws SAXException If the xml content couldn't be parsed using <b>schema</b>
   * @throws NullPointerException If <b>content</b> is null
   */
  public static Document parseFromString(String content, Schema schema) throws SAXException {
    if (content == null) throw new NullPointerException("content mustn't be null!");
    InputSource is = getFromString(content);
    try {
      return parseFromSource(is, schema);
    } catch (SAXException e) {
      throw new SAXException("Couldn't parse xml content from string.", e);
    }
  }
}