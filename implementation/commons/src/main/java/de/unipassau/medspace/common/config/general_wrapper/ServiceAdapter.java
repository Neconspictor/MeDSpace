package de.unipassau.medspace.common.config.general_wrapper;

import de.unipassau.medspace.common.register.Service;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.List;

/**
 * Created by David Goeth on 1/25/2018.
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