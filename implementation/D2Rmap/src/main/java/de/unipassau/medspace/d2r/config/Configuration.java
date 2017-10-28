package de.unipassau.medspace.d2r.config;

import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.d2r.D2rMap;
import org.apache.jena.riot.Lang;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.javatuples.Pair;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Holds the data of a D2rMap config file.
 */
public class Configuration {

  /**
   * The password used to authenticate into the database.
   */
  private String databasePassword;

  /**
   * Properties that should be send to the datasource.
   */
  private List<Pair<String, String>> dataSourceProperties;

  /**
   * The username used to authenticate into the database.
   */
  private String databaseUsername;

  /**
   * The jdbc url to the datasource
   */
  private String jdbc;

  /**
   * The jdbc driver class that should be used.
   */
  private Class jdbcDriver;

  /**
   * The directory the wrapper should store indexed data.
   */
  private Path indexDirectory;

  /**
   * The D2rMaps that were read from the config file.
   */
  private List<D2rMap> maps;

  /**
   * The maximal size of the connection pool.
   */
  private int maxConnections;

  /**
   * A prefix namespace mapping read from the config file.
   */
  private HashMap<String, Namespace> namespaces;

  /**
   * The export rdf language.
   */
  private Lang outputFormat;

  /**
   * The export rdf language.
   */
  private RDFFormat outputFormatRDF4J;

  /**
   * Specifies, if the wrapper should use an index.
   */
  private boolean useIndex;

  /**
   * Creates a new Configuration.
   */
  public Configuration() {
    dataSourceProperties = new ArrayList<>();
    indexDirectory = null; // null hints, that no index directory should be used
    maps = new ArrayList<>();
    namespaces = new HashMap<>();
    outputFormat = null;
    outputFormatRDF4J = null;
    useIndex = false;
  }

  /**
   * Adds a datasource property to the configuration.
   * @param propertyName The name of the property
   * @param value The value of the property.
   */
  public void addDataSourceProperty(String propertyName, String value) {
    dataSourceProperties.add(new Pair<>(propertyName, value));
  }

  /**
   * Provides the read database password.
   * @return The database password.
   */
  public String getDatabasePassword() {
    return databasePassword;
  }

  /**
   * Provides the read database user name.
   * @return The user name.
   */
  public String getDatabaseUsername() {
    return databaseUsername;
  }

  /**
   * Provides the datasource properties.
   * @return The datasource properties.
   */
  public List<Pair<String, String>> getDataSourceProperties() {
    return dataSourceProperties;
  }

  /**
   * Provides the index directory path.
   * @return The index directory.
   */
  public Path getIndexDirectory() {
    return indexDirectory;
  }

  /**
   * Provides the jdbc url to access the datasource.
   * @return The jdbc URL to the datasource.
   */
  public String getJdbc() {
    return jdbc;
  }

  /**
   * Provides the jdbc driver class.
   * @return The jdbc driver class.
   */
  public Class getJdbcDriver() {
    return jdbcDriver;
  }

  /**
   * Provides the read D2rMaps.
   * @return The read D2rMaps.
   */
  public List<D2rMap> getMaps() {
    return maps;
  }

  /**
   * Provides the maximal size of the connection pool a wrapper should use.
   * @return The maximal size of the connection pool a wrapper should use.
   */
  public int getMaxConnections() {
    return maxConnections;
  }

  /**
   * Provides the namespaces mapped by their prefixes.
   * @return The namespaces mapped by their prefixes.
   */
  public HashMap<String, Namespace> getNamespaces() {
    return namespaces;
  }

  /**
   * Provides the rdf output language.
   * @return The rdf output language.
   */
  public Lang getOutputFormat() {
    return outputFormat;
  }

  /**
   * Provides the rdf output language.
   * @return The rdf output language.
   */
  public RDFFormat getOutputFormatRDF4J() {
    return outputFormatRDF4J;
  }

  public String getOutputFormatString() {
    return outputFormatRDF4J.toString();
  }

  /**
   * Checks, if a wrapper should use an index.
   * @return Should the wrapper use an index?
   */
  public boolean isIndexUsed() {
    return useIndex;
  }

  /**
   * Sets the database password.
   * @param databasePassword The database password to use.
   */
  public void setDatabasePassword(String databasePassword) {
    this.databasePassword = databasePassword;
  }

  /**
   * Sets the database user name.
   * @param databaseUsername The database user name to use.
   */
  public void setDatabaseUsername(String databaseUsername) {
    this.databaseUsername = databaseUsername;
  }

  /**
   * Sets the jdbc URL to the datasource.
   * @param jdbc The jdbc URL to use.
   */
  public void setJdbc(String jdbc) {
    this.jdbc = jdbc;
  }

  /**
   * Sets the jdbc driver class.
   * @param jdbcDriver The jdbc driver class.
   */
  public void setJdbcDriver(Class jdbcDriver) {
    this.jdbcDriver = jdbcDriver;
  }

  /**
   * Sets the index directory a wrapper should use for indexed data.
   * @param indexDirectory The index directory.
   */
  public void setIndexDirectory(Path indexDirectory) {
    this.indexDirectory = indexDirectory.normalize();
  }

  /**
   * Sets the list of D2rMaps.
   * @param maps The D2rMaps to use.
   */
  public void setMaps(List<D2rMap> maps) {
    this.maps = maps;
  }

  /**
   * Sets the namespaces to use.
   * @param namespaces The namespaces to use.
   */
  public void setNamespaces(HashMap<String, Namespace> namespaces) {
    this.namespaces = namespaces;
  }

  /**
   * Sets the maximal connection pool size.
   * @param maxConnections The maximal connection pool size.
   */
  public void setMaxConnections(int maxConnections) {
    this.maxConnections = maxConnections;
  }

  /**
   * Sets the rdf export language.
   * @param outputFormat The rdf export language.
   */
  public void setOutputFormat(Lang outputFormat) {
    assert outputFormat != null;
    this.outputFormat = outputFormat;
  }

  /**
   * Sets the rdf export language.
   * @param outputFormat The rdf export language.
   */
  public void setOutputFormatRDF4J(RDFFormat outputFormat) {
    assert outputFormat != null;
    this.outputFormatRDF4J = outputFormat;
  }

  /**
   * Sets whether an index should be used by a wrapper.
   * @param useIndex Should an index be used?
   */
  public void setUseIndex(boolean useIndex) {
    this.useIndex = useIndex;
  }
}