package de.unipassau.medspace.common.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created by David Goeth on 22.01.2018.
 */
public class ServerConfig {

  protected InetSocketAddress address;

  protected File generalWrapperConfigFile;

  public ServerConfig(String hostAddress, int port)
      throws UnknownHostException {

    InetAddress inetAddress = InetAddress.getByName(hostAddress);
    address = new InetSocketAddress(inetAddress, port);
  }

  public File getGeneralWrapperConfigFile() {
    return generalWrapperConfigFile;
  }

  public String getHostName() {
    return address.getHostName();
  }

  public int getPort() {
    return address.getPort();
  }

  public InetSocketAddress getServerAddress() {
   return address;
  }
}