package de.fuberlin.wiwiss.d2r;

import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class holding all unit tests for {@link Instance}
 */
public class InstanceTest {

  private static Logger log = LogManager.getLogger(InstanceTest.class);

  /*
   *  Tests for method Instance.Instance(String, Model)
   * */

  @Test
  public void constructorTestStringModel() {
    Model model = ModelFactory.createDefaultModel();
    final String uri = "test";
    Instance instance = new Instance(uri, model);
    Assert.assertNotNull("instance mustn't be null!", instance);
    check(instance, uri);
  }

  /*
   *  Tests for method Instance.setInstanceURI and Instance.getInstanceURI
   * */

  @Test
  public void setGetInstanceUriTest() {
    Model model = ModelFactory.createDefaultModel();
    Instance instance = new Instance(model);
    Assert.assertNotNull("instance mustn't be null!", instance);
    String uri = "testURI";
    Assert.assertFalse(instance.getInstanceURI().equals(uri));
    instance.setInstanceURI(uri);
    Assert.assertTrue(instance.getInstanceURI().equals(uri));
  }

  /*
   *  Tests for method Instance.setInstanceID and Instance.getInstanceID
   * */

  @Test
  public void setGetInstanceIDTest() {
    Model model = ModelFactory.createDefaultModel();
    Instance instance = new Instance(model);
    Assert.assertNotNull("instance mustn't be null!", instance);
    String id = "testID";
    if (instance.getInstanceID() != null)
      Assert.assertFalse(instance.getInstanceID().equals(id));
    instance.setInstanceID(id);
    Assert.assertTrue(instance.getInstanceID().equals(id));
  }

  /*
   *  Tests for method Instance.setInstanceID and Instance.getInstanceID
   * */

  @Test
  public void addPropertyTest() {
    Model model = ModelFactory.createDefaultModel();
    Instance instance = new Instance(model);
    Assert.assertNotNull("instance mustn't be null!", instance);
    Property property = new PropertyImpl("testURI");
    int integerValue = 1;
    RDFNode value = model.createTypedLiteral(integerValue);
    Resource resource = instance.getInstanceResource();
    Statement stmt = resource.getProperty(property);
    Assert.assertNull("The property to add haven't to be included already!", stmt);

    // now test property addition
    instance.addProperty(property, value);
    resource = instance.getInstanceResource();
    stmt = resource.getProperty(property);
    Assert.assertNotNull("The property to add haven't to be included already!", stmt);
    Assert.assertTrue("The returned property has to be equal to the added one!",stmt.getInt() == integerValue);
  }



  private void check(Instance instance, String uri) {
    int compare = instance.getInstanceURI().compareTo(uri);
    Assert.assertTrue("instance uri and provided uri are not equal!", compare == 0);
    compare = instance.getInstanceResource().getURI().compareTo(uri);
    Assert.assertTrue("instance resource uri and provided uri are not equal!", compare == 0);
  }

}