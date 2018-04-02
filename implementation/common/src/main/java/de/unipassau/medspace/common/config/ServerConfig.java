package de.unipassau.medspace.common.config;

import java.io.File;
import java.net.*;

/**
 * A class holding state of the server configuration.
 */
public class ServerConfig {

  /**
   * The URL of the server.
   */
  protected URL serverURL;

  /**
   * The file that contains general wrapper configuration.
   */
  protected File generalWrapperConfigFile;

  /**
   * Default constructor.
   * Note: DO NOT DELETE, as it is needed for dependency injection.
   * Should NOT be used in user code.
   */
  public ServerConfig() {

  }

  /**
   * Creates a new server configuration.
   * @param protocol The used protocol the server uses.
   * @param hostAddress The host address of the server.
   * @param port The port the server is running on.
   * @throws MalformedURLException If the combination of protocol, host address and port doesn't form a valid URL.
   */
  public ServerConfig(String protocol, String hostAddress, int port)
      throws MalformedURLException {
    serverURL = new URL(protocol + hostAddress + ":"  + port);
  }

  /**
   * Provides the file that contains general wrapper configuration.
   * @return The file that contains general wrapper configuration.
   */
  public File getGeneralWrapperConfigFile() {
    return generalWrapperConfigFile;
  }

  /**
   * Provides the URL of the server.
   * @return The URL of the server.
   */
  public URL getServerURL() {
   return serverURL;
  }
}