package de.unipassau.medspace.common.config.general_wrapper;

import de.unipassau.medspace.common.register.Service;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * An XML adapter for an XML service property. Is used to convert
 * between a Service and its string representation.
 */
public class ServiceAdapter  extends XmlAdapter<String, Service> {
  @Override
  public Service unmarshal(String v) throws Exception {
    return new Service(v);
  }

  @Override
  public String marshal(Service v) throws Exception {
    return v.getName();
  }
}