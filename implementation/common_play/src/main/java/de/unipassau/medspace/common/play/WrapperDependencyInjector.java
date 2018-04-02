package de.unipassau.medspace.common.play;

import com.google.inject.AbstractModule;
import de.unipassau.medspace.common.config.GeneralWrapperConfig;
import de.unipassau.medspace.common.config.PathResolveParser;
import de.unipassau.medspace.common.config.ServerConfig;
import de.unipassau.medspace.common.play.wrapper.RegisterClient;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.common.rdf.rdf4j.RDF4J_RDFProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.Configuration;
import play.api.Environment;

import javax.inject.Inject;
import java.util.Locale;

/**
 * A dependency injector class for wrappers
 */
public class WrapperDependencyInjector extends DependencyInjector {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(WrapperDependencyInjector.class);

  /**
   * The environment of the play application.
   */
  protected final Environment environment;

  /**
   * Creates a new WrapperDependencyInjector object.
   * NOTE: environment and configuration are both necessary for Play EVEN if you don't use them!
   * If one of the arguments is mising, Play is not able to instantiate them!
   *
   * @param environment The Play environment. Will be injected by Play.
   * @param configuration The Play configuration. Will be injected by Play.
   */
  @Inject
  public WrapperDependencyInjector(Environment environment, Configuration configuration) {
    super(environment, configuration);
    Locale.setDefault(Locale.US);
    this.environment = environment;
  }


  @Override
  protected void configure() {

    //very important
    super.configure();

    if (environment.asJava().isTest()) return;

    bind(GeneralWrapperConfig.class)
        .toProvider(GeneralConfigProvider.class).asEagerSingleton();


    log.info("configured dependencies.");
  }
}