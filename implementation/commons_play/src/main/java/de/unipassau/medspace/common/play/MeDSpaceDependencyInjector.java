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
 * A default dependency injector class for MeDSpace modules
 */
public class MeDSpaceDependencyInjector extends AbstractModule {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(MeDSpaceDependencyInjector.class);

  /**
   * The environment of the play application.
   */
  protected final Environment environment;

  /**
   * Creates a new MeDSpaceDependencyInjector object.
   * NOTE: environment and configuration are both necessary for Play EVEN if you don't use them!
   * If one of the arguments is mising, Play is not able to instantiate them!
   *
   * @param environment The Play environment. Will be injected by Play.
   * @param configuration The Play configuration. Will be injected by Play.
   */
  @Inject
  public MeDSpaceDependencyInjector(Environment environment, Configuration configuration) {
    super();
    Locale.setDefault(Locale.US);
    this.environment = environment;
  }


  @Override
  protected void configure() {

    if (environment.asJava().isTest()) return;

    log.info("configure dependencies...");
    bind(ShutdownService.class).asEagerSingleton();

    bind(ResourceProvider.class).asEagerSingleton();

    bind(PathResolveParser.class)
        .toProvider(PathResolveParserProvider.class).asEagerSingleton();

    bind(ServerConfig.class)
        .toProvider(ServerConfigProvider.class).asEagerSingleton();

    bind(RDFProvider.class)
        .to(RDF4J_RDFProvider.class)
        .asEagerSingleton();

    bind(GeneralWrapperConfig.class)
        .toProvider(GeneralConfigProvider.class).asEagerSingleton();

    bind(RegisterClient.class).asEagerSingleton();

    log.info("configured dependencies.");
  }
}