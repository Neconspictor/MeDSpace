package de.unipassau.medspace.common.register;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a service of a datasource.
 * NOTE: This class is immutable.
 */
public class Service implements Comparable<Service> {

  /**
   * A service that actually does not represent any service.
   */
  public static final Service EMPTY_SERVICE = new Service("");

  /**
   * The keyword search service every datasource has to support.
   */
  public static final Service KEYWORD_SEARCH = new Service("keyword-search");

  private String name;

  /**
   * Creates a new Service object.
   * @param name The name of the service.
   */
  public Service(String name) {
    this.name = name.toLowerCase();
  }

  /**
   * Creates a new Service object.
   * @param name The name of the service.
   * @return a new Service object.
   */
  @JsonCreator
  public static Service create(@JsonProperty("name") String name) {
    return new Service(name);
  }

  /**
   * Checks if a service is supported by a datasource.
   * @param service the service
   * @param datasource the datasource
   * @return true if the service is supported by the datasource.
   */
  public static boolean supportsService(Service service, Datasource datasource) {
    return datasource.getServices().stream().anyMatch(otherService ->
        otherService.name.equals(service.name));
  }

  /**
   * Provides the name of this service.
   * @return the name of this service.
   */
  public String getName() {
    return name;
  }

  @Override
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