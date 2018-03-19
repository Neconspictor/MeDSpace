package de.unipassau.medspace.wrapper.pdf_wrapper.play;

import com.google.inject.AbstractModule;
import de.unipassau.medspace.common.play.ShutdownService;
import de.unipassau.medspace.common.play.wrapper.RegisterClient;
import de.unipassau.medspace.common.rdf.RDFProvider;
import de.unipassau.medspace.common.rdf.rdf4j.RDF4J_RDFProvider;
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
public class AppRoot extends AbstractModule {

  /**
   * Logger instance of this class.
   */
  private static Logger log = LoggerFactory.getLogger(AppRoot.class);

  /**
   * The environment of the play application.
   */
  private final Environment environment;

  /**
   * Creates a new AppRoot.
   * @param environment The environment of the play application.
   * @param config Not used, but the play framework needs a constructor with this parameter.
   */
  public AppRoot(Environment environment, Configuration config) {
    super();
    Locale.setDefault(Locale.US);
    this.environment = environment;
  }

  @Override
  protected void configure() {
    if (environment.asJava().isTest()) return;

    log.info("GlobuleModule configures dependencies...");
    bind(ShutdownService.class).asEagerSingleton();

    bind(RDFProvider.class)
        .to(RDF4J_RDFProvider.class)
        .asEagerSingleton();

    bind(PdfWrapperConfigProvider.class).asEagerSingleton();

    //Generics have to be included in a TypeLiteral

    bind(Wrapper.class)
        .toProvider(WrapperProvider.class)
        .asEagerSingleton();

    //bind(new TypeLiteral<D2rWrapper<?>>(){})
    //    .toProvider(WrapperProvider.class).asEagerSingleton();

    bind(RegisterClient.class).asEagerSingleton();
    bind(PdfWrapperService.class).asEagerSingleton();

    log.info("done.");
  }
}