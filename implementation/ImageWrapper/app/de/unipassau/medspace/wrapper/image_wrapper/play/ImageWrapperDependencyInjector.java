package de.unipassau.medspace.wrapper.image_wrapper.play;

import de.unipassau.medspace.common.play.WrapperDependencyInjector;
import de.unipassau.medspace.common.wrapper.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.Configuration;
import play.api.Environment;

import java.util.Locale;

/**
 * The AppRoot is a configuration class that configures the play framework for the sql wrapper.
 * It's main purpose is the definition of Dependency Injection definitions.
 */
public class ImageWrapperDependencyInjector extends WrapperDependencyInjector {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(ImageWrapperDependencyInjector.class);

  /**
   * The environment of the play application.
   */
  private final Environment environment;

  /**
   * Creates a new AppRoot.
   * @param environment The environment of the play application.
   * @param config Not used, but the play framework needs a constructor with this parameter.
   */
  public ImageWrapperDependencyInjector(Environment environment, Configuration config) {
    super(environment, config);
    Locale.setDefault(Locale.US);
    this.environment = environment;
  }

  @Override
  protected void configure() {

    // very important!
    super.configure();

    if (environment.asJava().isTest()) return;

    log.info("configure dependencies...");

    bind(DdsmConfigProvider.class).asEagerSingleton();

    bind(Wrapper.class)
        .toProvider(WrapperProvider.class)
        .asEagerSingleton();

    bind(ImageWrapperService.class).asEagerSingleton();

    log.info("...configured dependencies.");
  }
}