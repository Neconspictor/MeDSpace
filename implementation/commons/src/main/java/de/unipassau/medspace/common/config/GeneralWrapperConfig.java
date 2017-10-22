package de.unipassau.medspace.common.config;

import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.register.Datasource;
import org.apache.jena.riot.Lang;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A class for storing general configuration data all wrappers need.
 * Note: This class is immutable.
 */
public class GeneralWrapperConfig {

  private final GeneralWrapperConfigData data;

  private GeneralWrapperConfig(GeneralWrapperConfigData data) {
    // Use the copy constructor so that data is not modifiable outside from this class (-> immutability).
    this.data = new GeneralWrapperConfigData(data);
  }

  public Datasource getDatasource() {
    return data.datasource;
  }

  /**
   * The directory the wrapper should store indexed data.
   */
  public Path getIndexDirectory() {
    return data.indexDirectory;
  }

  /**
   * A prefix namespace mapping read from the config file.
   */
  public Map<String, Namespace> getNamespaces() {
    return Collections.unmodifiableMap(data.namespaces);
  }

  /**
   * The export rdf language.
   */
  public Lang getOutputFormat() {
    return data.outputFormat;
  }

  /**
   * The url of the register the wrapper should connect to.
   * @return The url to the register.
   */
  public URL getRegisterURL() {
    return data.registerURL;
  }

  /**
   * Checks, if the wrapper uses an index.
   * @return true, if the wrapper uses and index.
   */
  public boolean isIndexUsed() {
    return data.useIndex;
  }

  @Override
  public String toString() {
    return data.toString();
  }


  /**
   * A builder class for {@link GeneralWrapperConfig}.
   * This class uses the Builder pattern to change the config file while constructing it.
   */
  public static class GeneralWrapperConfigData {

    private Datasource datasource;

    /**
     * The directory the wrapper should store indexed data.
     */
    private Path indexDirectory;

    /**
     * A prefix namespace mapping read from the config file.
     */
    private Map<String, Namespace> namespaces;

    /**
     * The export rdf language.
     */
    private Lang outputFormat;

    /**
     * The url of the register the wrapper should connect to.
     */
    private URL registerURL;

    /**
     * Specifies, if the wrapper should use an index.
     */
    private boolean useIndex;

    public GeneralWrapperConfigData() {
      indexDirectory = null; // null hints, that no index directory should be used
      namespaces = new HashMap<>();
      outputFormat = null;
      useIndex = false;
    }

    /**
     * Copy constructor. Creates a depp copy of the specified {@link GeneralWrapperConfigData} object.
     * @param data Used to initialize this object
     */
    public GeneralWrapperConfigData(GeneralWrapperConfigData data) {

      //Datasource is immutable
      datasource = data.datasource;

      // Path implementations are immutable
      indexDirectory = data.indexDirectory;

      // Namespace is an immutable class.
      namespaces = new HashMap<>(data.namespaces);

      // Lang is immutable
      outputFormat = data.outputFormat;

      // URL is immutable
      registerURL = data.registerURL;

      useIndex = data.useIndex;
    }

    public GeneralWrapperConfig build() {
      return new GeneralWrapperConfig(this);
    }

    /**
     * Sets the index directory a wrapper should use for indexed data.
     * @param indexDirectory The index directory.
     */
    public void setIndexDirectory(Path indexDirectory) {
      this.indexDirectory = indexDirectory.normalize();
    }

    /**
     * Sets the namespaces to use.
     * @param namespaces The namespaces to use.
     */
    public void setNamespaces(HashMap<String, Namespace> namespaces) {
      this.namespaces = namespaces;
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
     * Sets whether an index should be used by a wrapper.
     * @param useIndex Should an index be used?
     */
    public void setUseIndex(boolean useIndex) {
      this.useIndex = useIndex;
    }

    /**
     * Provides the directory the wrapper should store the indexed data.
     * @return The directory the wrapper should store the indexed data.
     */
    public Path getIndexDirectory() {
      return indexDirectory;
    }

    /**
     * Provides the prefix namespace mapping read from the config file.
     * @return The prefix namespace mapping read from the config file.
     */
    public Map<String, Namespace> getNamespaces() {
      return namespaces;
    }

    /**
     * Provides the export rdf language.
     * @return The export rdf language.
     */
    public Lang getOutputFormat() {
      return outputFormat;
    }

    /**
     * The url of the register the wrapper should connect to.
     * @return The url of the register
     */
    public URL getRegisterURL() {
      return registerURL;
    }

    /**
     * Specifies, if the wrapper should use an index.
     * @return true, if the wrapper uses an index.
     */
    public boolean isUseIndex() {
      return useIndex;
    }

    public void setRegisterURL(URL registerURL) {
      this.registerURL = registerURL;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("GeneralConfig: [\n");

      builder.append("  indexDirectory:  " + indexDirectory + ",\n");
      builder.append("  namespaces: [\n");
      for (Map.Entry<String, Namespace> entry : namespaces.entrySet()) {
        builder.append("    (prefix: " + entry.getKey() + ", namespace: " + entry.getValue() + "),\n");
      }
      builder.append("  ],\n");
      builder.append("  outputFormat: " + outputFormat.toString() + ",\n");
      builder.append("  registerURL: " + registerURL + ",\n");
      builder.append("  useIndex: " + useIndex + "\n");
      builder.append("]");

      return builder.toString();
    }

    public Datasource getDatasource() {
      return datasource;
    }

    public void setDatasource(Datasource datasource) {
      this.datasource = datasource;
    }
  }
}