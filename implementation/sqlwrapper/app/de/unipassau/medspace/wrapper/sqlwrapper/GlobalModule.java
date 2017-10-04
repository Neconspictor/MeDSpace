package de.unipassau.medspace.wrapper.sqlwrapper;

import com.google.inject.AbstractModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.Configuration;
import play.api.Environment;

/**
 * The GlobalModule is a configuration class that configures the play framework for the sql wrapper.
 * It's main purpose is the definition of Dependency Injection definitions.
 */
public class GlobalModule extends AbstractModule {

  private static Logger log = LoggerFactory.getLogger(GlobalModule.class);

  /**
   * The environment of the play application.
   */
  private final Environment environment;

  /**
   * Creates a new GlobalModule.
   * @param environment The environment of the play application.
   * @param config Not used, but the play framework needs a constructor with this parameter.
   */
  public GlobalModule(Environment environment, Configuration config) {
    super();
    this.environment = environment;
  }

  @Override
  protected void configure() {
    if (environment.asJava().isTest()) return;
    log.info("GlobuleModule configures dependencies...");
    bind(SystemConfig.class).asEagerSingleton();
    bind(TestClient.class).asEagerSingleton();
    bind(SQLWrapperService.class).asEagerSingleton();
    log.info("done.");
  }
}