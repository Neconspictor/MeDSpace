package de.unipassau.medspace.util;

import org.junit.Test;
import org.xml.sax.InputSource;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import org.junit.Assert;

/**
 * Created by David Goeth on 16.05.2017.
 */
public class XmlUtilTest {


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
}
