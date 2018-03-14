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
   * TODO
   */
  private final GeneralWrapperConfigData data;

  /**
   * TODO
   * @param data
   */
  private GeneralWrapperConfig(GeneralWrapperConfigData data) {
    // Use the copy constructor so that data is not modifiable outside from this class (-> immutability).
    this.data = new GeneralWrapperConfigData(data);
  }

  /**
   * TODO
   * @return
   */
  public String getDescription() {
    return data.description;
  }

  /**
   * TODO
   * @return
   */
  public List<Service> getServices() {
    return Collections.unmodifiableList(data.services);
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
   * TODO
   * @return
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
     * TODO
     */
    private String description;

    /**
     * TODO
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

    public GeneralWrapperConfigData() {
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
     * TODO
     * @return
     */
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
     * TODO
     * @param registerURL
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
     * TODO
     * @return
     */
    public String getDescription() {
      return description;
    }

    /**
     * TODO
     * @param description
     */
    public void setDescription(String description) {
      this.description = description;
    }

    /**
     * TODO
     * @return
     */
    public boolean getConnectToRegister() {
      return connectToRegister;
    }

    /**
     * TODO
     * @param connectToRegister
     */
    public void setConnectToRegister(boolean connectToRegister) {
      this.connectToRegister = connectToRegister;
    }

    /**
     * TODO
     */
    public List<Service> getServices() {
      return services;
    }

    /**
     * TODO
     * @param services
     */
    public void setServices(List<Service> services) {
      this.services = services;
    }
  }
}