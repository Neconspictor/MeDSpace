package de.unipassau.medspace.register.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.validation.Constraints;

import java.sql.Timestamp;
import java.util.*;

/**
 * NOTE: This class is immutable.
 */
public class Datasource {

  private final String uri;
  private final String description;
  private final Set<String> services;
  private final long timeStampMilliSeconds;

  private static Logger log = LoggerFactory.getLogger(Datasource.class);

  public Datasource(String uri, String desc, Set<String> services) {

    if (uri == null) {
      throw new IllegalArgumentException("URI isn't allowed to be null");
    }

    if (services == null) {
      this.services = new HashSet<>();
    } else {
      this.services = new HashSet<>(services);

      // remove null and empty list entries if they exist
      this.services.remove("");
      this.services.remove(null);
    }

    // we don't want any case mismatches when comparing URIs.
    this.uri = uri.toLowerCase();

    if (desc == null) {
      this.description = "";
    } else {
      this.description = desc;
    }
    timeStampMilliSeconds = System.currentTimeMillis();
  }

  /**
   * Creates a copy from the provided Datasource, but updates the time stamp.
   * @param other The datasource to copy.
   */
  public static Datasource copyUpdateTimeStamp(Datasource other) {
    return new Datasource(other.uri, other.description, other.services);
  }

  public static Datasource createFromMutable(MutableDatasource mutable) {
    if (mutable == null) return null;
    Set<String> set = new HashSet<>();
    if (mutable.services != null) {
      set.addAll(mutable.services);
    }
    return new Datasource(mutable.uri, mutable.description, set);
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
    // Note: Differences in the other attributes won't be considered!
    return uri.equals(oth.getUri());
  }

  public String getUri() {
    return uri;
  }

  public String getDescription() {
    return description;
  }

  public Set<String> getServices() {
    // create an unmodifiable set, so that this object remains immutable
    return Collections.unmodifiableSet(services);
  }

  public Timestamp getTimeStamp() {
    return new Timestamp(timeStampMilliSeconds);
  }

  @Override
  public int hashCode() {
    // uri is immutable, so it is safe to use its hash code
    // as the hash code won't change for any existing object
    return uri.hashCode();
  }

  @Override
  public String toString() {
    return "[" + uri + ", '" + description + "', services: " + services + ", time stamp: " + timeStampMilliSeconds + "]";
  }

  /**
   * Created by David Goeth on 20.09.2017.
   */
  public static class MutableDatasource {

    @Constraints.Required
    private String uri;

    private String description;

    private List<String> services;

    public String getUri() {
      return uri;
    }

    public void setUri(String uri) {
      this.uri = uri;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public List<String> getServices() {
      return services;
    }

    public void setServices(List<String> services) {
      this.services = services;
    }
  }
}