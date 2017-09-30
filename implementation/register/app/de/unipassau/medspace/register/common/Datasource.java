package de.unipassau.medspace.register.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.validation.Constraints;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;

/**
 * A Datasource contains all relevant information, the register needs to know of each wrapper.
 * The ost important property is the URI, as it is the location to communicate with the (datasource) wrapper.
 * <br>
 * <strong>NOTE:</strong> This class is <strong>immutable</string>.
 */
public class Datasource {

  /**
   * Defines the location to query the wrapper.
   */
  private final URL url;

  /**
   * An optional description of the datasource. E.g. it can be described what kind of data are accessible.
   */
  private final String description;

  /**
   * A list of services that the datasource wrapper provides.
   */
  private final Set<String> services;

  /**
   * The time stamp this object was created at.
   */
  private final long timeStampMilliSeconds;

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(Datasource.class);

  /**
   * Creates a new Datasource object.
   * @param url The URL to the datasource wrapper.
   * @param desc The description of the datasource wrapper. Can be null
   * @param services A list of supported services, the datasource wrapper supplies.
   */
  public Datasource(String url, String desc, Set<String> services) throws MalformedURLException {

    if (url == null) {
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
    this.url = new URL(url);

    if (desc == null) {
      this.description = "";
    } else {
      this.description = desc;
    }
    timeStampMilliSeconds = System.currentTimeMillis();

    if (log.isDebugEnabled())
      log.debug("New Datasource object created: " + this.toString());
  }

  /**
   * Creates a copy from the provided Datasource, but updates the time stamp.
   * @param other The Datasource to copy.
   */
  public static Datasource copyUpdateTimeStamp(Datasource other) throws MalformedURLException {
    return new Datasource(other.url.toExternalForm(), other.description, other.services);
  }

  /**
   * Creates an immutable Datasource object from a MutableDatasource object.
   * @param mutable The MutableDatasource object used to create an immutable one.
   * @return A Datasource object, that represents the same datasource as the mutable one. Or null, if
   * no valid datasource object could be created.
   */
  public static Datasource createFromMutable(MutableDatasource mutable) {
    if (mutable == null) return null;
    Set<String> set = new HashSet<>();
    if (mutable.services != null) {
      set.addAll(mutable.services);
    }
    try {
      return new Datasource(mutable.url, mutable.description, set);
    } catch (MalformedURLException e) {
      if (log.isDebugEnabled()) {
        log.debug("Couldn't create new Datasource: " + e.getMessage());
      }
    }

    return null;
  }

  /**
   * A Datasource object is equal to another Object, if they are both Datasource objects and their URI field
   * member are identical. Other properties like the list of services or the description are not considered.
   * @param o The other object to test for equality.
   * @return true if this object and the other object are considered to be equal. Otherwise false.
   */
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
    return url.equals(oth.getUrl());
  }

  /**
   * Provides the location of the datasource (wrapper).
   * @return The location of the datasource.
   */
  public URL getUrl() {
    return url;
  }

  /**
   * Provides the description of the datasource.
   * @return The description of the datasource.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Provides read access to the set of services this datasource provides.
   * @return An unmodifiable set of the services of this datasource.
   */
  public Set<String> getServices() {
    // create an unmodifiable set. As the list of services won't change after its creation
    // it is safe to use a wrapper instead of a copy!
    return Collections.unmodifiableSet(services);
  }

  /**
   * Returns a copy of the time stamp this object was created at.
   * @return The time stamp this object was created at.
   */
  public Timestamp getTimeStamp() {
    return new Timestamp(timeStampMilliSeconds);
  }

  @Override
  public int hashCode() {
    // url is immutable, so it is safe to use its hash code
    // as the hash code won't change for any existing object
    return url.hashCode();
  }

  @Override
  public String toString() {
    return "[" + url + ", '" + description + "', services: " + services + ", time stamp: " + timeStampMilliSeconds + "]";
  }

  /**
   * Represents a mutable Datasource. This class is intended to be used to create immutable Datasource objects.
   */
  public static class MutableDatasource {

    /**
     * The url of the datasource.
     */
    @Constraints.Required
    private String url;

    /**
     * The descrption of this datasource.
     */
    private String description;

    /**
     * A list of services this datasource provides.
     */
    private List<String> services;

    /**
     * Provides the url of this datasource.
     * @return The url of this datasource.
     */
    public String getUrl() {
      return url;
    }

    /**
     * Sets the url for this datasource.
     * @param url The new url for this datasource.
     */
    public void setUrl(String url) {
      this.url = url;
    }

    /**
     * Provides the description of this datasource.
     * @return The description of this datasource.
     */
    public String getDescription() {
      return description;
    }

    /**
     * Sets the description for this datasource.
     * @param description The new description.
     */
    public void setDescription(String description) {
      this.description = description;
    }

    /**
     * Provides the list of services this datasource supplies.
     * @return The list of services of this datasource.
     */
    public List<String> getServices() {
      return services;
    }

    /**
     * Sets the list of services for this datasource.
     * @param services The new list of services.
     */
    public void setServices(List<String> services) {
      this.services = services;
    }
  }
}