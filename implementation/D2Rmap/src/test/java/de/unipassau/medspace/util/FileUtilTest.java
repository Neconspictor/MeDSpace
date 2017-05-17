package de.unipassau.medspace.util;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class holding all unit tests for {@link FileUtil}
 */
public class FileUtilTest {

  /**
   *  Tests for method FileUtil.getAbsoluteFilePathFromResource
   * */

  @Test (expected=IllegalArgumentException.class)
  public void  getAbsoluteFilePathFromResourceTestEmptyString() {
    String path = FileUtil.getAbsoluteFilePathFromResource("");
  }

  @Test (expected=IllegalArgumentException.class)
  public void  getAbsoluteFilePathFromResourceTestNoValidResource() {
    String path = FileUtil.getAbsoluteFilePathFromResource("test/testFile.txt");
  }

  @Test (expected=NullPointerException.class)
  public void  getAbsoluteFilePathFromResourceTestNull() {
    String path = FileUtil.getAbsoluteFilePathFromResource(null);
  }

  @Test
  public void  getAbsoluteFilePathFromResourceTestValidResource() {
    String path = FileUtil.getAbsoluteFilePathFromResource("/testSchema.xml");
    Assert.assertNotNull("The path of a valid resource mustn't be null!", path);
    Assert.assertFalse("The path of a valid resource mustn't be an empty string!", path.isEmpty());
  }

  /**
   *  Tests for method FileUtil.isResource
   * */

  @Test
  public void  isResourceTestValidResource() {
    boolean isResource = FileUtil.isResource("/testSchema.xml");
    Assert.assertTrue("A valid ressource should be recognised as a resource!", isResource);
  }

  @Test
  public void  isResourceTestNotValidResource() {
    boolean isResource = FileUtil.isResource("/ANotFindableResource.abc");
    Assert.assertFalse("A not valid ressource should be recognised as such!", isResource);
  }

  @Test
  public void  isResourceTestFilesArentResources() {
    boolean isResource = FileUtil.isResource("test/testFile.txt");
    Assert.assertFalse("A file isn't a resource!", isResource);
  }

  @Test(expected=NullPointerException.class)
  public void  isResourceTestNull() {
    boolean isResource = FileUtil.isResource(null);
    Assert.assertFalse("Null isn't a valid resource!", isResource);
  }
}