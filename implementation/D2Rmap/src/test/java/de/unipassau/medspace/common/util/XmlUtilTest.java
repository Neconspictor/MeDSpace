package de.unipassau.medspace.common.util;

import de.unipassau.medspace.d2r.exception.D2RException;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.Assert;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.validation.*;

/**
 * Test class holding all unit tests for {@link XmlUtil}
 */
public class XmlUtilTest {

  private static SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
  private static String NOT_FINDABLE_FILE = "/ANotValidFile.abc";
  private static String VALID_INPUT_SOURCE = "/test_validXMLFile.xml";
  private static String VALID_SCHEMA = "/test_validSchema.xsd";
  private static String INCOMPATIBLE_SCHEMA = "/test_incompatibleSchema.xsd";

  /*
   *  Tests for method XmlUtil.createDocBuilder
   * */

  @Test
  public void createDocBuilderTestNull() {
    DocumentBuilder builder = XmlUtil.createDocBuilder(null);
    Assert.assertNotNull("The returned document builder shouldn't be null", builder);
  }

  @Test
  public void createDocBuilderTestValidSchema() throws SAXException {
    DocumentBuilder builder = XmlUtil.createDocBuilder(factory.newSchema());
    Assert.assertNotNull("The returned document builder shouldn't be null", builder);
  }

  /*
   *  Tests for method XmlUtil.createSchema
   * */

  @Test
  public void createSchemaTestEmptySchemaArray() throws SAXException {
    Schema schema = XmlUtil.createSchema(new String[]{});
    Assert.assertNotNull("The returned schema shouldn't be null", schema);
  }

  @Test (expected=SAXException.class)
  public void createSchemaTestNotValidSchemaFiles() throws SAXException {
    Schema schema = XmlUtil.createSchema(new String[]{""});
  }

  @Test (expected=NullPointerException.class)
  public void createSchemaTestNull() throws SAXException {
      Schema schema = XmlUtil.createSchema(null);
  }

  @Test
  public void createSchemaTestValidSchemaFiles() throws SAXException {
    Schema schema = XmlUtil.createSchema(new String[]{"/test_validSchema.xsd"});
    Assert.assertNotNull("The returned schema is specified to be not null!", schema);

  }

  /*
   *  Tests for method XmlUtil.getFromFile
  * */

  @Test (expected=FileNotFoundException.class)
  public void  getFromFileTestNotFindableFile() throws FileNotFoundException {
    InputSource source = XmlUtil.getFromFile("AFileNotToBeFound.abc");
  }

  @Test (expected=FileNotFoundException.class)
  public void  getFromFileTestNotFindableResource() throws FileNotFoundException {
    InputSource source = XmlUtil.getFromFile("/AResourceNotToBeFound.abc");
  }

  @Test (expected=NullPointerException.class)
  public void  getFromFileTestNull() throws FileNotFoundException {
    InputSource source = XmlUtil.getFromFile(null);
  }

  @Test
  public void  getFromFileTestValidResource() throws FileNotFoundException {
    InputSource source = XmlUtil.getFromFile("/testSchema.xml");
    Assert.assertNotNull("On a valid resource file there should be returned a not null InputSource object!", source);
  }

  @Test
  public void  getFromFileTestValidFile() throws FileNotFoundException {
    InputSource source = XmlUtil.getFromFile("test/testFile.txt");
    Assert.assertNotNull("On a valid resource file there should be returned a not null InputSource object!", source);
  }


  /*
   *  Tests for method XmlUtil.getFromString
   * */

  @Test
  public void getFromStringTestEmptyString() {
    InputSource source = XmlUtil.getFromString("");
    Assert.assertNotNull("Even a empty string should produce a valid InputSource object!", source);
  }

  @Test
  public void getFromStringTestValidString() {
    InputSource source = XmlUtil.getFromString("This is a String representing a text file.");
    Assert.assertNotNull("The returned InputSource object shouldn't be null for a valid string object!", source);
  }

  @Test (expected=NullPointerException.class)
  public void getFromStringTestNull() {
    InputSource source = XmlUtil.getFromString(null);
  }


  /*
   *  Tests for method XmlUtil.parseFromFile
   * */

  @Test (expected=NullPointerException.class)
  public void parseFromFileTestNull() throws SAXException {
    Document doc = XmlUtil.parseFromFile(null, factory.newSchema());
  }

  @Test (expected=SAXException.class)
  public void parseFromFileTestNotFindableFile() throws SAXException {
    Document doc = XmlUtil.parseFromFile(NOT_FINDABLE_FILE, factory.newSchema());
  }

