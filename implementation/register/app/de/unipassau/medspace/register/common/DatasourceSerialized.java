package de.unipassau.medspace.register.common;


import play.data.validation.Constraints;

import java.util.List;

/**
 * Created by David Goeth on 20.09.2017.
 */
public class DatasourceSerialized {

  @Constraints.Required
  private String uri;

  private String description;

  private List<String> services;

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getServices() {
    return services;
  }

  public void setServices(List<String> services) {
    this.services = services;
  }
}