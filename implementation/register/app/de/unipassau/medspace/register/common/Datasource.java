package de.unipassau.medspace.register.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NOTE: This class is immutable.
 */
public class Datasource {

  private final String uri;
  private final String description;
  private final List<String> services;

  public Datasource(String uri, String desc, List<String> services) {

    if (uri == null) {
      throw new IllegalArgumentException("URI isn't allowed to be null");
    }

    if (services == null) {
      this.services = new ArrayList<>();
    } else {
      this.services = new ArrayList<>(services);
    }

    // we don't want any case mismatches when comparing URIs.
    this.uri = uri.toLowerCase();

    if (desc == null) {
      this.description = "";
    } else {
      this.description = desc;
    }
  }

  @Override
  public boolean equals(Object o) {

    // self check
    if (this == o) return true;

    //null check
    if (o == null) return false;

    //type check and cast
    if (getClass() != o.getClass())
      return false;

    Datasource oth = (Datasource) o;

    // The URIs are expected to be both lower (resp. upper) case
    // So a simple equals check is enough
    return uri.equals(oth.getUri());
  }

  public String getUri() {
    return uri;
  }

  public String getDescription() {
    return description;
  }

  public List<String> getServices() {
    return Collections.unmodifiableList(services);
  }

  @Override
  public int hashCode() {
    // uri is immutable, so it is safe to use its hash code
    // as the hash code won't change for any existing object
    return uri.hashCode();
  }

  @Override
  public String toString() {
    return "[" + uri + ", '" + description + "', services: " + services + "]";
  }
}