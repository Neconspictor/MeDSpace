package de.unipassau.medspace.common.config;

import java.io.File;
import java.net.*;

/**
 * Created by David Goeth on 22.01.2018.
 */
public class ServerConfig {

  protected URL serverURL;

  protected File generalWrapperConfigFile;

  public ServerConfig(String protocol, String hostAddress, int port)
      throws MalformedURLException {
    serverURL = new URL(protocol + hostAddress + ":"  + port);
  }

  public File getGeneralWrapperConfigFile() {
    return generalWrapperConfigFile;
  }

  public URL getServerURL() {
   return serverURL;
  }
}