package de.unipassau.medspace.common.config;

import com.google.common.collect.Lists;
import de.unipassau.medspace.common.rdf.Namespace;
import de.unipassau.medspace.common.register.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class for storing general configuration data all wrappers need.
 * Note: This class is immutable.
 */
public class GeneralWrapperConfig {

  /**
   * This builder object is used so that users can build a confugration file
   * but the resulting configuration (this class) remains immutable.
   */
  private final GeneralWrapperConfigData data;

  /**
   * Default constructor.
   * Note: DO NOT DELETE, as it is needed for dependency injection.
   * Should NOT be used in user code.
   */
  public GeneralWrapperConfig() {
    data = null;
  }

  /**
   * Creates a new GeneralWrapperConfig from a builder object.
   * @param data The builder object.
   */
  private GeneralWrapperConfig(GeneralWrapperConfigData data) {
    // Use the copy constructor so that data is not modifiable outside from this class (-> immutability).
    this.data = new GeneralWrapperConfigData(data);
  }

  /**
   * Provides the option whether the wrapper should reindex the data on startup.
   * @return the option whether the wrapper should reindex the data on startup.
   */
  public boolean getForceReindex() {
    return data.forceReindex;
  }

  /**
   * Provides the datasource description.
   * @return The datasource description.
   */
  public String getDescription() {
    return data.description;
  }

  /**
   * Provides an unmodifiable list of supported services.
   * @return An unmodifiable list of supported services.
   */
  public List<Service> getServices() {
    return Collections.unmodifiableList(data.services);
  }

  /**
   * Provides the directory the wrapper should store indexed data.
   * @return the directory the wrapper should store indexed data.
   */
  public Path getIndexDirectory() {
    return data.indexDirectory;
  }

  /**
   * Provides the prefix namespace mapping read from the config file.
   * @return the prefix namespace mapping read from the config file.
   */
  public Map<String, Namespace> getNamespaces() {
    return Collections.unmodifiableMap(data.namespaces);
  }

  /**
   * Provides the export rdf language.
   * @return the export rdf language.
   */
  public String getOutputFormat() {
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

  /**
   * Specifies whether the wrapper should connect to the register on startup.
   * @return true if the wrapper should connect to the register on startup.
   */
  public boolean getConnectToRegister() {
    return data.connectToRegister;
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

    /**
     * The datasource description.
     */
    private String description;

    /**
     * The option whether the wrapper should reindex the data on startup.
     */
    private boolean forceReindex;


    /**
     * The list of supported services.
     */
    private List<Service> services;

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
    private String outputFormat;

    /**
     * The url of the register the wrapper should connect to.
     */
    private URL registerURL;

    /**
     * Specifies, if the wrapper should use an index.
     */
    private boolean useIndex;

    /**
     * Specifies, whether the wrapper should establish a connection to the register
     * and thus registering itself.
     */
    private boolean connectToRegister;

    /**
     * Creates a default GeneralWrapperConfigData object.
     */
    public GeneralWrapperConfigData() {
      forceReindex = false;
      indexDirectory = null; // null hints, that no index directory should be used
      namespaces = new HashMap<>();
      outputFormat = null;
      useIndex = false;
      connectToRegister = true;
    }

    /**
     * Copy constructor. Creates a depp copy of the specified {@link GeneralWrapperConfigData} object.
     * @param data Used to initialize this object
     */
    public GeneralWrapperConfigData(GeneralWrapperConfigData data) {

      description = data.description.trim();

      forceReindex = data.forceReindex;

      services = Lists.newLinkedList(data.services);

      // Path implementations are immutable
      indexDirectory = data.indexDirectory;

      // Namespace is an immutable class.
      namespaces = new HashMap<>(data.namespaces);

      // Lang is immutable
      outputFormat = data.outputFormat;

      // URL is immutable
      registerURL = data.registerURL;

      useIndex = data.useIndex;

      connectToRegister = data.connectToRegister;
    }

    /**
     * Builds an immutable GeneralWrapperConfig object from this builder object.
     * @return an immutable GeneralWrapperConfig object from this builder object.
     */
    public GeneralWrapperConfig build() {
      return new GeneralWrapperConfig(this);
    }


    /**
     * Provides the option whether the wrapper should reindex the data on startup.
     * @return The option whether the wrapper should reindex the data on startup.
     */
    public boolean getForceReindex() {
      return forceReindex;
    }

    /**
     * Sets the option whether the wrapper should reindex the data on startup.
     * @param forceReindex The option whether the wrapper should reindex the data on startup.
     */
    public void setForceReindex(boolean forceReindex) {
       this.forceReindex = forceReindex;
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
    public void setNamespaces(Map<String, Namespace> namespaces) {
      this.namespaces = namespaces;
    }

    /**
     * Sets the rdf export language.
     * @param outputFormat The rdf export language.
     */
    public void setOutputFormat(String outputFormat) {
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
    public String getOutputFormat() {
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

    /**
     * Sets the URL to the register.
     * @param registerURL The URL to the register.
     */
    public void setRegisterURL(URL registerURL) {
      String url = registerURL.toExternalForm();

      // The url has to end with a '/'
      if (!url.endsWith("/")) {
        url += "/";
      }

      try {
        this.registerURL = new URL(url);
      } catch (MalformedURLException e) {
        throw new IllegalStateException("Couldn't create URL out of an existing URL?!");
      }
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("GeneralConfig: [\n");
      builder.append("  description: '" + description + "',\n");
      builder.append("  services: [\n");
      for (Service service : services) {
        builder.append("    " + service.toString() + ",\n");
      }
      builder.append("  ],\n");
      builder.append("  indexDirectory: " + indexDirectory + ",\n");
      builder.append("  namespaces: [\n");
      for (Map.Entry<String, Namespace> entry : namespaces.entrySet()) {
        builder.append("    (prefix: " + entry.getKey() + ", namespace: " + entry.getValue() + "),\n");
      }
      builder.append("  ],\n");
      builder.append("  outputFormat: " + outputFormat + ",\n");
      builder.append("  registerURL: " + registerURL + ",\n");
      builder.append("  useIndex: " + useIndex + "\n");
      builder.append("  connectToRegister: " + connectToRegister + "\n");
      builder.append("]");

      return builder.toString();
    }

    /**
     * The description of the datasource.
     * @return The description of the datasource.
     */
    public String getDescription() {
      return description;
    }

    /**
     * Sets the description of the datasource.
     * @param description the description of the datasource.
     */
    public void setDescription(String description) {
      this.description = description;
    }

    /**
     * Should the wrapper should connect to the register on startup ?
     * @return true if the wrapper should connect to the register on startup.
     */
    public boolean getConnectToRegister() {
      return connectToRegister;
    }

    /**
     * Specifies whether the wrapper should connect to the register on startup.
     * @param connectToRegister Should the wrapper connect to the register on startup?
     */
    public void setConnectToRegister(boolean connectToRegister) {
      this.connectToRegister = connectToRegister;
    }

    /**
     * Provides the list of supported services.
     * @return the list of supported services.
     */
    public List<Service> getServices() {
      return services;
    }

    /**
     * Sets the list of supported services.
     * @param services The list of supported services.
     */
    public void setServices(List<Service> services) {
      this.services = services;
    }
  }
}