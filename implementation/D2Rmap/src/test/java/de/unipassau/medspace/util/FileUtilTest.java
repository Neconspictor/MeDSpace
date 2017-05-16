package de.unipassau.medspace.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by David Goeth on 16.05.2017.
 */
public class FileUtilTest {

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
}