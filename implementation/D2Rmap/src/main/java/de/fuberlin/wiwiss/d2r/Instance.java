package de.fuberlin.wiwiss.d2r;

import org.apache.jena.rdf.model.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * An instance. Instances correspondent with Jena resources but have some additional properties for identification.
 * <BR>History: 01-15-2003   : Initial version of this class.
 * <BR>History: 09-25-2003   : Changed for Jena2.
 * TODO this class properly should be refactored
 * @author Chris Bizer chris@bizer.de
 * @version V0.2
 */
public class Instance {
  private String instanceURI;
  private String instanceID;
  private Resource instanceResource;

  /** log4j logger used for this class */
  private static Logger log = LogManager.getLogger(Instance.class);

  /**
   * Creates a resource node instance.
   * @param  uri URI of the resource node.
   * @param  model Reference to an jena model.
   */
  protected Instance(String uri, Model model) {
    this.instanceURI = uri;
      this.instanceResource = model.createResource(uri);
  }

  /**
   * Creates a bNode node instance.
   * @param  model Reference to an jena model.
   */
  protected Instance(Model model) {
    this.instanceURI = "bNode";
      this.instanceResource = model.createResource();
  }

  protected String getInstanceURI() {
    return instanceURI;
  }

  protected void setInstanceURI(String instanceURI) {
    this.instanceURI = instanceURI;
  }

  protected String getInstanceID() {
    return instanceID;
  }

  protected void setInstanceID(String instanceID) {
    this.instanceID = instanceID;
  }

  protected Resource getInstanceResource() {
    return instanceResource;
  }

  /**
   * Add a property to an instance.
   * @param  property Jena property object.
   * @param  value Jena RDFNode (literal or resource).
   */
  protected void addProperty(Property property, RDFNode value) {
      this.instanceResource.addProperty(property, value);
  }
}