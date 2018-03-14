package de.unipassau.medspace.common.register;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by David Goeth on 13.10.2017.
 */
public class Service implements Comparable<Service> {

  private String name;

  public static final Service EMPTY_SERVICE = new Service("");
  public static final Service KEYWORD_SEARCH = new Service("keyword-search");

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

  @Override
  public int compareTo(Service o) {
    if (o == null) return 1;
    if (o.equals(this)) return 0;
    return name.compareTo(o.name);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    if (o == this) return true;
    if (!(o instanceof Service)) {
      return false;
    }

    Service other = (Service) o;
    return name.compareTo(other.name) == 0;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}