  @Test (expected=SAXException.class)
  public void parseFromFileTestNotValidFile() throws SAXException, D2RException {
    SourceSchema tuple = new SourceSchema(null, INCOMPATIBLE_SCHEMA);
    Document doc = XmlUtil.parseFromFile(VALID_INPUT_SOURCE, tuple.getSchema());
  }

  @Test
  public void parseFromFileTestValidFileNoSchema() throws SAXException {
    Document doc = XmlUtil.parseFromFile(VALID_INPUT_SOURCE, null);
    Assert.assertNotNull("The returned DocumentWrapper musnt'nt ne null!", doc);
  }

  @Test
  public void parseFromFileTestValidFileValidSchema() throws SAXException, D2RException {
    SourceSchema tuple = new SourceSchema(null, VALID_SCHEMA);
    Document doc = XmlUtil.parseFromFile(VALID_INPUT_SOURCE, tuple.getSchema());
    Assert.assertNotNull("The returned DocumentWrapper musnt'nt ne null!", doc);
  }

  /*
   *  Tests for method XmlUtil.parseFromSource
   * */

  @Test (expected=NullPointerException.class)
  public void parseFromSourceTestNull() throws SAXException {
    Document doc = XmlUtil.parseFromSource(null, factory.newSchema());
  }

  @Test
  public void parseFromSourceTestValidSchemaSource() throws SAXException, D2RException {
    SourceSchema tuple = new SourceSchema(VALID_INPUT_SOURCE, VALID_SCHEMA);
    Document doc = XmlUtil.parseFromSource(tuple.getSource(), tuple.getSchema());
    Assert.assertNotNull("The returned document mustn't be null!", doc);
  }

  @Test
  public void parseFromSourceTestValidSourceNoSchema() throws SAXException, D2RException {
    SourceSchema tuple = new SourceSchema(VALID_INPUT_SOURCE, null);
    Document doc = XmlUtil.parseFromSource(tuple.getSource(), null);
    Assert.assertNotNull("The returned document mustn't be null!", doc);
  }

  @Test (expected = SAXException.class)
  public void parseFromSourceTestValidSourceIncompatibleSchema() throws SAXException, D2RException {
    SourceSchema tuple = new SourceSchema(VALID_INPUT_SOURCE, INCOMPATIBLE_SCHEMA);
    Document doc = XmlUtil.parseFromSource(tuple.getSource(), tuple.getSchema());
  }

  /*
   *  Tests for method XmlUtil.parseFromString
   * */

  @Test (expected=NullPointerException.class)
  public void parseFromStringTestNull() throws SAXException {
    Document doc = XmlUtil.parseFromString(null, factory.newSchema());
  }

  @Test (expected=SAXException.class)
  public void parseFromStringTestNotValid() throws SAXException, D2RException {
    SourceSchema tuple = new SourceSchema(null, VALID_SCHEMA);
    Document doc = XmlUtil.parseFromString("<test></test>", tuple.getSchema());
  }

  @Test
  public void parseFromStringTestValidStringNoSchema() throws SAXException {
    Document doc = XmlUtil.parseFromString("<test></test>", null);
    Assert.assertNotNull("The returned DocumentWrapper musnt'nt ne null!", doc);
  }

  @Test
  public void parseFromStringTestValidStringValidSchema() throws SAXException, D2RException {
    SourceSchema tuple = new SourceSchema(null, VALID_SCHEMA);
    Document doc = XmlUtil.parseFromString("<TestElement></TestElement>", tuple.getSchema());
    Assert.assertNotNull("The returned DocumentWrapper musnt'nt ne null!", doc);
  }

  private class SourceSchema {
    private Schema schema;
    private InputSource source;

    public SourceSchema(String sourcePath, String schemaPath) throws D2RException {

      try {
        if (sourcePath != null) {
          URL url = Class.class.getResource(sourcePath);
          File file = Paths.get(url.toURI()).toFile();
          source = new InputSource(new FileInputStream(file));
        }

        if (schemaPath != null) {
          URL url = Class.class.getResource(schemaPath);
          File file = Paths.get(url.toURI()).toFile();
          schema = factory.newSchema(file);
        }
      } catch(FileNotFoundException | SAXException | URISyntaxException e) {
        throw new D2RException("Couldn't create SourceSchema object!");
      }
    }

    public Schema getSchema() {
      return schema;
    }

    public InputSource getSource() {
      return source;
    }
  }
}