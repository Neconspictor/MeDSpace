package de.unipassau.medspace.common.config.general_wrapper;

import de.unipassau.medspace.common.register.Service;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.List;

/**
 * An XML adapter used to convert MeDSpace services and their respective XML configuration objects.
 */
public class ServicesAdapter extends XmlAdapter<Services, List<Service>> {

  @Override
  public List<Service> unmarshal(Services v) throws Exception {
    return v.getService();
  }

  @Override
  public Services marshal(List<Service> v) throws Exception {
    Services services = new Services();
    services.setService(v);
    return services;
  }
}