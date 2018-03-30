package de.unipassau.medspace.common.register;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.unipassau.medspace.common.exception.NoValidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * A Datasource contains all relevant information, the register needs to know of each wrapper.
 * The ost important property is the URI, as it is the location to communicate with the (datasource) wrapper.
 * <br>
 * <strong>NOTE:</strong> This class is <strong>immutable</strong>.
 */
public class Datasource implements Comparable<Datasource> {

  public static final String DEFAULT_FORMAT = "TURTLE";

  /**
   * Defines the location to query the wrapper.
   */
  private final URL url;

  /**
   * An optional description of the datasource. E.g. it can be described what kind of data are accessible.
   */
  private final String description;

  private final String rdfFormat;

  /**
   * A list of services that the datasource wrapper provides.
   */
  private final Set<Service> services;

  /**
   * Logger instance for this class.
   */
  private static Logger log = LoggerFactory.getLogger(Datasource.class);

  /**
   * Creates a new Datasource object.
   * @param url The URL to the datasource wrapper.
   * @param desc The description of the datasource wrapper. Can be null
   * @param rdfFormat The rdf format the datasource uses for rdf content publishing.
   * @param services A list of supported services, the datasource wrapper supplies.
   * @throws NoValidArgumentException If rdfFormat isn't a supported rdf format.
   * @throws IllegalArgumentException If url or rdfFormat are null
   */
  public Datasource(URL url, String desc, String rdfFormat, Set<Service> services) throws
      IllegalArgumentException, NoValidArgumentException {

    if (url == null) {
      throw new IllegalArgumentException("URI isn't allowed to be null");
    }

    if (rdfFormat == null) {
      throw new IllegalArgumentException("Format isn't allowed to be null!");
    }

    /*if (!isSupported(rdfFormat)) {
      throw new NoValidArgumentException("rdf format isn't supported");
    }*/

    if (services == null) {
      this.services = new HashSet<>();
    } else {
      this.services = new HashSet<>(services);

      // remove null and empty list entries if they exist
      this.services.remove(Service.EMPTY_SERVICE);
      this.services.remove(null);
    }

    // All datasources have to support the keyword search service
    // Even if it is not explicitly stated!
    this.services.add(Service.KEYWORD_SEARCH);

    // java.net.URL is immutable
    this.url = url;

    if (desc == null) {
      this.description = "";
    } else {
      this.description = desc;
    }


    this.rdfFormat = rdfFormat;

    if (log.isDebugEnabled())
      log.debug("New Datasource object created: " + this.toString());
  }

  /**
   * Creates a copy from the provided Datasource, but updates the time stamp.
   * @param other The Datasource to copy.
   *
   * @return a copy from the provided Datasource, but updates the time stamp.
   */
  public static Datasource copyUpdateTimeStamp(Datasource other)  {

    try {
      return new Datasource(other.url, other.description, other.rdfFormat, other.services);
    } catch (NoValidArgumentException e) {
      throw new RuntimeException("Couldn't copy and update timestamp!");
    }
  }

  @JsonCreator
  public static Datasource create(@JsonProperty("url") URL url,
                                  @JsonProperty("description") String desc,
                                  @JsonProperty("rdfFormat") String rdfFormat,
                                  @JsonProperty("services") Set<Service> services) throws NoValidArgumentException {
    if (rdfFormat == null) rdfFormat = DEFAULT_FORMAT;
    return new Datasource(url, desc, rdfFormat, services);
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
   * Provides the rdf format.
   * @return the rdf format.
   */
  public String getRdfFormat() {
    return rdfFormat;
  }

  /**
   * Provides read access to the set of services this datasource provides.
   * <br>
   * <strong>Note:</strong> The services are all in <strong>lower case</strong>
   * @return An unmodifiable set of the services of this datasource.
   */
  public Set<Service> getServices() {
    // create an unmodifiable set. As the list of services won't change after its creation
    // it is safe to use a wrapper instead of a copy!
    return Collections.unmodifiableSet(services);
  }

  @Override
  public int hashCode() {
    // url is immutable, so it is safe to use its hash code
    // as the hash code won't change for any existing object
    return url.hashCode();
  }

  @Override
  public String toString() {
    return "Datasource:[" + url + ", '" + description
        + "', format: " + rdfFormat
        + ", services: " + services  + "]";
  }

  @Override
  public int compareTo(Datasource other) {
    String thisUrlStr = url.toExternalForm();
    String otherUrlStr = other.url.toExternalForm();

    return thisUrlStr.compareTo(otherUrlStr);
  }

  /**
   * Represents a mutable Datasource. This class is intended to be used to create immutable Datasource objects.
   */
  public static class Builder {

    /**
     * The url of the datasource.
     */
    private URL url;

    /**
     * The description of this datasource.
     */
    private String description;

    /**
     * The rdf format the datasource uses for publishing rdf data.
     */
    private String rdfFormat;

    /**
     * A list of services this datasource provides.
     */
    private List<Service> services;

    /**
     * Creates a new Builder object.
     */
    public Builder() {
      url = null;
      description = null;
      services = null;
      rdfFormat = null;
    }

    /**
     * Creates a new Builder object and initializes it with a given datasource.
     * @param datasource Used to initialize this Builder.
     */
    public Builder(Datasource datasource) {
      url = datasource.url;
      description = datasource.description;
      services = new ArrayList<>(datasource.services);
      rdfFormat = datasource.rdfFormat;
    }

    /**
     * Creates an immutable Datasource object from this Builder object.
     * @return A Datasource object, that represents the same datasource as the mutable one. Or null, if
     * no valid datasource object could be created.
     * @throws NoValidArgumentException if the builder is wrong configured.
     */
    public Datasource build() throws NoValidArgumentException {
      Set<Service> set = new HashSet<>();
      if (services != null) set.addAll(services);
      if (rdfFormat == null) rdfFormat = DEFAULT_FORMAT;
      return new Datasource(url, description, rdfFormat, set);
    }

    /**
     * NOTE: DO NOT DELETE the following getters/setters, even if some of the methods aren't used in the project
     * apparently! The play framework uses Spring data binder for automatic binding
     * forms to java objects and it needs for each field member a getter and setter method.
     */

    /**
     * Provides the url of this datasource.
     * @return The url of this datasource.
     */
    public URL getUrl() {
      return url;
    }

    /**
     * Sets the url for this datasource.
     * @param url The new url for this datasource.
     */
    public void setUrl(URL url) {
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
     * Provides the rdf format the datasource uses for publishing rdf data.
     * @return The rdf format the datasource uses for publishing rdf data.
     */
    public String getRdfFormat() {
      return rdfFormat;
    }

    /**
     * Provides the list of services this datasource supplies.
     * @return The list of services of this datasource.
     */
    public List<Service> getServices() {
      return services;
    }

    /**
     * Sets the description for this datasource.
     * @param description The new description.
     */
    public void setDescription(String description) {
      this.description = description;
    }

    /**
     * Sets the rdf format the datasource uses for publishing rdf data.
     * @param rdfFormat The rdf export format.
     */
    public void setRdfFormat(String rdfFormat) {
      this.rdfFormat = rdfFormat;
    }

    /**
     * Sets the list of services for this datasource and lower cases each element.
     * @param services The new list of services.
     */
    public void setServices(List<Service> services) {
      this.services = services;
    }
  }
}