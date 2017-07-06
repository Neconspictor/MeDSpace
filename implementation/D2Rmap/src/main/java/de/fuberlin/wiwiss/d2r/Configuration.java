package de.fuberlin.wiwiss.d2r;

import org.apache.jena.riot.Lang;
import org.javatuples.Pair;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by David Goeth on 30.05.2017.
 */
public class Configuration {
  private String databasePassword;
  private List<Pair<String, String>> dataSourceProperties;
  private String databaseUsername;
  private String jdbc;
  private String jdbcDriver;
  private Path indexDirectory;
  private List<D2rMap> maps;
  private int maxConnections; //Connection configurations
  private HashMap<String, String> namespaces;
  private String prepend;
  private String postpend;
  private Lang outputFormat;
  private String saveAs;
  private HashMap<String, TranslationTable> translationTables;
  private boolean useIndex;

  public Configuration() {
    dataSourceProperties = new ArrayList<>();
    indexDirectory = Paths.get("./");
    maps = new ArrayList<>();
    namespaces = new HashMap<>();
    outputFormat = null;
    translationTables = new HashMap<>();
    useIndex = false;
  }

  public void addDataSourceProperty(String propertyName, String value) {
    dataSourceProperties.add(new Pair<>(propertyName, value));
  }

  public String getDatabasePassword() {
    return databasePassword;
  }

  public String getDatabaseUsername() {
    return databaseUsername;
  }

  public List<Pair<String, String>> getDataSourceProperties() {
    return dataSourceProperties;
  }

  public Path getIndexDirectory() {
    return indexDirectory;
  }

  public String getJdbc() {
    return jdbc;
  }

  public String getJdbcDriver() {
    return jdbcDriver;
  }

  public List<D2rMap> getMaps() {
    return maps;
  }

  public int getMaxConnections() {
    return maxConnections;
  }

  public HashMap<String, String> getNamespaces() {
    return namespaces;
  }

  public String getPostpend() {
    return postpend;
  }

  public String getPrepend() {
    return prepend;
  }

  public Lang getOutputFormat() {
    return outputFormat;
  }

  public String getSaveAs() {
    return saveAs;
  }

  public HashMap<String, TranslationTable> getTranslationTables() {
    return translationTables;
  }

  public boolean isIndexUsed() {
    return useIndex;
  }

  public void setDatabasePassword(String databasePassword) {
    this.databasePassword = databasePassword;
  }

  public void setDatabaseUsername(String databaseUsername) {
    this.databaseUsername = databaseUsername;
  }

  public void setJdbc(String jdbc) {
    this.jdbc = jdbc;
  }

  public void setJdbcDriver(String jdbcDriver) {
    this.jdbcDriver = jdbcDriver;
  }

  public void setIndexDirectory(Path indexDirectory) {
    this.indexDirectory = indexDirectory;
  }

  public void setMaps(List<D2rMap> maps) {
    this.maps = maps;
  }

  public void setNamespaces(HashMap<String, String> namespaces) {
    this.namespaces = namespaces;
  }

  public void setMaxConnections(int maxConnections) {
    this.maxConnections = maxConnections;
  }

  public void setPrepend(String prepend) {
    this.prepend = prepend;
  }

  public void setPostpend(String postpend) {
    this.postpend = postpend;
  }

  public void setOutputFormat(Lang outputFormat) {
    assert outputFormat != null;
    this.outputFormat = outputFormat;
  }

  public void setSaveAs(String saveAs) {
    this.saveAs = saveAs;
  }

  public void setTranslationTables(HashMap<String, TranslationTable> translationTables) {
    this.translationTables = translationTables;
  }

  public void setUseIndex(boolean useIndex) {
    this.useIndex = useIndex;
  }
}