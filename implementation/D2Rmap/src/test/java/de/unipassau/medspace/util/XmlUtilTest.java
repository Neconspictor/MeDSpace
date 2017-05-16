package de.unipassau.medspace.util;

import org.junit.Test;
import org.xml.sax.InputSource;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.xml.sax.SAXException;

import javax.xml.validation.Schema;

/**
 * Created by David Goeth on 16.05.2017.
 */
public class XmlUtilTest {


  /**
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

  /**
   *  Tests for method XmlUtil.getFromFile
  * */

  @Test (expected=FileNotFoundException.class)
  public void  getFromFileTestNotFindableFile() throws FileNotFoundException, URISyntaxException {
    InputSource source = XmlUtil.getFromFile("AFileNotToBeFound.abc");
  }

  @Test (expected=FileNotFoundException.class)
  public void  getFromFileTestNotFindableResource() throws FileNotFoundException {
    InputSource source = XmlUtil.getFromFile("/AResourceNotToBeFound.abc");
  }

  @Test
  public void  getFromFileTestValidResource() throws FileNotFoundException, URISyntaxException {
    InputSource source = XmlUtil.getFromFile("/testSchema.xml");
    Assert.assertNotNull("On a valid resource file there should be returned a not null InputSource object!", source);
  }

  @Test
  public void  getFromFileTestValidFile() throws FileNotFoundException, URISyntaxException {
    InputSource source = XmlUtil.getFromFile("test/testFile.txt");
    Assert.assertNotNull("On a valid resource file there should be returned a not null InputSource object!", source);
  }


  /**
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

  @Test (expected=IllegalArgumentException.class)
  public void getFromStringTestNull() {
    InputSource source = XmlUtil.getFromString(null);
  }
}