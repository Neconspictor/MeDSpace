package de.fuberlin.wiwiss.d2r;

import org.javatuples.Pair;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by David Goeth on 30.05.2017.
 */
public class Configuration {
  private String saveAs;
  private String outputFormat;
  private String jdbc;
  private String jdbcDriver;
  private String databaseUsername;
  private String databasePassword;
  private String prepend;
  private String postpend;
  private Vector<D2RMap> maps;
  private HashMap<String, TranslationTable> translationTables;
  private HashMap<String, String> namespaces;
  private Vector<Pair<String, String>> dataSourceProperties;

  //Connection configurations
  private int maxConnections;

  public Configuration() {
    maps = new Vector<>();
    namespaces = new HashMap<>();
    translationTables = new HashMap<>();
    dataSourceProperties = new Vector<>();
  }

  public Vector<Pair<String, String>> getDataSourceProperties() {
    return dataSourceProperties;
  }

  public String getSaveAs() {
    return saveAs;
  }

  public void setSaveAs(String saveAs) {
    this.saveAs = saveAs;
  }

  public String getOutputFormat() {
    return outputFormat;
  }

  public void setOutputFormat(String outputFormat) {
    this.outputFormat = outputFormat;
  }

  public String getJdbcDriver() {
    return jdbcDriver;
  }

  public void setJdbcDriver(String jdbcDriver) {
    this.jdbcDriver = jdbcDriver;
  }

  public String getJdbc() {
    return jdbc;
  }

  public void setJdbc(String jdbc) {
    this.jdbc = jdbc;
  }

  public String getDatabaseUsername() {
    return databaseUsername;
  }

  public void setDatabaseUsername(String databaseUsername) {
    this.databaseUsername = databaseUsername;
  }

  public String getDatabasePassword() {
    return databasePassword;
  }

  public void setDatabasePassword(String databasePassword) {
    this.databasePassword = databasePassword;
  }

  public String getPrepend() {
    return prepend;
  }

  public void setPrepend(String prepend) {
    this.prepend = prepend;
  }

  public String getPostpend() {
    return postpend;
  }

  public void setPostpend(String postpend) {
    this.postpend = postpend;
  }

  public Vector<D2RMap> getMaps() {
    return maps;
  }

  public void setMaps(Vector<D2RMap> maps) {
    this.maps = maps;
  }

  public HashMap<String, TranslationTable> getTranslationTables() {
    return translationTables;
  }

  public void setTranslationTables(HashMap<String, TranslationTable> translationTables) {
    this.translationTables = translationTables;
  }

  public HashMap<String, String> getNamespaces() {
    return namespaces;
  }

  public void setNamespaces(HashMap<String, String> namespaces) {
    this.namespaces = namespaces;
  }

  public int getMaxConnections() {
    return maxConnections;
  }

  public void setMaxConnections(int maxConnections) {
    this.maxConnections = maxConnections;
  }

  public void addDataSourceProperty(String propertyName, String value) {
    dataSourceProperties.add(new Pair<>(propertyName, value));
  }
}