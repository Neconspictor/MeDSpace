package de.unipassau.medspace.common.register;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by David Goeth on 13.10.2017.
 */
public class Service {

  private String name;

  public Service(String name) {
    this.name = name.toLowerCase();
  }

  @JsonCreator
  public static Service create(@JsonProperty("name") String name) {
    return new Service(name);
  }

  public static boolean supportsService(Service service, Datasource datasource) {
    return datasource.getServices().stream().anyMatch(otherService ->
        otherService.name.equals(service.name));
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return "Service:['" + name + "']";
  }
}